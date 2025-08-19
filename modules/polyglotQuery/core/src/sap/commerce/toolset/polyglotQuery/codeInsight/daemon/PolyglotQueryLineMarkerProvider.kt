/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.polyglotQuery.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.codeStyle.CodeStyleManager
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.polyglotQuery.PolyglotQueryUtils
import sap.commerce.toolset.polyglotQuery.psi.PolyglotElementFactory
import java.awt.datatransfer.StringSelection
import java.util.function.Supplier
import javax.swing.Icon

class PolyglotQueryLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName() = i18n("hybris.editor.gutter.pgq.name")
    override fun getIcon(): Icon = HybrisIcons.PolyglotQuery.FILE

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.project.isNotHybrisProject) return null

        return when (element) {
            is PsiPolyadicExpression -> process(element) { PolyglotQueryUtils.computeExpression(element) }
            is PsiLiteralExpression -> process(element) { PolyglotQueryUtils.computeExpression(element) }
            else -> null
        }
    }

    private fun process(
        element: PsiElement,
        expressionProvider: () -> String
    ): PolyglotQueryLineMarkerInfo? {
        val parent = element.parent
        if (parent !is PsiVariable || parent.nameIdentifier == null) return null
        if (element.project.isNotHybrisProject) return null

        val expression = expressionProvider.invoke()
        if (!PolyglotQueryUtils.isPolyglotQuery(expression)) return null

        return PolyglotQueryLineMarkerInfo(parent.nameIdentifier!!, icon, expression)
    }

    private fun copyToClipboard(e: PsiElement?, expression: String) {
        val project = e?.project ?: return

        val formattedExpression = PolyglotElementFactory.createFile(project, expression)
            .let { CodeStyleManager.getInstance(project).reformat(it) }
            .text

        CopyPasteManager.getInstance().setContents(StringSelection(formattedExpression))

        Notifications.create(NotificationType.INFORMATION, i18n("hybris.editor.gutter.pgq.notification.title"), formattedExpression)
            .hideAfter(10)
            .notify(project)
    }

    private inner class PolyglotQueryLineMarkerInfo(
        element: PsiElement,
        icon: Icon,
        expression: String
    ) : MergeableLineMarkerInfo<PsiElement?>(
        element, element.textRange, icon,
        { _ -> i18n("hybris.editor.gutter.pgq.tooltip") },
        { _, e -> copyToClipboard(e, expression) },
        GutterIconRenderer.Alignment.CENTER,
        Supplier { i18n("hybris.editor.gutter.pgq.tooltip") }
    ) {
        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is PolyglotQueryLineMarkerInfo && info.icon === icon
        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
    }
}