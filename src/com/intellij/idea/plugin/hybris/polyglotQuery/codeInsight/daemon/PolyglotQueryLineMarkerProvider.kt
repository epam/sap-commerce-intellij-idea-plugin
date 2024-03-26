/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.polyglotQuery.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryUtils
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotElementFactory
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.Function
import java.awt.datatransfer.StringSelection
import java.util.function.Supplier
import javax.swing.Icon

class PolyglotQueryLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName() = HybrisI18NBundleUtils.message("hybris.editor.gutter.pgq.name")
    override fun getIcon(): Icon = HybrisIcons.PGQ_FILE

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = when (element) {
        is PsiPolyadicExpression -> process(element) { PolyglotQueryUtils.computeExpression(element) }
        is PsiLiteralExpression -> process(element) { PolyglotQueryUtils.computeExpression(element) }
        else -> null
    }

    private fun process(
        element: PsiElement,
        expressionProvider: () -> String
    ): PolyglotQueryLineMarkerInfo? {
        val parent = element.parent
        if (parent !is PsiVariable || parent.nameIdentifier == null) return null
        if (!ProjectSettingsComponent.getInstance(element.project).isHybrisProject()) return null

        val expression = expressionProvider.invoke()
        if (!PolyglotQueryUtils.isPolyglotQuery(expression)) return null

        val formattedExpression = formatExpression(element.project, expression)

        val tooltipProvider = Function { _: PsiElement? ->
            "${HybrisI18NBundleUtils.message("hybris.editor.gutter.pgq.tooltip")}<br><hr>$formattedExpression"
        }

        return PolyglotQueryLineMarkerInfo(parent.nameIdentifier!!, icon, tooltipProvider, CopyToClipboard(formattedExpression))
    }

    private fun formatExpression(project: Project, expression: String): String {
        val fxsFile = PolyglotElementFactory.createFile(project, expression)

        return CodeStyleManager.getInstance(project).reformat(fxsFile).text
    }

    internal class CopyToClipboard(val content: String) : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            CopyPasteManager.getInstance().setContents(StringSelection(content))
            Notifications.create(NotificationType.INFORMATION, HybrisI18NBundleUtils.message("hybris.editor.gutter.pgq.notification.title"), content)
                .hideAfter(10)
                .notify(e.project)
        }
    }

    internal class PolyglotQueryLineMarkerInfo(
        element: PsiElement,
        icon: Icon,
        tooltipProvider: Function<in PsiElement?, String>,
        val action: AnAction
    ) :
        MergeableLineMarkerInfo<PsiElement?>(element, element.textRange, icon, tooltipProvider, null, GutterIconRenderer.Alignment.CENTER,
            Supplier { tooltipProvider.`fun`(element) }) {

        override fun createGutterRenderer(): GutterIconRenderer {
            return object : LineMarkerGutterIconRenderer<PsiElement?>(this) {
                override fun getClickAction() = action
                override fun isNavigateAction() = true
                override fun getPopupMenuActions() = null
            }
        }

        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()

        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is PolyglotQueryLineMarkerInfo && info.getIcon() === icon

        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
    }
}