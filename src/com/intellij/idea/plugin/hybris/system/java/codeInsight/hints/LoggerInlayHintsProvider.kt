/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.codeInsight.daemon.impl.JavaCodeVisionProviderBase
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.settings.language.isInlaySettingsEditor
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.endOffset
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.awt.event.MouseEvent


class LoggerInlayHintsProvider : JavaCodeVisionProviderBase() {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Default
    override val id: String
        get() = "SAPCxLoggerInlayHintsProvider"
    override val name: String
        get() = "SAP CX Logger"

    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = emptyList()

    override fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        if (!ProjectSettingsComponent.getInstance(psiFile.project).isHybrisProject()) return emptyList()

        val entries = mutableListOf<Pair<TextRange, CodeVisionEntry>>()

        psiFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                val targetElement = when (element) {
                    is PsiClass -> PsiTreeUtil.getChildrenOfType(element, PsiKeyword::class.java)
                        ?.first()
                        ?.text
                        ?.takeIf { it == "class" || it == "enum" || it == "interface" || it == "record" }
                        ?.let { element.nameIdentifier }

                    is PsiPackageStatement -> PsiTreeUtil.getChildrenOfType(element, PsiKeyword::class.java)
                        ?.first()
                        ?.text
                        ?.takeIf { it == "package" }
                        ?.let { element.packageReference }

                    else -> null
                }
                if (targetElement == null) return

                val loggerIdentifier = extractIdentifierForLogger(element, psiFile) ?: return

                val handler = ClickHandler(targetElement, loggerIdentifier)
                val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(targetElement)
                entries.add(range to ClickableTextCodeVisionEntry("[y] log level", id, handler, HybrisIcons.Y.REMOTE, "", "Setup the logger for SAP Commerce Cloud"))
            }
        })

        return entries
    }

    fun extractIdentifierForLogger(element: PsiElement, file: PsiFile): String? = when (element) {
        is PsiClass -> file.packageName()
            ?.let { "$it.${element.name}" }

        is PsiPackageStatement -> element.packageName
        else -> null
    }

    private inner class ClickHandler(
        element: PsiElement,
        private val loggerIdentifier: String,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            val element = elementPointer.element ?: return
            handleClick(editor, element, loggerIdentifier)
        }
    }

    fun handleClick(editor: Editor, element: PsiElement, loggerIdentifier: String) {
        val actionGroup = ActionManager.getInstance().getAction("sap.cx.logging.actions") as ActionGroup

        val project = editor.project ?: return
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.EDITOR, editor)
            .add(HybrisConstants.KEY_LOGGER_IDENTIFIER_DATA_CONTEXT, loggerIdentifier)
            .build()

        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Select an Option",
                actionGroup,
                dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )

        // Calculate the position for the popup
        val implementsPsiElement = PsiTreeUtil.findSiblingForward(element, JavaStubElementTypes.IMPLEMENTS_LIST, null) ?: element

        val offset = implementsPsiElement.endOffset
        val logicalPosition = editor.offsetToLogicalPosition(offset)
        val visualPosition = editor.logicalToVisualPosition(logicalPosition)
        val point = editor.visualPositionToXY(visualPosition)

        // Convert the point to a RelativePoint
        val relativePoint = RelativePoint(editor.contentComponent, Point(point))

        // Show the popup at the calculated relative point
        popup.show(relativePoint)
    }
}

private fun PsiFile.packageName() = childrenOfType<PsiPackageStatement>()
    .firstOrNull()
    ?.packageName