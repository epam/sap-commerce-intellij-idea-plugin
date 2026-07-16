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

package sap.commerce.toolset.impex.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.util.Function
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.impl.ImpExFullHeaderParameterMixin
import sap.commerce.toolset.scratch.createScratchFile
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.model.PersistenceType
import java.awt.datatransfer.StringSelection
import java.util.function.Supplier
import javax.swing.Icon

class ImpExToFlexibleSearchLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (DumbService.isDumb(element.project)) return null
        if (element !is ImpExValueLine) return null
        if (element.headerLine?.uniqueFullHeaderParameters?.any { it.hasDocIdQualifier() } == true) return null

        return ImpExLineMarkerInfo(
            element.firstLeaf(),
            HybrisIcons.ImpEx.Actions.COPY_TO_FLEXIBLE_SEARCH
        )
    }

    private fun ImpExFullHeaderParameter.hasDocIdQualifier(): Boolean = parametersList
        .firstOrNull()
        ?.parameterList
        ?.takeIf { it.size == 1 }
        ?.firstOrNull()
        ?.childrenOfType<ImpExDocumentIdUsage>()
        ?.firstOrNull() != null

    fun ImpExValueLine.toUniqueSelectQuery(): String? {
        val header = headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project

        val ctx = QueryContext()
        val rootMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(rootType)

        header.uniqueFullHeaderParameters.forEach { param ->
            val pathDelimiter = param.getAttributeValue(AttributeModifier.PATH_DELIMITER, ImpExConstants.PATH_DELIMITER)
            val resolvedValue = getValueGroup(param.columnNumber)?.resolveValue() ?: return@forEach
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

                val splitValues = resolvedValue.split(pathDelimiter)
                    .toMutableList()

                parametersContext.subParameters.forEach { subParameter ->
                    processSubParameter(subParameter, ctx, joinAlias, parametersContext, splitValues)
                }
            }
        }

        if (ctx.conditions.isEmpty()) return null

        val hasJoins = ctx.joins.isNotEmpty()

        val selectColumns = (rootMeta?.selectableColumns() ?: listOf("pk"))
            .joinToString(", ") { if (hasJoins) "{${ctx.rootAlias}.$it}" else "{$it}" }

        val fromClause = buildString {
            append(if (hasJoins) "$rootType AS ${ctx.rootAlias}" else rootType)
            ctx.joins.forEach { append(" JOIN ${it.type} AS ${it.alias} ON {${it.alias}.pk} = {${it.ownerAlias}.${it.ownerAttr}}") }
        }

        val whereClause = ctx.conditions.joinToString(" AND ") { c ->
            if (hasJoins) "{${c.alias}.${c.attribute}} ${c.predicate} \n"
            else "{${c.attribute}} ${c.predicate} \n"
        }

        return """
            SELECT $selectColumns
            FROM {$fromClause}
            WHERE $whereClause
            """.trimIndent()
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
            val subParameterValue = rawValues.removeFirstOrNull() ?: "?"

            ctx.conditions += Condition(
                alias = ownerAlias,
                attribute = subParameter.name,
                predicate = formatPredicate(subParameterValue, metaContext.attributeType)
            )
        }
    }

    private fun TSGlobalMetaItem.selectableColumns(): List<String> = buildList {
        add("pk")
        allAttributes.values
            .asSequence()
            .filter { it.persistence.type == PersistenceType.PROPERTY }
            .filterNot { it.isLocalized }
            .map { it.name }
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

    private data class Condition(val alias: String, val attribute: String, val predicate: String)
    private data class Join(val type: String, val alias: String, val ownerAlias: String, val ownerAttr: String)

    private class QueryContext {
        val rootAlias = "t"
        val joins = mutableListOf<Join>()
        val conditions = mutableListOf<Condition>()
        var counter = 0
        fun nextAlias() = "t${counter++}"
    }

    private fun handler(leaf: PsiElement?) {
        val element = leaf?.parentOfType<ImpExValueLine>() ?: return
        val project = element.project

        val flexibleSearchStatement = element.toUniqueSelectQuery() ?: return
        val fileType = FileTypeRegistry.getInstance().getFileTypeByExtension(HybrisConstants.Languages.FlexibleSearch.EXTENSION)
        val formattedStatement = PsiFileFactory.getInstance(project).createFileFromText(
            "dummy", fileType, flexibleSearchStatement
        )
            .let { CodeStyleManager.getInstance(project).reformat(it) }
            .text

        CopyPasteManager.getInstance().setContents(StringSelection(formattedStatement))

        Notifications.create(
            NotificationType.INFORMATION,
            "FlexibleSearch copied to Clipboard",
            formattedStatement
        )
            .hideAfter(10)
            .addAction("Open as a Scratch File") { _, _ -> createScratchFile(project, formattedStatement, HybrisConstants.Languages.FlexibleSearch.EXTENSION) }
            .notify(project)
    }

    private inner class ImpExLineMarkerInfo(
        leaf: PsiElement,
        icon: Icon,
    ) : MergeableLineMarkerInfo<PsiElement?>(
        leaf, leaf.textRange, icon,
        Function { "Copy as FlexibleSearch" },
        { _, e -> handler(e) },
        GutterIconRenderer.Alignment.CENTER,
        Supplier { "Copy as FlexibleSearch" }
    ) {
        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is ImpExLineMarkerInfo && info.icon === icon
    }
}
