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

package sap.commerce.toolset.impex.transform.polyglotQuery

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.transform.ImpExUniqueParamsParser
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Condition
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Join
import sap.commerce.toolset.impex.transform.flexibleSearch.context.QueryContext
import sap.commerce.toolset.polyglotQuery.editor.PolyglotQuerySplitEditor
import sap.commerce.toolset.polyglotQuery.editor.PolyglotQueryVirtualParameter
import sap.commerce.toolset.polyglotQuery.psi.PolyglotElementFactory
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.handlers.CopyToClipboardTransformResultHandler
import sap.commerce.toolset.transform.handlers.CreateScratchFileTransformResultHandler
import java.lang.ref.WeakReference

@Service(Service.Level.PROJECT)
class PgQTransformationService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    fun transform(
        languageFileType: LanguageFileType,
        element: ImpExValueLine,
        onComplete: (TransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(languageFileType, element)
            onComplete(result)
        }
    }

    suspend fun transform(fileType: LanguageFileType, element: ImpExValueLine): TransformationResult {
        val data = readAction { buildTransformData(element) }
            ?: error("cannot extract PSI/meta data")

        val formattedText = edtWriteAction {
            PolyglotElementFactory.createFile(data.project, data.content)
                .let { CodeStyleManager.getInstance(data.project).reformat(it) }
                .text
        }

        val seedValues = resolveSeedValues(data)

        return TransformationResult(
            content = formattedText,
            description = "${data.rootType} to ${fileType.name}",
            handlers = listOf(
                CopyToClipboardTransformResultHandler(formattedText),
                CreateScratchFileTransformResultHandler(project, formattedText, fileType) {
                    this.asSafely<PolyglotQuerySplitEditor>()?.let { editor ->
                        if (seedValues.isNotEmpty()) {
                            editor.virtualParameters = seedValues.mapValues { (name, value) ->
                                PolyglotQueryVirtualParameter(
                                    name = name,
                                    project = WeakReference(project),
                                ).apply { rawValue = value }
                            }
                        }
                        editor.inEditorParameters = true
                    }
                }
            )
        )
    }

    private fun buildTransformData(element: ImpExValueLine): TransformData? {
        val parsed = ImpExUniqueParamsParser.parse(element) ?: return null
        val ctx = parsed.ctx
        val paramNameCounts = mutableMapOf<String, Int>()
        val paramNames = mutableMapOf<String, String>()  // attrName/ownerAttr → paramName
        val pgqConditions = mutableListOf<String>()

        ctx.conditions
            .filter { it.alias == ctx.rootAlias }
            .forEach { c ->
                val paramName = nextParamName(c.attribute, paramNameCounts)
                paramNames[c.attribute] = paramName
                pgqConditions += "{${c.attribute}}=?$paramName"
            }

        ctx.joins
            .filter { it.ownerAlias == ctx.rootAlias }
            .forEach { j ->
                val paramName = nextParamName(j.ownerAttr, paramNameCounts)
                paramNames[j.ownerAttr] = paramName
                pgqConditions += "{${j.ownerAttr}}=?$paramName"
            }

        if (pgqConditions.isEmpty()) return null

        return TransformData(
            project = parsed.project,
            rootType = parsed.rootType,
            content = "GET {${parsed.rootType}} WHERE ${pgqConditions.joinToString(" AND ")}",
            ctx = ctx,
            paramNames = paramNames,
        )
    }

    private fun nextParamName(attr: String, counts: MutableMap<String, Int>): String {
        val count = counts.merge(attr, 1, Int::plus)!!
        return if (count == 1) attr else "$attr$count"
    }

    private suspend fun resolveSeedValues(data: TransformData): Map<String, String?> {
        val result = mutableMapOf<String, String?>()
        val ctx = data.ctx

        // Direct root-alias conditions → raw cell value as seed
        ctx.conditions
            .filter { it.alias == ctx.rootAlias }
            .forEach { c ->
                val paramName = data.paramNames[c.attribute] ?: return@forEach
                result[paramName] = c.rawValue
            }

        // Root-level joins → resolve PK via recursive PgQ lookups
        val rootJoins = ctx.joins.filter { it.ownerAlias == ctx.rootAlias }
        if (rootJoins.isEmpty()) return result

        val connection = HacExecConnectionService.getInstance(project).activeConnection
        val execClient = FlexibleSearchExecClient.getInstance(project)
        val settings = FlexibleSearchExecContext.defaultSettings(connection)

        val conditionsByAlias = ctx.conditions.groupBy { it.alias }
        val subJoinsByOwnerAlias = ctx.joins.groupBy { it.ownerAlias }

        for (join in rootJoins) {
            val paramName = data.paramNames[join.ownerAttr] ?: continue
            try {
                val pk = resolveAliasPK(
                    alias = join.alias,
                    typeName = join.type,
                    conditionsByAlias = conditionsByAlias,
                    subJoinsByOwnerAlias = subJoinsByOwnerAlias,
                    execClient = execClient,
                    connection = connection,
                    settings = settings,
                )
                if (pk != null) result[paramName] = pk
            } catch (_: Exception) {
                // skip — leave parameter empty
            }
        }

        return result
    }

    /**
     * Recursively resolves the PK for a join alias by executing a PgQ GET query.
     *
     * PgQ supports only direct attribute lookups — no nested navigation. Resolution is
     * therefore bottom-up: sub-joins are resolved first, their PKs then used as unquoted
     * FK comparisons (`{attr}=<pk_long>`) alongside the leaf string conditions.
     */
    private suspend fun resolveAliasPK(
        alias: String,
        typeName: String,
        conditionsByAlias: Map<String, List<Condition>>,
        subJoinsByOwnerAlias: Map<String, List<Join>>,
        execClient: FlexibleSearchExecClient,
        connection: HacConnectionSettingsState,
        settings: FlexibleSearchExecContext.Settings,
    ): String? {
        val conditions = mutableListOf<String>()

        // Leaf string conditions on this alias (already formatted as "= 'value'")
        conditionsByAlias[alias]?.forEach { c ->
            conditions += "{${c.attribute}} ${c.predicate}"
        }

        // Sub-joins → recursively resolve their PKs, then compare as unquoted longs
        subJoinsByOwnerAlias[alias]?.forEach { subJoin ->
            val subPk = resolveAliasPK(
                alias = subJoin.alias,
                typeName = subJoin.type,
                conditionsByAlias = conditionsByAlias,
                subJoinsByOwnerAlias = subJoinsByOwnerAlias,
                execClient = execClient,
                connection = connection,
                settings = settings,
            ) ?: return null
            conditions += "{${subJoin.ownerAttr}}=$subPk"
        }

        if (conditions.isEmpty()) return null

        val query = "SELECT {pk} FROM {$typeName} WHERE ${conditions.joinToString(" AND ")}"
        return executeLookup(typeName, query, execClient, connection, settings)
    }

    private suspend fun executeLookup(
        typeName: String,
        query: String,
        execClient: FlexibleSearchExecClient,
        connection: HacConnectionSettingsState,
        settings: FlexibleSearchExecContext.Settings,
    ): String? {
        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            queryMode = QueryMode.FlexibleSearch,
            settings = settings,
        )
        val result = withBackgroundProgress(project, "Resolving PK for $typeName", true) {
            execClient.execute(context)
        }
        val headers = result.headers ?: return null
        val firstRow = result.rows?.firstOrNull() ?: return null
        val pkIndex = headers.indexOfFirst { it.equals("PK", ignoreCase = true) }
        return if (pkIndex >= 0) firstRow.getOrNull(pkIndex) else firstRow.firstOrNull()
    }

    private data class TransformData(
        val project: Project,
        val rootType: String,
        val content: String,
        val ctx: QueryContext,
        val paramNames: Map<String, String>,
    )

    companion object {
        fun getInstance(project: Project): PgQTransformationService = project.service()
    }
}
