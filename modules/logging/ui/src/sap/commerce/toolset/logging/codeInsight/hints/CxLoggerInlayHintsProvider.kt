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

package sap.commerce.toolset.logging.codeInsight.hints

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.codeVision.ui.model.ClickableRichTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.richText.RichText
import com.intellij.codeInsight.daemon.impl.JavaCodeVisionProviderBase
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.settings.language.isInlaySettingsEditor
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
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import java.awt.event.MouseEvent

class CxLoggerInlayHintsProvider : JavaCodeVisionProviderBase() {

    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Default
    override val id: String = "SAPCxLoggerInlayHintsProvider"
    override val name: String = "SAP CX Logger"
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()

    private data class LoggerHintTarget(
        val element: PsiElement,
        val loggerIdentifier: String,
    )

    override fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        if (psiFile.isNotHybrisProject) return emptyList()
        val project = psiFile.project
        val logStateService = CxRemoteLogStateService.getInstance(project)

        return collectHintTargets(psiFile)
            .map { target ->
                val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(target.element)
                val logger = logStateService.logger(target.loggerIdentifier)
                val text = buildHintText(logger)
                val handler = ClickHandler(target.element, target.loggerIdentifier)
                val tooltip = buildTooltip(logger)

                range to ClickableRichTextCodeVisionEntry(id, text, handler, HybrisIcons.Y.REMOTE, "", tooltip)
            }
    }

    private inner class ClickHandler(
        element: PsiElement,
        private val loggerIdentifier: String,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            elementPointer.element ?: return
            handleClick(editor, loggerIdentifier, event)
        }
    }

    private fun handleClick(editor: Editor, loggerIdentifier: String, event: MouseEvent?) {
        val actionGroup = ActionManager.getInstance().getAction("sap.cx.logging.actions").asSafely<ActionGroup>() ?: return
        val project = editor.project ?: return
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.EDITOR, editor)
            .add(CxLogConstants.DATA_KEY_LOGGER_IDENTIFIER, loggerIdentifier)
            .build()

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            /* title = */ "Select an Option",
            /* actionGroup = */ actionGroup,
            /* dataContext = */ dataContext,
            /* selectionAidMethod = */ JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            /* showDisabledActions = */ true
        )

        val relativePoint = if (event != null) RelativePoint(event)
        else JBPopupFactory.getInstance().guessBestPopupLocation(editor)

        popup.show(relativePoint)
    }

    private fun collectHintTargets(psiFile: PsiFile): List<LoggerHintTarget> {
        val resolver = CxLoggerIdentifierResolver.getInstance(psiFile.project)
        return PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiPackageStatement::class.java, PsiField::class.java)
            .mapNotNull { element ->
                when (element) {
                    is PsiPackageStatement -> LoggerHintTarget(element, element.packageName)
                    is PsiField -> {
                        val loggerIdentifier = resolver.resolve(element) ?: return@mapNotNull null
                        LoggerHintTarget(element, loggerIdentifier)
                    }

                    else -> null
                }
            }
    }

    private fun buildHintText(logger: CxLoggerPresentation?): RichText {
        if (logger == null) return RichText("[y] log level")

        val style = when {
            logger.inherited -> SimpleTextAttributes(
                SimpleTextAttributes.STYLE_UNDERLINE or SimpleTextAttributes.STYLE_BOLD or SimpleTextAttributes.STYLE_ITALIC,
                JBColor.GRAY
            )

            else -> SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.blue)
        }

        return RichText().apply {
            append("[", SimpleTextAttributes.REGULAR_ATTRIBUTES)
            append(logger.level.name, style)
            append("] log level", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    private fun buildTooltip(logger: CxLoggerPresentation?): String = when {
        logger == null -> "Fetch or Define the logger for SAP Commerce"
        logger.inherited -> "Inherited from: ${logger.parentName}"
        else -> "Setup the logger for SAP Commerce"
    }
}
