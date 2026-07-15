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
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.util.Function
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueLine
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

    /**
     * Builds a FlexibleSearch SELECT with JOINs, resolving qualifier columns
     * (e.g. restrictedType(code), principal(uid)) against their referenced type
     */
    fun ImpExValueLine.toUniqueSelectQuery(): String? {
        val header = headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project

        val ctx = QueryContext()
        val rootAlias = ctx.nextAlias()
        val rootMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(rootType)

        header.uniqueFullHeaderParameters.forEach { param ->
            val value = getValueGroup(param.columnNumber)?.resolveValue() ?: return@forEach
            addCondition(ctx, project, rootAlias, rootMeta, param, value)
        }

        if (ctx.conditions.isEmpty()) return null

        val hasJoins = ctx.joins.isNotEmpty()

        val selectColumns = (rootMeta?.selectableColumns() ?: listOf("pk"))
            .joinToString(", ") { if (hasJoins) "{$rootAlias.$it}" else "{$it}" }

        val fromClause = buildString {
            append(if (hasJoins) "$rootType AS $rootAlias" else rootType)
            ctx.joins.forEach { append(" JOIN ${it.type} AS ${it.alias} ON {${it.alias}.pk} = {${it.ownerAlias}.${it.ownerAttr}}") }
        }

        val whereClause = ctx.conditions.joinToString(" AND ") { c ->
            if (hasJoins) "{${c.alias}.${c.attribute}} ${c.predicate}"
            else "{${c.attribute}} ${c.predicate}"
        }

        return """
            SELECT $selectColumns
            FROM {$fromClause}
            WHERE $whereClause
            """.trimIndent()
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

    private fun addCondition(
        ctx: QueryContext,
        project: Project,
        ownerAlias: String,
        ownerMeta: TSGlobalMetaItem?,
        param: ImpExFullHeaderParameter,
        rawValue: String
    ) {
        val attrName = param.anyHeaderParameterName.text.substringBefore('(')
        val qualifiers = param.parametersList.firstOrNull()?.parameterList.orEmpty()
        val attrType = ownerMeta?.allAttributes?.get(attrName)?.type

        if (qualifiers.isEmpty() || attrType == null) {
            ctx.conditions += Condition(ownerAlias, attrName, formatPredicate(rawValue, attrType))
            return
        }

        val targetMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(attrType)
        val joinAlias = ctx.nextAlias()
        ctx.joins += Join(attrType, joinAlias, ownerAlias, attrName)

        val qualifierNames = qualifiers.map { it.text }
        if (qualifierNames.size == 1) {
            val q = qualifierNames.first()
            ctx.conditions += Condition(joinAlias, q, formatPredicate(rawValue, targetMeta?.allAttributes?.get(q)?.type))
        } else {
            val parts = rawValue.split(":")
            qualifierNames.forEachIndexed { i, q ->
                val part = parts.getOrNull(i) ?: return@forEachIndexed
                ctx.conditions += Condition(joinAlias, q, formatPredicate(part, targetMeta?.allAttributes?.get(q)?.type))
            }
        }
    }

    /** Produces "= <literal>" respecting the attribute's declared type. */
    private fun formatPredicate(rawValue: String, type: String?): String {
        val value = rawValue.trim().removeSurrounding("\"")

        return when (type) {
            TSConstants.Primitive.BOOLEAN,
            TSConstants.Type.JAVA_BOOLEAN -> "= ${if (value.equals("true", true) || value == "1") "1" else "0"}"

            TSConstants.Type.JAVA_STRING -> "= '${value.replace("'", "''")}'"

            null -> "= '${value.replace("'", "''")}'" // unresolved type -> safe default: quote as string
            else -> "= $value" // numeric/enum/date/etc — used as-is, unquoted
        }
    }

    private data class Condition(val alias: String, val attribute: String, val predicate: String)
    private data class Join(val type: String, val alias: String, val ownerAlias: String, val ownerAttr: String)

    private class QueryContext {
        val joins = mutableListOf<Join>()
        val conditions = mutableListOf<Condition>()
        var counter = 0
        fun nextAlias() = "t${counter++}"
    }

    private fun handler(leaf: PsiElement?) {
        val element = leaf?.parentOfType<ImpExValueLine>() ?: return
        val project = element.project

        val flexibleSearchStatement = element.toUniqueSelectQuery() ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(flexibleSearchStatement))

        Notifications.create(
            NotificationType.INFORMATION,
            "FlexibleSearch copied to Clipboard",
            flexibleSearchStatement
        )
            .hideAfter(10)
            .addAction("Open as a Scratch File") { _, _ -> createScratchFile(project, flexibleSearchStatement, HybrisConstants.Languages.FlexibleSearch.EXTENSION) }
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