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

package com.intellij.idea.plugin.hybris.system.java.codeInsight.hints

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.codeVision.ui.model.ClickableRichTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.richText.RichText
import com.intellij.codeInsight.daemon.impl.JavaCodeVisionProviderBase
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.settings.language.isInlaySettingsEditor
import com.intellij.ide.actions.FqnUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.*
import com.intellij.ui.awt.RelativePoint
import java.awt.event.MouseEvent

class LoggerInlayHintsProvider : JavaCodeVisionProviderBase() {
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Default
    override val id: String = "SAPCxLoggerInlayHintsProvider"
    override val name: String = "SAP CX Logger"
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()

    override fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        if (psiFile.isNotHybrisProject) return emptyList()
        val project = psiFile.project

        return PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java, PsiPackageStatement::class.java)
            .mapNotNull {
                val fqn = when (it) {
                    is PsiClass -> FqnUtil.elementToFqn(it, editor)
                    is PsiPackageStatement -> it.packageName
                    else -> null
                } ?: return@mapNotNull null
                it to fqn
            }
            .map { (psiElement, loggerIdentifier) ->
                val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(psiElement)
                val logger = CxLoggerAccess.getInstance(project).logger(loggerIdentifier)
                val text = if (logger == null) RichText("[y] log level")
                else {
                    val style = if (logger.inherited) SimpleTextAttributes(STYLE_UNDERLINE or STYLE_BOLD or STYLE_ITALIC, JBColor.GRAY)
                    else SimpleTextAttributes(STYLE_PLAIN, JBColor.blue)

                    RichText("[").apply {
                        append(logger.effectiveLevel, style)
                        append("] log level", REGULAR_ATTRIBUTES)
                    }
                }

                val handler = ClickHandler(psiElement, loggerIdentifier, text)
                val tooltip = when {
                    logger == null -> "Fetch or Define the logger for SAP Commerce"
                    logger.inherited -> "Inherited from: ${logger.parentName}"
                    else -> "Setup the logger for SAP Commerce"
                }

                return@map range to ClickableRichTextCodeVisionEntry(id, text, handler, HybrisIcons.Y.REMOTE, "", tooltip)
            }
    }

    private inner class ClickHandler(
        element: PsiElement,
        private val loggerIdentifier: String,
        val text: RichText,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            val element = elementPointer.element ?: return

            handleClick(editor, element, loggerIdentifier, event)
        }
    }

    fun handleClick(editor: Editor, element: PsiElement, loggerIdentifier: String, event: MouseEvent?) {
        val actionGroup = ActionManager.getInstance().getAction("sap.cx.logging.actions") as ActionGroup
        val project = editor.project ?: return
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.EDITOR, editor)
            .add(HybrisConstants.DATA_KEY_LOGGER_IDENTIFIER, loggerIdentifier)
            .build()

        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Select an Option",
                actionGroup,
                dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )

        // Convert the point to a RelativePoint
        val relativePoint = if (event != null) RelativePoint(event)
        else JBPopupFactory.getInstance().guessBestPopupLocation(editor)

        // Show the popup at the calculated relative point
        popup.show(relativePoint)
    }
}