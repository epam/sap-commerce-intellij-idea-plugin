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
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.transform.context.FkResolutionInfo
import sap.commerce.toolset.flexibleSearch.transform.context.FxSQueryInfo
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

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
        context: FxSTransformationContext,
        onComplete: (String) -> Unit,
    ) {
        val queryInfo = context.queryInfo
        val params = context.params
        val joinUniqueParams = context.joinUniqueParams
        val rows = context.rows

        val connection = context.connection ?: HacExecConnectionService.getInstance(project).activeConnection
        val enumSourceIndicesByType = ImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)
        val fkSourceIndicesByResolutionInfo = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(queryInfo, params)

        if (rows.isEmpty() || enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            onComplete(ImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, rows))
            return
        }

        coroutineScope.launch {
            onComplete(resolveAndBuild(queryInfo, params, joinUniqueParams, rows, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo))
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly.
     */
    suspend fun transform(context: FxSTransformationContext): String {
        val queryInfo = context.queryInfo
        val params = context.params
        val joinUniqueParams = context.joinUniqueParams
        val rows = context.rows

        val connection = context.connection ?: HacExecConnectionService.getInstance(project).activeConnection
        val enumSourceIndicesByType = ImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)
        val fkSourceIndicesByResolutionInfo = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(queryInfo, params)

        if (rows.isEmpty() || enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            return ImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, rows)
        }

        return resolveAndBuild(queryInfo, params, joinUniqueParams, rows, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo)
    }

    /**
     * Issues all follow-up queries concurrently (each under its own background progress indicator),
     * resolves enum codes and FK natural keys, then builds the final ImpEx text.
     */
    private suspend fun resolveAndBuild(
        queryInfo: FxSQueryInfo,
        params: List<ImpExParam>,
        joinUniqueParams: List<ImpExParam>,
        rows: List<List<String>>,
        connection: HacConnectionSettingsState,
        enumSourceIndicesByType: Map<Int, String>,
        fkSourceIndicesByResolutionInfo: Map<Int, FkResolutionInfo>,
    ): String {
        val enumContextsWithLabel = enumSourceIndicesByType.values.distinct().map { enumType ->
            "Getting values for $enumType" to FlexibleSearchExecContext(
                connection = connection,
                content = "SELECT {pk}, {code} FROM {$enumType}",
                queryMode = QueryMode.FlexibleSearch,
                maxCount = 10_000,
                locale = FlexibleSearchExecConstants.Defaults.LOCALE,
                dataSource = FlexibleSearchExecConstants.Defaults.DATA_SOURCE,
                user = null,
                timeout = connection.timeout,
            )
        }
        val fkContextsWithLabel = fkSourceIndicesByResolutionInfo.values
            .distinctBy { it.fxsLookupQuery }
            .map { fkInfo ->
                "Getting natural key for ${fkInfo.typeName}" to FlexibleSearchExecContext(
                    connection = connection,
                    content = fkInfo.fxsLookupQuery,
                    queryMode = QueryMode.FlexibleSearch,
                    maxCount = 10_000,
                    locale = FlexibleSearchExecConstants.Defaults.LOCALE,
                    dataSource = FlexibleSearchExecConstants.Defaults.DATA_SOURCE,
                    user = null,
                    timeout = connection.timeout,
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

        val resolvedRows = ImpExHeaderBuilder.resolveEnumPks(rows, enumSourceIndicesByType.keys, pkToCode)
        val finalRows = ImpExHeaderBuilder.resolveFkPks(resolvedRows, fkSourceIndicesByResolutionInfo.keys, pkToNaturalKey)
        return ImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, finalRows)
    }

    companion object {
        fun getInstance(project: Project): ImpExTransformationService = project.service()
    }
}