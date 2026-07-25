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

package sap.commerce.toolset.impex.transform

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.impl.ImpExFullHeaderParameterMixin
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Condition
import sap.commerce.toolset.impex.transform.flexibleSearch.context.Join
import sap.commerce.toolset.impex.transform.flexibleSearch.context.QueryContext
import sap.commerce.toolset.typeSystem.TSConstants

/**
 * Parses the unique header parameters of an ImpEx value line into a [QueryContext]
 * containing joins and leaf conditions with formatted predicates.
 *
 * This is the shared parsing layer consumed by both the FlexibleSearch and PolyglotQuery
 * output formats. Callers apply their own output-specific logic on the resulting context.
 */
internal object ImpExUniqueParamsParser {

    data class ParseResult(
        val rootType: String,
        val project: Project,
        val ctx: QueryContext,
    )

    /**
     * Parses unique header parameters into a [QueryContext] with joins and conditions.
     *
     * An empty cell value falls back to sub-parameter `[default=...]` modifiers, matching
     * ImpEx semantics. Returns null when no parseable parameters could be extracted.
     */
    fun parse(element: ImpExValueLine): ParseResult? {
        val header = element.headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project
        val ctx = QueryContext()

        header.uniqueFullHeaderParameters.forEach { param ->
            val pathDelimiter = param.getAttributeValue(AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)
            val valueGroup = element.getValueGroup(param.columnNumber) ?: return@forEach
            val parametersContext = param.parametersContext

            if (parametersContext.subParameters == null) {
                val resolvedValue = valueGroup.resolveValue() ?: return@forEach
                val rootParameter = parametersContext.rootParameter
                val rootMetaContext = rootParameter.metaContext ?: return@forEach
                ctx.conditions += Condition(
                    alias = ctx.rootAlias,
                    attribute = rootParameter.name,
                    predicate = formatPredicate(resolvedValue, rootMetaContext.attributeType),
                    rawValue = resolvedValue,
                )
            } else {
                val resolvedValue = valueGroup.resolveValue()
                val rootParameter = parametersContext.rootParameter
                val rootMetaContext = rootParameter.metaContext ?: return@forEach
                val joinAlias = ctx.nextAlias()
                ctx.joins += Join(
                    type = rootMetaContext.attributeType,
                    alias = joinAlias,
                    ownerAlias = ctx.rootAlias,
                    ownerAttr = rootParameter.name,
                )

                // An empty cell resolves to the joined defaults of only those leaves that declare
                // one — distributing that string positionally would misalign segments onto the wrong
                // leaves. Pass no positional values instead and let every leaf pull its own default.
                val splitValues = if (resolvedValue == null || valueGroup.value == null) mutableListOf()
                else resolvedValue.split(pathDelimiter).toMutableList()

                parametersContext.subParameters!!.forEach { subParameter ->
                    processSubParameter(subParameter, ctx, joinAlias, splitValues)
                }
            }
        }

        if (ctx.conditions.isEmpty() && ctx.joins.isEmpty()) return null
        return ParseResult(rootType = rootType, project = project, ctx = ctx)
    }

    internal fun processSubParameter(
        subParameter: ImpExFullHeaderParameterMixin.ParametersContext.Parameter,
        ctx: QueryContext,
        ownerAlias: String,
        rawValues: MutableList<String>,
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
                ownerAttr = ownerAttr,
            )

            subParameters.forEach {
                processSubParameter(it, ctx, nextAlias, rawValues)
            }
        } else {
            val metaContext = subParameter.metaContext ?: return
            val subParameterValue = resolveLeafValue(
                positional = rawValues.removeFirstOrNull(),
                default = subParameter.getAttributeValue(AttributeModifier.DEFAULT, ""),
            )

            ctx.conditions += Condition(
                alias = ownerAlias,
                attribute = subParameter.name,
                predicate = formatPredicate(subParameterValue, metaContext.attributeType),
            )
        }
    }

    /**
     * Resolves a nested leaf parameter's value.
     *
     * Uses the positional value from the split cell if present and non-blank,
     * falls back to the `[default=...]` modifier (unquoted), then returns `"?"`.
     */
    internal fun resolveLeafValue(positional: String?, default: String): String = positional
        ?.takeIf { it.isNotBlank() }
        ?: default
            .takeIf { it.isNotEmpty() }
            ?.let { StringUtil.unquoteString(it, '\'') }
        ?: "?"

    /** Produces `"= <literal>"` respecting the attribute's declared SAP type. */
    internal fun formatPredicate(resolvedValue: String, type: String?): String {
        val value = resolvedValue.trim().removeSurrounding("\"")

        return when (type) {
            TSConstants.Primitive.BOOLEAN,
            TSConstants.Type.JAVA_BOOLEAN -> "= ${if (value.equals("true", true) || value == "1") "1" else "0"}"

            TSConstants.Type.JAVA_STRING -> "= '${value.replace("'", "''")}'"

            null -> "= '${value.replace("'", "''")}'"
            else -> "= $value"
        }
    }
}
