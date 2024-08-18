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
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider
import com.intellij.codeInsight.hints.settings.language.isInlaySettingsEditor
import com.intellij.ide.DataManager
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import java.awt.event.MouseEvent
import javax.swing.Icon


class LoggerInlayHintsProvider : DaemonBoundCodeVisionProvider {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Default
    override val id: String
        get() = "LoggerInlayHintsProvider"
    override val name: String
        get() = "SAP Commerce Cloud Logger"

    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = emptyList()

    override fun computeForEditor(editor: Editor, file: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        val entries = mutableListOf<Pair<TextRange, CodeVisionEntry>>()

        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                if (shouldShowIcon(element)) {
                    when (element) {
                        is PsiClass -> println(element.name)
                        is PsiPackage -> println(element.name)
                        else -> println("element: $element")
                    }
                    val hint = "!Logging!"
                    val handler = ClickHandler(element, hint)
                    val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(element)
                    entries.add(range to ClickableTextCodeVisionEntry(hint, id, handler, HybrisIcons.Log.TOGGLE, hint, "Setup the logger"))
                }
            }
        })

        return entries
    }

    private fun shouldShowIcon(element: PsiElement): Boolean {
        // Implement your logic to decide whether the icon should be shown at this element
        return element is PsiClass || element is PsiPackageStatement
    }

    private inner class ClickHandler(
        element: PsiElement,
        private val hint: String,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            val element = elementPointer.element ?: return
            handleClick(editor, element, event)
        }
    }

    fun handleClick(editor: Editor, element: PsiElement, event: MouseEvent?) {
        val actionGroup = DefaultActionGroup().apply {
            add(LoggerAction("TRACE", HybrisIcons.Log.Level.TRACE))
            add(LoggerAction("DEBUG", HybrisIcons.Log.Level.DEBUG))
            add(LoggerAction("INFO", HybrisIcons.Log.Level.INFO))
            add(LoggerAction("WARN", HybrisIcons.Log.Level.WARN))
            add(LoggerAction("ERROR", HybrisIcons.Log.Level.ERROR))
            add(LoggerAction("FATAL", HybrisIcons.Log.Level.FATAL))
            add(LoggerAction("SEVERE", HybrisIcons.Log.Level.SEVERE))
        }

        val dataManager = DataManager.getInstance()

        // Show the popup menu
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Select an Option",
                actionGroup,
                dataManager.getDataContext(editor.component),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )

        popup.showInBestPositionFor(editor)
    }

}

class LoggerAction(private val text: String, icon: Icon) : AnAction(text, "", icon) {
    override fun actionPerformed(e: AnActionEvent) {
        // Handle the action click
        println("Action '$text' clicked")
    }
}