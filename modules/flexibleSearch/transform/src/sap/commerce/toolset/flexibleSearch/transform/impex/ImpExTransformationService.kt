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
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.flexibleSearch.transform.FxSQueryAnalyzer
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationResult
import sap.commerce.toolset.flexibleSearch.transform.impex.context.ImpExTransformationContext
import sap.commerce.toolset.flexibleSearch.transform.impex.context.ImpExTransformationDescriptor
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.typeSystem.TSConstants

/**
 * Orchestrates the conversion of FlexibleSearch result rows into an ImpEx INSERT_UPDATE block.
 *
 * When enum or FK columns are present, issues follow-up queries via [sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient]
 * to resolve raw PKs to their natural key strings, then delegates to [ImpExConverter].
 *
 * Progress of each follow-up query is shown in the IDE background progress indicator.
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
        transformerName: String,
        psiFile: FlexibleSearchPsiFile,
        onComplete: (FxSTransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(transformerName, psiFile)
            onComplete(result)
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly.
     */
    suspend fun transform(transformerName: String, psiFile: FlexibleSearchPsiFile): FxSTransformationResult {
        val descriptor = psiFile.transformationDescriptor()
        val connection = descriptor.connection
            ?: HacExecConnectionService.getInstance(project).activeConnection
        val enumSourceIndicesByType = ImpExHeaderBuilder.enumSourceIndicesByType(descriptor)
        val fkSourceIndicesByResolutionInfo = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(descriptor)

        if (descriptor.rows.isEmpty() || enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            val content = ImpExConverter.buildImpEx(descriptor)

            return FxSTransformationResult(
                transformerName = transformerName,
                content = content,
                exportType = descriptor.typeName,
                exportRows = descriptor.rows,
            )
        }

        val context = ImpExTransformationContext(descriptor, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo)
        val content = resolveAndBuild(context)

        return FxSTransformationResult(
            transformerName = transformerName,
            content = content,
            exportType = descriptor.typeName,
            exportRows = descriptor.rows,
        )
    }

    /**
     * Issues all follow-up queries concurrently (each under its own background progress indicator),
     * resolves enum codes and FK natural keys, then builds the final ImpEx text.
     */
    private suspend fun resolveAndBuild(context: ImpExTransformationContext): String {
        val enumContextsWithLabel = context.enumSourceIndicesByType.values.distinct().map { enumType ->
            "Getting values for $enumType" to FlexibleSearchExecContext(
                connection = context.connection,
                content = "SELECT {${TSConstants.Attribute.PK}}, {${TSConstants.Attribute.CODE}} FROM {$enumType}",
                queryMode = QueryMode.FlexibleSearch,
                settings = context.descriptor.execSettings,
            )
        }
        val fkContextsWithLabel = context.fkSourceIndicesByResolutionInfo.values
            .distinctBy { it.fxsLookupQuery }
            .map { fkInfo ->
                "Getting natural key for ${fkInfo.typeName}" to FlexibleSearchExecContext(
                    connection = context.connection,
                    content = fkInfo.fxsLookupQuery,
                    queryMode = QueryMode.FlexibleSearch,
                    settings = context.descriptor.execSettings,
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

        val resolvedRows = ImpExHeaderBuilder.resolveEnumPks(context.descriptor.rows, context.enumSourceIndicesByType.keys, pkToCode)
        val finalRows = ImpExHeaderBuilder.resolveFkPks(resolvedRows, context.fkSourceIndicesByResolutionInfo.keys, pkToNaturalKey)
        return ImpExConverter.buildImpEx(context.descriptor.copy(rows = finalRows))
    }


    private suspend fun FlexibleSearchPsiFile.transformationDescriptor(): ImpExTransformationDescriptor {
        val project = this.project
        val includeTypeSystemUnique = getUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE) ?: false
        val includeData = getUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA) ?: false
        val connection = getUserData(FlexibleSearchExecConstants.Transform.CONNECTION)
        val execSettings = getUserData(FlexibleSearchExecConstants.Transform.EXEC_SETTINGS)
            ?: FlexibleSearchExecContext.defaultSettings(connection)
        val execResult = getUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS)

        val headers = execResult?.headers ?: emptyList()
        val rows = execResult?.rows ?: emptyList()
        val baseQueryInfo = FxSQueryAnalyzer.analyze(this, headers)

        val queryInfo = if (includeTypeSystemUnique) {
            val tsUniqueAttrs = ImpExHeaderBuilder.typeSystemUniqueAttributeNames(baseQueryInfo.primaryType, project)
            baseQueryInfo.copy(uniqueAttributeNames = baseQueryInfo.uniqueAttributeNames + tsUniqueAttrs)
        } else {
            baseQueryInfo
        }

        val params = ImpExHeaderBuilder.buildParams(queryInfo, project)
        val joinUniqueParams = ImpExHeaderBuilder.buildJoinUniqueParams(queryInfo, project)
        val exportRows = if (includeData) rows else emptyList()

        return ImpExTransformationDescriptor(
            project = project,
            queryInfo = queryInfo,
            params = params,
            joinUniqueParams = joinUniqueParams,
            rows = exportRows,
            connection = connection,
            execSettings = execSettings,
        )
    }

    companion object {
        fun getInstance(project: Project): ImpExTransformationService = project.service()
    }
}