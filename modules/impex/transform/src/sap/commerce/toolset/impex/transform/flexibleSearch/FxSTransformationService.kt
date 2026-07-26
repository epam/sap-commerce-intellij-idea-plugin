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

package sap.commerce.toolset.impex.transform.flexibleSearch

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.impex.ImpExConstants.Transform
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.transform.ImpExUniqueParamsParser
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.handlers.CopyToClipboardTransformResultHandler
import sap.commerce.toolset.transform.handlers.CreateScratchFileTransformResultHandler
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.model.Cardinality
import sap.commerce.toolset.typeSystem.model.PersistenceType

@Service(Service.Level.PROJECT)
class FxSTransformationService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    fun transform(
        outputFileType: LanguageFileType,
        element: ImpExValueLine,
        onComplete: (TransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(outputFileType, element)
            onComplete(result)
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly.
     */
    suspend fun transform(outputFileType: LanguageFileType, element: ImpExValueLine): TransformationResult {
        val data = readAction { buildTransformData(element) }
            ?: error("cannot extract PSI/meta data")

        val content = "SELECT ${data.selectColumns} FROM {${data.fromClause}} WHERE ${data.whereClause}"

        val formattedText = edtWriteAction {
            FlexibleSearchElementFactory.createFile(data.project, content)
                .let { CodeStyleManager.getInstance(data.project).reformat(it) }
                .text
        }

        return TransformationResult(
            content = formattedText,
            description = "${data.rootType} to ${outputFileType.name}",
            handlers = listOf(
                CopyToClipboardTransformResultHandler(formattedText),
                CreateScratchFileTransformResultHandler(project, formattedText, outputFileType)
            )
        )
    }

    private fun buildTransformData(element: ImpExValueLine): TransformData? {
        val parsed = ImpExUniqueParamsParser.parse(element) ?: return null
        val ctx = parsed.ctx
        val rootType = parsed.rootType
        val project = parsed.project
        val includeAllAttributes = element.getUserData(Transform.INCLUDE_ALL_ATTRIBUTES) ?: false
        val rootMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(rootType)
        val header = element.headerLine ?: return null

        if (ctx.conditions.isEmpty()) return null

        val hasJoins = ctx.joins.isNotEmpty()

        val selectColumns = if (includeAllAttributes) {
            rootMeta?.selectableColumns() ?: listOf(TSConstants.Attribute.PK)
        } else {
            buildList {
                add(TSConstants.Attribute.PK)
                header.fullHeaderParameterList
                    .map { it.parametersContext.rootParameter.name }
                    .distinct()
                    .filter { it != TSConstants.Attribute.PK }
                    .forEach { add(it) }
            }
        }.joinToString(", ") { if (hasJoins) "{${ctx.rootAlias}.$it}" else "{$it}" }

        val fromClause = buildString {
            append(if (hasJoins) "$rootType AS ${ctx.rootAlias}" else rootType)
            ctx.joins.forEach { append(" JOIN ${it.type} AS ${it.alias} ON {${it.alias}.pk} = {${it.ownerAlias}.${it.ownerAttr}}") }
        }

        val whereClause = ctx.conditions.joinToString(" AND ") { c ->
            if (hasJoins) "{${c.alias}.${c.attribute}} ${c.predicate} \n"
            else "{${c.attribute}} ${c.predicate} \n"
        }

        return TransformData(
            project = project,
            rootType = rootType,
            selectColumns = selectColumns,
            fromClause = fromClause,
            whereClause = whereClause
        )
    }

    private fun TSGlobalMetaItem.selectableColumns(): List<String> = buildList {
        add(TSConstants.Attribute.PK)
        allAttributes.values
            .asSequence()
            .filter { it.persistence.type == PersistenceType.PROPERTY }
            .filterNot { it.isLocalized }
            .map { it.name }
            .distinct()
            .forEach { add(it) }
        allRelationEnds
            .asSequence()
            .filter { it.cardinality == Cardinality.ONE }
            .mapNotNull { it.qualifier }
            .distinct()
            .forEach { add(it) }
    }

    private data class TransformData(
        val project: Project,
        val rootType: String,
        val selectColumns: String,
        val fromClause: String,
        val whereClause: String
    )

    companion object {
        fun getInstance(project: Project): FxSTransformationService = project.service()
    }
}
