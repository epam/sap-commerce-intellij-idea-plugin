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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.CodeStyleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.ImpExConstants.Transform
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.impl.ImpExFullHeaderParameterMixin
import sap.commerce.toolset.impex.transform.context.ImpExTransformationResult
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Condition
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Join
import sap.commerce.toolset.impex.transform.flexibleSearch.context.QueryContext
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
        fileType: LanguageFileType,
        element: ImpExValueLine,
        onComplete: (ImpExTransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(fileType, element)
            onComplete(result)
        }
    }

    /**
     * Suspend overload for coroutine callers (e.g. MCP tools).
     *
     * Behaves identically to the callback overload but returns the ImpEx string directly.
     */
    suspend fun transform(fileType: LanguageFileType, element: ImpExValueLine): ImpExTransformationResult {
        val data = readAction { buildTransformData(element) }
            ?: error("cannot extract PSI/meta data")

        val content = "SELECT ${data.selectColumns} FROM {${data.fromClause}} WHERE ${data.whereClause}"

        val formattedText = edtWriteAction {
            FlexibleSearchElementFactory.createFile(data.project, content)
                .let { CodeStyleManager.getInstance(data.project).reformat(it) }
                .text
        }

        return ImpExTransformationResult(
            languageName = fileType.name,
            content = formattedText,
            exportType = data.rootType
        )
    }

    private fun buildTransformData(element: ImpExValueLine): TransformData? {
        val header = element.headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project
        val includeAllAttributes = element.getUserData(Transform.INCLUDE_ALL_ATTRIBUTES) ?: false

        val ctx = QueryContext()
        val rootMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(rootType)

        header.uniqueFullHeaderParameters.forEach { param ->
            val pathDelimiter = param.getAttributeValue(AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)
            val valueGroup = element.getValueGroup(param.columnNumber) ?: return@forEach
            val resolvedValue = valueGroup.resolveValue() ?: return@forEach
            val parametersContext = param.parametersContext

            if (parametersContext.subParameters == null) {
                val rootParameter = parametersContext.rootParameter
                val rootMetaContext = rootParameter.metaContext ?: return@forEach
                ctx.conditions += Condition(
                    alias = ctx.rootAlias,
                    attribute = rootParameter.name,
                    predicate = formatPredicate(resolvedValue, rootMetaContext.attributeType)
                )
            } else {
                val rootParameter = parametersContext.rootParameter
                val rootMetaContext = rootParameter.metaContext ?: return@forEach
                val joinAlias = ctx.nextAlias()
                ctx.joins += Join(
                    type = rootMetaContext.attributeType,
                    alias = joinAlias,
                    ownerAlias = ctx.rootAlias,
                    ownerAttr = parametersContext.rootParameter.name
                )

                // An empty cell resolves to the joined defaults of only those leaves that declare
                // one (see resolveDefaultValue) — distributing that string positionally would
                // misalign segments onto the wrong leaves. Pass no positional values instead and
                // let every leaf pull its own default in place.
                val splitValues = if (valueGroup.value == null) mutableListOf()
                else resolvedValue.split(pathDelimiter).toMutableList()

                parametersContext.subParameters?.forEach { subParameter ->
                    processSubParameter(subParameter, ctx, joinAlias, parametersContext, splitValues)
                }
            }
        }

        if (ctx.conditions.isEmpty()) return null

        val hasJoins = ctx.joins.isNotEmpty()

        val selectColumns = if (includeAllAttributes) {
            rootMeta?.selectableColumns() ?: listOf("pk")
        } else {
            buildList {
                add("pk")
                header.fullHeaderParameterList
                    .map { it.parametersContext.rootParameter.name }
                    .distinct()
                    .filter { it != "pk" }
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

    private fun processSubParameter(
        subParameter: ImpExFullHeaderParameterMixin.ParametersContext.Parameter,
        ctx: QueryContext,
        ownerAlias: String,
        parametersContext: ImpExFullHeaderParameterMixin.ParametersContext,
        rawValues: MutableList<String>
    ) {
        val subParameters = subParameter.subParameters
        if (subParameters != null) {
            val metaContext = subParameter.metaContext ?: return
            val ownerAttr = metaContext.meta.name ?: return

            val nextAlias = ctx.nextAlias()
            ctx.joins += Join(
                type = metaContext.attributeType,
                alias = nextAlias,
                ownerAlias = ownerAlias,
                ownerAttr = ownerAttr
            )

            subParameters.forEach {
                processSubParameter(it, ctx, nextAlias, parametersContext, rawValues)
            }
        } else {
            val metaContext = subParameter.metaContext ?: return
            val subParameterValue = resolveLeafValue(
                positional = rawValues.removeFirstOrNull(),
                default = subParameter.getAttributeValue(AttributeModifier.DEFAULT, "")
            )

            ctx.conditions += Condition(
                alias = ownerAlias,
                attribute = subParameter.name,
                predicate = formatPredicate(subParameterValue, metaContext.attributeType)
            )
        }
    }

    /**
     * Resolves a nested leaf parameter's value.
     *
     * The single cell value of a nested unique column is distributed positionally across its leaf
     * sub-parameters. When a leaf has no positional value left (e.g. only `code` is supplied for
     * `baseProduct(code, catalogversion(catalog(id[default=$cat]),version[default='Staged']))`)
     * or its segment is blank (`26002000::Staged`), fall back to its `[default=...]` modifier —
     * matching ImpEx semantics where an empty value triggers the default — before the `?` sentinel.
     *
     * The default's macros are already expanded (see [ImpExFullHeaderParameterMixin] value resolution);
     * surrounding single quotes are stripped the same way value groups unquote raw values.
     */
    internal fun resolveLeafValue(positional: String?, default: String): String = positional
        ?.takeIf { it.isNotBlank() }
        ?: default
            .takeIf { it.isNotEmpty() }
            ?.let { StringUtil.unquoteString(it, '\'') }
        ?: "?"

    private fun TSGlobalMetaItem.selectableColumns(): List<String> = buildList {
        add("pk")
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

    /** Produces "= <literal>" respecting the attribute's declared type. */
    fun formatPredicate(resolvedValue: String, type: String?): String {
        val value = resolvedValue.trim().removeSurrounding("\"")

        return when (type) {
            TSConstants.Primitive.BOOLEAN,
            TSConstants.Type.JAVA_BOOLEAN -> "= ${if (value.equals("true", true) || value == "1") "1" else "0"}"

            TSConstants.Type.JAVA_STRING -> "= '${value.replace("'", "''")}'"

            null -> "= '${value.replace("'", "''")}'" // unresolved type → safe default: quote as string
            else -> "= $value" // numeric/enum/date/etc — used as-is, unquoted
        }
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
