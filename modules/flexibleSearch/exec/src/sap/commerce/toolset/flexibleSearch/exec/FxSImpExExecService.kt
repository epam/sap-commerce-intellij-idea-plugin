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

package sap.commerce.toolset.flexibleSearch.exec

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.impex.FkResolutionInfo
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExConverter
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExHeaderBuilder
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExParam
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryInfo
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

/**
 * Orchestrates the conversion of FlexibleSearch result rows into an ImpEx INSERT_UPDATE block.
 *
 * When enum or FK columns are present, issues follow-up queries via [FlexibleSearchExecClient]
 * to resolve raw PKs to their natural key strings, then delegates to [FxSImpExConverter].
 *
 * Progress of each follow-up query is shown in the IDE background progress indicator.
 * [isExporting] is set to `true` for the duration of any async resolution so callers
 * (e.g. toolbar actions) can disable themselves while the export is in flight.
 */
@Service(Service.Level.PROJECT)
class FxSImpExExecService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    /**
     * `true` while a follow-up resolution is in progress.
     * Read from BGT (action `update()`), written from the service coroutine.
     */
    @Volatile
    var isExporting: Boolean = false
        private set

    /**
     * Callback-based entry point for action handlers.
     *
     * If no enum or FK resolution is needed the ImpEx is built synchronously and [onComplete]
     * is called before this function returns.  Otherwise [isExporting] is set to `true`,
     * follow-up queries are issued concurrently (each with its own progress indicator), and
     * [onComplete] is called from the service coroutine once all resolution is done.
     */
    fun exportToImpEx(
        queryInfo: FxSQueryInfo,
        params: List<FxSImpExParam>,
        joinUniqueParams: List<FxSImpExParam>,
        rows: List<List<String>>,
        connection: HacConnectionSettingsState,
        onComplete: (String) -> Unit,
    ) {
        val enumSourceIndicesByType = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)
        val fkSourceIndicesByResolutionInfo = FxSImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(queryInfo, params)

        if (enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            onComplete(FxSImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, rows))
            return
        }

        isExporting = true
        coroutineScope.launch {
            try {
                onComplete(resolveAndBuild(queryInfo, params, joinUniqueParams, rows, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo))
            } finally {
                isExporting = false
            }
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly
     * and does not manage [isExporting].
     */
    suspend fun exportToImpEx(
        queryInfo: FxSQueryInfo,
        params: List<FxSImpExParam>,
        joinUniqueParams: List<FxSImpExParam>,
        rows: List<List<String>>,
        connection: HacConnectionSettingsState,
    ): String {
        val enumSourceIndicesByType = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)
        val fkSourceIndicesByResolutionInfo = FxSImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(queryInfo, params)

        if (enumSourceIndicesByType.isEmpty() && fkSourceIndicesByResolutionInfo.isEmpty()) {
            return FxSImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, rows)
        }

        return resolveAndBuild(queryInfo, params, joinUniqueParams, rows, connection, enumSourceIndicesByType, fkSourceIndicesByResolutionInfo)
    }

    /**
     * Issues all follow-up queries concurrently (each under its own background progress indicator),
     * resolves enum codes and FK natural keys, then builds the final ImpEx text.
     */
    private suspend fun resolveAndBuild(
        queryInfo: FxSQueryInfo,
        params: List<FxSImpExParam>,
        joinUniqueParams: List<FxSImpExParam>,
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
                val keyParts = row.drop(1).filter { it.isNotBlank() }
                if (keyParts.isEmpty()) return@mapNotNull null
                pk to keyParts.joinToString(":")
            }
            .toMap()

        val resolvedRows = FxSImpExHeaderBuilder.resolveEnumPks(rows, enumSourceIndicesByType.keys, pkToCode)
        val finalRows = FxSImpExHeaderBuilder.resolveFkPks(resolvedRows, fkSourceIndicesByResolutionInfo.keys, pkToNaturalKey)
        return FxSImpExConverter.buildImpEx(queryInfo.primaryType, params, joinUniqueParams, queryInfo, finalRows)
    }

    companion object {
        fun getInstance(project: Project): FxSImpExExecService = project.service()
    }
}
