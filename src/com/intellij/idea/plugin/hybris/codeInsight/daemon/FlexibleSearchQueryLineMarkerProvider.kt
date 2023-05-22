/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchElementFactory
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.JavaConstantExpressionEvaluator
import com.intellij.util.Function
import java.awt.datatransfer.StringSelection
import java.util.function.Supplier
import javax.swing.Icon

private val topRegex = "(SELECT )|( UNION )|( DISTINCT )|( ORDER BY )|( LEFT JOIN )|( JOIN )|( FROM )|( WHERE )|( ASC )|( DESC )|( ON )".toRegex(RegexOption.IGNORE_CASE)
private val regexFrom = " FROM ".toRegex(RegexOption.IGNORE_CASE)
private val regexLeftJoin = " LEFT JOIN ".toRegex(RegexOption.IGNORE_CASE)
private val regexWhere = " WHERE ".toRegex(RegexOption.IGNORE_CASE)
private val regexUnit = " UNION ".toRegex(RegexOption.IGNORE_CASE)
private val whitespaceRegex = "\\s+".toRegex()

class FlexibleSearchQueryLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName() = message("hybris.editor.gutter.fsq.name")
    override fun getIcon(): Icon = HybrisIcons.FXS_FILE

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = when (element) {
        is PsiPolyadicExpression -> process(element) { computeExpression(element) }
        is PsiLiteralExpression -> process(element) { computeExpression(element) }
        else -> null
    }

    private fun process(
        element: PsiElement,
        expressionProvider: () -> String
    ): FlexibleSearchQueryLineMarkerInfo? {
        val parent = element.parent
        if (parent !is PsiVariable || parent.nameIdentifier == null) return null
        if (!CommonIdeaService.getInstance().isHybrisProject(element.project)) return null

        val expression = expressionProvider.invoke()
        if (!(expression.contains(topRegex) && expression.contains('{') && expression.contains('}'))) return null

        val formattedExpression = formatExpression(element.project, expression)

        val tooltipProvider = Function { _: PsiElement? ->
            "${message("hybris.editor.gutter.fsq.tooltip")}<br><hr>$formattedExpression"
        }

        return FlexibleSearchQueryLineMarkerInfo(parent.nameIdentifier!!, icon, tooltipProvider, CopyToClipboard(formattedExpression))
    }

    private fun formatExpression(project: Project, expression: String): String {
        val fxsFile = FlexibleSearchElementFactory.createFile(project, expression)

        return CodeStyleManager.getInstance(project).reformat(fxsFile).text
    }

    private fun computeExpression(element: PsiLiteralExpression): String = if (element.isTextBlock) {
        element.text
            .replace("\"\"\"", "")
    } else {
        element.text
            .replace("\"", "")
    }

    private fun computeExpression(literalExpression: PsiPolyadicExpression): String {
        var computedValue = ""

        literalExpression.operands
            .forEach { operand ->
                if (operand is PsiReference) {
                    val probableDefinition = operand.resolve()
                    if (probableDefinition is PsiVariable) {
                        probableDefinition.initializer?.let { initializer ->
                            val value = JavaConstantExpressionEvaluator.computeConstantExpression(initializer, true);
                            if (value is String) {
                                computedValue += value;
                            }
                        }
                    }
                } else {
                    val value = JavaConstantExpressionEvaluator.computeConstantExpression(operand, true);
                    if (value is String) {
                        computedValue += value;
                    }
                }
            }
        return computedValue
            .trim()
            .replace("\n", "")
            .replace("\t", "")
            .replace(whitespaceRegex, " ")
    }

    internal class CopyToClipboard(val content: String) : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            CopyPasteManager.getInstance().setContents(StringSelection(content))
            Notifications.create(NotificationType.INFORMATION, message("hybris.editor.gutter.fsq.notification.title"), content)
                .hideAfter(10)
                .notify(e.project)
        }
    }

    internal class FlexibleSearchQueryLineMarkerInfo(
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

        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is FlexibleSearchQueryLineMarkerInfo && info.getIcon() === icon

        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
    }

}
