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

package sap.commerce.toolset.flexibleSearch.transform.impex

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationContext
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationRequest
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.typeSystem.TSConstants

/**
 * Orchestrates the conversion of FlexibleSearch result rows into an ImpEx INSERT_UPDATE block.
 *
 * When enum or FK columns are present, issues follow-up queries via [sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient]
 * to resolve raw PKs to their natural key strings, then delegates to [ImpExConverter].
 *
 * Progress of each follow-up query is shown in the IDE background progress indicator.
 * [isExporting] is set to `true` for the duration of any async resolution so callers
 * (e.g. toolbar actions) can disable themselves while the export is in flight.
 */
@Service(Service.Level.PROJECT)
internal class ImpExTransformationService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    /**
     * Callback-based entry point for action handlers.
     *
     * If no enum or FK resolution is needed the ImpEx is built synchronously and [onComplete]
     * is called before this function returns.
     */
    fun transform(
        request: FxSTransformationRequest,
        onComplete: (String) -> Unit,
    ) {
        val connection = request.connection ?: HacExecConnectionService.getInstance(project).activeConnection
        val enumSourceIndicesByType = ImpExHeaderBuilder.enumSourceIndicesByType(request)
        val fkSourceIndicesByResolutionInfo = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(request)

        if (request.rows.isEmpty() || enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            onComplete(ImpExConverter.buildImpEx(request))
            return
        }

        val context = FxSTransformationContext(request, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo)
        coroutineScope.launch {
            val impexContent = resolveAndBuild(context)
            onComplete(impexContent)
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly.
     */
    suspend fun transform(request: FxSTransformationRequest): String {
        val connection = request.connection ?: HacExecConnectionService.getInstance(project).activeConnection
        val enumSourceIndicesByType = ImpExHeaderBuilder.enumSourceIndicesByType(request)
        val fkSourceIndicesByResolutionInfo = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(request)

        if (request.rows.isEmpty() || enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            return ImpExConverter.buildImpEx(request)
        }

        val context = FxSTransformationContext(request, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo)
        return resolveAndBuild(context)
    }

    /**
     * Issues all follow-up queries concurrently (each under its own background progress indicator),
     * resolves enum codes and FK natural keys, then builds the final ImpEx text.
     */
    private suspend fun resolveAndBuild(context: FxSTransformationContext): String {
        val enumContextsWithLabel = context.enumSourceIndicesByType.values.distinct().map { enumType ->
            "Getting values for $enumType" to FlexibleSearchExecContext(
                connection = context.connection,
                content = "SELECT {${TSConstants.Attribute.PK}}, {${TSConstants.Attribute.CODE}} FROM {$enumType}",
                queryMode = QueryMode.FlexibleSearch,
                settings = context.request.execSettings,
            )
        }
        val fkContextsWithLabel = context.fkSourceIndicesByResolutionInfo.values
            .distinctBy { it.fxsLookupQuery }
            .map { fkInfo ->
                "Getting natural key for ${fkInfo.typeName}" to FlexibleSearchExecContext(
                    connection = context.connection,
                    content = fkInfo.fxsLookupQuery,
                    queryMode = QueryMode.FlexibleSearch,
                    settings = context.request.execSettings,
                )
            }

        val allLabeledContexts = enumContextsWithLabel + fkContextsWithLabel

        val client = FlexibleSearchExecClient.getInstance(project)
        val allResults = supervisorScope {
            allLabeledContexts.map { (label, ctx) ->
                async { withBackgroundProgress(project, label, true) { client.execute(ctx) } }
            }.awaitAll()
        }

        val enumResults = allResults.take(enumContextsWithLabel.size)
        val fkResults = allResults.drop(enumContextsWithLabel.size)

        val pkToCode = enumResults
            .flatMap { r -> r.rows ?: emptyList() }
            .mapNotNull { row ->
                val pk = row.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val code = row.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                pk to code
            }
            .toMap()
        val pkToNaturalKey = fkResults
            .flatMap { r -> r.rows ?: emptyList() }
            .mapNotNull { row ->
                val pk = row.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

                @Suppress("SENSELESS_COMPARISON")
                val keyParts = row.drop(1).filter { it != null && it.isNotBlank() }
                if (keyParts.isEmpty()) return@mapNotNull null
                pk to keyParts.joinToString(":")
            }
            .toMap()

        val resolvedRows = ImpExHeaderBuilder.resolveEnumPks(context.request.rows, context.enumSourceIndicesByType.keys, pkToCode)
        val finalRows = ImpExHeaderBuilder.resolveFkPks(resolvedRows, context.fkSourceIndicesByResolutionInfo.keys, pkToNaturalKey)
        return ImpExConverter.buildImpEx(context.request.copy(rows = finalRows))
    }

    companion object {
        fun getInstance(project: Project): ImpExTransformationService = project.service()
    }
}