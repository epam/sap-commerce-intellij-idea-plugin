/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.properties.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.exec.CxRemotePropertyClient
import sap.commerce.toolset.properties.exec.CxRemotePropertyStatePage
import sap.commerce.toolset.properties.mcp.context.CxComparePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxListRemotePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxPropertyReportMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxValidatePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxWriteRemotePropertyMcpRequest
import sap.commerce.toolset.properties.mcp.dto.*
import sap.commerce.toolset.properties.meta.CxProperty
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyComparison
import sap.commerce.toolset.properties.meta.CxPropertyDeclaration
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyRule
import sap.commerce.toolset.properties.meta.CxPropertyRuleSeverity
import sap.commerce.toolset.properties.meta.CxPropertyValidator
import sap.commerce.toolset.properties.meta.CxPropertyViolation
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * Everything the property tools actually do.
 *
 * Two sides are on offer and they answer different questions: an instance knows what it is running right now, the
 * project's own files know what it was told to run and where that was written down. A comparison is only meaningful
 * when both were asked.
 */
@Service(Service.Level.PROJECT)
class PropertiesMcpService(private val project: Project) {

    suspend fun listRemoteProperties(request: CxListRemotePropertiesMcpRequest): CxRemotePropertiesDto {
        requirePositive(request.limit)
        val connection = request.connection(project)
        val page = fetchRemote(connection, request.keyFilter, request.valueFilter, request.limit)
        val items = page.properties.take(request.limit)

        return CxRemotePropertiesDto(
            connection = connection.connectionName,
            keyFilter = request.keyFilter?.trim()?.takeIf { it.isNotEmpty() },
            valueFilter = request.valueFilter?.trim()?.takeIf { it.isNotEmpty() },
            returned = items.size,
            total = page.totalItems,
            truncated = items.size < page.totalItems,
            items = items.map { CxPropertyValueDto(it.key, it.value) },
        )
    }

    suspend fun setRemoteProperty(request: CxWriteRemotePropertyMcpRequest): CxRemotePropertyWriteDto {
        val connection = request.connection(project)
        val value = request.value.orEmpty()
        val outcome = CxRemotePropertyClient.getInstance(project).upsert(connection, request.key, value)
        if (outcome.success) refreshToolWindow(connection)

        return CxRemotePropertyWriteDto(
            connection = connection.connectionName,
            key = request.key.trim(),
            success = outcome.success,
            value = value.takeIf { outcome.success },
            error = outcome.reason,
        )
    }

    suspend fun deleteRemoteProperty(request: CxWriteRemotePropertyMcpRequest): CxRemotePropertyWriteDto {
        val connection = request.connection(project)
        val outcome = CxRemotePropertyClient.getInstance(project).delete(connection, request.key)
        if (outcome.success) refreshToolWindow(connection)

        return CxRemotePropertyWriteDto(
            connection = connection.connectionName,
            key = request.key.trim(),
            success = outcome.success,
            error = outcome.reason,
        )
    }

    suspend fun compareProperties(request: CxComparePropertiesMcpRequest): CxPropertyComparisonDto {
        requirePositive(request.limit)
        val connection = request.connection(project)
        val chain = chain()
        // Both halves go through the same matcher: the instance only knows a literal substring, so a regex handed to it
        // would select another population than on the project side and everything outside it would read as missing.
        val matches = request.keyFilter
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { regexOrContainsMatcher(it) }
            ?: { true }

        // The script pages from offset 0, so an unbounded page is every property of the instance.
        val remote = (CxRemotePropertyClient.getInstance(project).fetch(connection, pageSize = Int.MAX_VALUE)
            ?: error("${connection.connectionName} returned no readable properties payload."))
            .properties
            .filter { matches(it.key) }
        val declared = chain.resolveAll().filterKeys(matches)

        val missingInProject = CxPropertyComparison.missingInProject(remote, chain).filter { matches(it.key) }
        val missingOnRemote = CxPropertyComparison.missingOnRemote(remote, chain).filter { matches(it.key) }
        val differing = CxPropertyComparison.differing(remote, declared)
            .map { CxPropertyDifferenceDto(it.key, it.project, it.remote) }

        return CxPropertyComparisonDto(
            connection = connection.connectionName,
            remoteProperties = remote.size,
            projectProperties = declared.size,
            missingInProjectTotal = missingInProject.size,
            missingOnRemoteTotal = missingOnRemote.size,
            differingTotal = differing.size,
            truncated = listOf(missingInProject, missingOnRemote, differing).any { it.size > request.limit },
            missingInProject = missingInProject.take(request.limit).map { CxPropertyValueDto(it.key, it.value) },
            missingOnRemote = missingOnRemote.take(request.limit).map { CxPropertyValueDto(it.key, it.value) },
            differing = differing.take(request.limit),
        )
    }

    suspend fun reportProperty(request: CxPropertyReportMcpRequest): CxPropertyReportDto {
        val key = request.key.trim()
        val chain = chain()
        val property = chain[key]
        val effectiveValue = chain.resolve(key)
        val rule = CxPropertyRule.of(key)?.toDto()

        if (!request.compareWithRemote) {
            return report(key, property, effectiveValue, rule)
        }

        val connection = catching { request.connection(project) }
        val remote = connection.getOrNull()
            ?.let { server -> catching { fetchRemote(server, key, null, REMOTE_LOOKUP_LIMIT) } }

        val remoteValue = remote
            ?.getOrNull()
            ?.properties
            // The instance matches the filter as a substring, so a key which merely contains this one is not it.
            ?.find { it.key == key }
            ?.value

        return report(key, property, effectiveValue, rule).copy(
            connection = connection.getOrNull()?.connectionName,
            remoteValue = remoteValue,
            differsFromRemote = remoteValue?.let { it != effectiveValue },
            remoteError = (connection.exceptionOrNull() ?: remote?.exceptionOrNull())?.message,
        )
    }

    suspend fun validateProperties(request: CxValidatePropertiesMcpRequest): CxPropertyValidationDto {
        requirePositive(request.limit)
        val severity = request.severity
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                CxPropertyRuleSeverity.of(it)
                    ?: error("Invalid severity '$it'. Valid values: ${CxPropertyRuleSeverity.entries.joinToString { severity -> severity.title }}")
            }
        val chain = chain()
        val violations = CxPropertyValidator.validate(chain)
            .filter { severity == null || it.severity == severity }
            .filter { request.extension == null || it.source.extension == request.extension }

        return CxPropertyValidationDto(
            sources = chain.sources.size,
            properties = chain.keys.size,
            errors = violations.count { it.severity == CxPropertyRuleSeverity.ERROR },
            warnings = violations.count { it.severity == CxPropertyRuleSeverity.WARNING },
            truncated = violations.size > request.limit,
            violations = violations.take(request.limit).map { it.toDto() },
        )
    }

    fun listRules() = CxPropertyRulesDto(
        items = CxPropertyRule.entries.map { rule ->
            rule.toDto().copy(
                keys = rule.keys.sorted(),
                keyPrefixes = rule.keyPrefixes.sorted(),
            )
        }
    )

    /**
     * The project's property chain, refusing to answer rather than answering emptily while the indexes are not ready:
     * an empty chain would read as "the project declares nothing", which is a very different claim.
     */
    private suspend fun chain(): CxPropertyModel {
        if (DumbService.isDumb(project)) error("The project is still indexing, so its property files cannot be read yet. Try again once indexing has finished.")

        return smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }
            .takeIf { it.sources.isNotEmpty() }
            ?: error("No SAP Commerce property files were found in this project.")
    }

    private fun requirePositive(limit: Int) = require(limit > 0) { "'limit' must be greater than 0." }

    /** An open Properties tool window showing [connection] would otherwise keep the value it read before the write. */
    private fun refreshToolWindow(connection: HacConnectionSettingsState) = CxRemotePropertyStateService.getInstance(project)
        .takeIf { it.state(connection.uuid).initialized() }
        ?.fetch(connection)

    private suspend fun fetchRemote(
        connection: HacConnectionSettingsState,
        keyFilter: String?,
        valueFilter: String?,
        limit: Int,
    ): CxRemotePropertyStatePage = CxRemotePropertyClient.getInstance(project).fetch(
        connection = connection,
        pageSize = limit,
        keyFilter = keyFilter.orEmpty(),
        valueFilter = valueFilter.orEmpty(),
    ) ?: error("${connection.connectionName} returned no readable properties payload.")

    private fun report(
        key: String,
        property: CxProperty?,
        effectiveValue: String?,
        rule: CxPropertyRuleDto?,
    ) = CxPropertyReportDto(
        key = key,
        declared = property != null,
        effectiveValue = effectiveValue,
        rawValue = property?.rawValue?.takeIf { it != effectiveValue },
        declarations = property?.declarations.orEmpty().map { it.toDto(property) },
        rule = rule,
    )

    private fun CxPropertyDeclaration.toDto(property: CxProperty?) = CxPropertyDeclarationDto(
        value = value,
        file = source.presentableName,
        scope = source.scope.title,
        wins = this == property?.declaration,
        ignored = !source.active,
        extension = source.extension,
        path = source.path,
    )

    private fun CxPropertyViolation.toDto() = CxPropertyViolationDto(
        key = key,
        severity = severity.title,
        message = message,
        rule = rule.name,
        file = source.presentableName,
        documentationUrl = rule.documentationUrl,
        extension = source.extension,
        path = source.path,
    )

    private fun CxPropertyRule.toDto() = CxPropertyRuleDto(
        rule = name,
        title = title,
        severity = severity.title,
        reportedIn = fileNames.sorted(),
        documentationUrl = documentationUrl,
    )

    companion object {
        /**
         * A key filter narrows the instance to a handful of properties, so the cap is only there to stop a filter
         * which happens to match thousands from being answered in full.
         */
        private const val REMOTE_LOOKUP_LIMIT = 200

        /**
         * [runCatching] for suspend code: a failed lookup becomes part of the answer, while a cancelled tool call is
         * rethrown so it actually stops instead of being reported as a remote error.
         */
        private inline fun <T> catching(block: () -> T): Result<T> = try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

        suspend fun getInstance(): PropertiesMcpService = currentCoroutineContext().project.service()
    }
}
