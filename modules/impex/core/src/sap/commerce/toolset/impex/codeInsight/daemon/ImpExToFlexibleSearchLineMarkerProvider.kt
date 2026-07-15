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
import com.intellij.psi.PsiElement
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.util.Function
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
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

        return ImpExLineMarkerInfo(
            element.firstLeaf(),
            HybrisIcons.ImpEx.Actions.COPY_TO_FLEXIBLE_SEARCH
        )
    }

    /**
     * Builds a FlexibleSearch SELECT with JOINs, resolving qualifier columns
     * (e.g. restrictedType(code), principal(uid)) against their referenced type
     */
    fun ImpExValueLine.toUniqueSelectQuery(): String? {
        val header = headerLine ?: return null
        val rootType = header.fullHeaderType?.headerTypeName?.text ?: return null
        val project = header.project

        val ctx = JoinBuilder()
        val rootAlias = ctx.nextAlias()
        val rootMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(rootType)

        header.uniqueFullHeaderParameters.forEach { param ->
            val value = getValueGroup(param.columnNumber)?.resolveValue() ?: return@forEach
            addCondition(ctx, project, rootAlias, rootMeta, param, value)
        }

        if (ctx.conditions.isEmpty()) return null

        val selectColumns = (rootMeta?.selectableColumns() ?: listOf("pk"))
            .joinToString(", ") { "{$rootAlias.$it}" }

        val fromClause = buildString {
            append("$rootType AS $rootAlias")
            ctx.joins.forEach { append(" $it") }
        }
        return "SELECT $selectColumns FROM {$fromClause} WHERE " + ctx.conditions.joinToString(" AND ")
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
        ctx: JoinBuilder,
        project: com.intellij.openapi.project.Project,
        ownerAlias: String,
        ownerMeta: TSGlobalMetaItem?,
        param: ImpExFullHeaderParameter,
        rawValue: String
    ) {
        val attrName = param.anyHeaderParameterName.text.substringBefore('(')
        val qualifiers = param.parametersList.firstOrNull()?.parameterList.orEmpty()
        val attrType = ownerMeta?.allAttributes?.get(attrName)?.type

        if (qualifiers.isEmpty()) {
            ctx.conditions += "{$ownerAlias.$attrName} ${formatCondition(rawValue, attrType)}"
            return
        }

        val targetMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(attrType)
        if (attrType == null) {
            ctx.conditions += "{$ownerAlias.$attrName} ${formatCondition(rawValue, attrType)}"
            return
        }

        val alias = ctx.nextAlias()
        ctx.joins += "JOIN $attrType AS $alias ON {$alias.pk} = {$ownerAlias.$attrName}"

        val qualifierNames = qualifiers.map { it.text }
        if (qualifierNames.size == 1) {
            val q = qualifierNames.first()
            val qType = targetMeta?.allAttributes?.get(q)?.type
            ctx.conditions += "{$alias.$q} ${formatCondition(rawValue, qType)}"
        } else {
            val parts = rawValue.split(":")
            qualifierNames.forEachIndexed { i, q ->
                val part = parts.getOrNull(i) ?: return@forEachIndexed
                val qType = targetMeta?.allAttributes?.get(q)?.type
                ctx.conditions += "{$alias.$q} ${formatCondition(part, qType)}"
            }
        }
    }

    /** Produces "= <literal>" respecting the attribute's declared type. */
    private fun formatCondition(rawValue: String, type: String?): String {
        val value = rawValue.trim().removeSurrounding("\"")

        return when (type) {
            TSConstants.Primitive.BOOLEAN,
            TSConstants.Type.JAVA_BOOLEAN -> "= ${if (value.equals("true", true) || value == "1") "1" else "0"}"

            TSConstants.Type.JAVA_STRING -> "= '${value.replace("'", "''")}'"

            null -> "= '${value.replace("'", "''")}'" // unresolved type -> safe default: quote as string
            else -> "= $value" // numeric/enum/date/etc — used as-is, unquoted
        }
    }

    private class JoinBuilder {
        val joins = mutableListOf<String>()
        val conditions = mutableListOf<String>()
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