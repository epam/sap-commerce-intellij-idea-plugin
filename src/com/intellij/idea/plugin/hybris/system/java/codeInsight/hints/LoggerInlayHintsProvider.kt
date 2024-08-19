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
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
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

                if (isEligibleForLogging(element)) {
                    val handler = ClickHandler(element)
                    val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(element)
                    entries.add(range to ClickableTextCodeVisionEntry("", id, handler, HybrisIcons.Log.TOGGLE, "", "Setup the logger for SAP Commerce Cloud"))
                }
            }
        })

        return entries
    }

    private fun isEligibleForLogging(element: PsiElement): Boolean {
        return element is PsiClass || element is PsiPackageStatement
    }

    private inner class ClickHandler(
        element: PsiElement,
    ) : (MouseEvent?, Editor) -> Unit {
        private val elementPointer = SmartPointerManager.createPointer(element)

        override fun invoke(event: MouseEvent?, editor: Editor) {
            if (isInlaySettingsEditor(editor)) return
            val element = elementPointer.element ?: return
            handleClick(editor, element)
        }
    }

    fun handleClick(editor: Editor, element: PsiElement) {
        val logIdentifier = getLogIdentifier(element) ?: return

        val actionGroup = DefaultActionGroup().apply {
            add(LoggerAction("TRACE", logIdentifier, HybrisIcons.Log.Level.TRACE))
            add(LoggerAction("DEBUG", logIdentifier, HybrisIcons.Log.Level.DEBUG))
            add(LoggerAction("INFO", logIdentifier, HybrisIcons.Log.Level.INFO))
            add(LoggerAction("WARN", logIdentifier, HybrisIcons.Log.Level.WARN))
            add(LoggerAction("ERROR", logIdentifier, HybrisIcons.Log.Level.ERROR))
            add(LoggerAction("FATAL", logIdentifier, HybrisIcons.Log.Level.FATAL))
            add(LoggerAction("SEVERE", logIdentifier, HybrisIcons.Log.Level.SEVERE))
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

        // Calculate the position for the popup
        val offset = element.textOffset
        val logicalPosition = editor.offsetToLogicalPosition(offset)
        val visualPosition = editor.logicalToVisualPosition(logicalPosition)
        val point = editor.visualPositionToXY(visualPosition)

        // Convert the point to a RelativePoint
        val relativePoint = RelativePoint(editor.contentComponent, Point(point))

        // Show the popup at the calculated relative point
        popup.show(relativePoint)
    }
}

class LoggerAction(private val logLevel: String, val logIdentifier: String, val icon: Icon) : AnAction(logLevel, "", icon) {

    override fun actionPerformed(e: AnActionEvent) {
        //TODO comment this
        println("Set the log level: $logLevel for $logIdentifier")

        val project = e.project ?: return
        val psiFile: PsiFile = e.getData(LangDataKeys.PSI_FILE) ?: return
        val virtualFile = psiFile.virtualFile
        val packageName = getPackageName(psiFile)

        val fileNameWithPackage = "$packageName.${virtualFile.nameWithoutExtension}"

        val result = HybrisHacHttpClient.getInstance(project).executeLogUpdate(
            project,
            fileNameWithPackage,
            logLevel,
            AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
        )
        println(result.statusCode)
    }

    private fun getPackageName(psiFile: PsiFile): String {
        val packageStatement = psiFile.children.firstOrNull { it is PsiPackageStatement }
        return (packageStatement as? PsiPackageStatement)?.packageName ?: ""
    }
}

fun getLogIdentifier(element: PsiElement) = when (element) {
    is PsiClass -> element.name
    is PsiPackageStatement -> element.packageName
    else -> null
}