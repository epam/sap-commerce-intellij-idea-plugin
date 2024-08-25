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
import com.intellij.ide.DataManager
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.endOffset
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.Icon


class LoggerInlayHintsProvider : JavaCodeVisionProviderBase() {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Default
    override val id: String
        get() = "LoggerInlayHintsProvider"
    override val name: String
        get() = "SAP Commerce Cloud Logger"

    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = emptyList()

    override fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        //todo check it is hybris

        val entries = mutableListOf<Pair<TextRange, CodeVisionEntry>>()

        psiFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                val targetElement = when (element) {
                    is PsiClass -> {
                        val psiKeyword = PsiTreeUtil.getChildrenOfType(element, PsiKeyword::class.java)?.first()?.text
                        if (psiKeyword == "class")
                             element.nameIdentifier
                        else null
                    }
                    is PsiPackageStatement -> {
                        val psiKeyword = PsiTreeUtil.getChildrenOfType(element, PsiKeyword::class.java)?.first()?.text
                        if (psiKeyword == "package")
                            element.packageReference
                        else null
                    }
                    else -> null
                }
                if (targetElement == null) return

                val loggerIdentifier = extractIdentifierForLogger(element, psiFile) ?: return

                val handler = ClickHandler(targetElement, loggerIdentifier)
                val range = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(targetElement)
                entries.add(range to ClickableTextCodeVisionEntry("log level", id, handler, HybrisIcons.Log.TOGGLE, "", "Setup the logger for SAP Commerce Cloud"))
            }
        })

        return entries
    }

    fun extractIdentifierForLogger(element: PsiElement, file: PsiFile): String? = when (element) {
        is PsiClass -> file.packageName()?.let { "$it.${element.name}" }
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
        val actionGroup = DefaultActionGroup().apply {
            add(LoggerAction("TRACE", loggerIdentifier, HybrisIcons.Log.Level.TRACE))
            add(LoggerAction("DEBUG", loggerIdentifier, HybrisIcons.Log.Level.DEBUG))
            add(LoggerAction("INFO", loggerIdentifier, HybrisIcons.Log.Level.INFO))
            add(LoggerAction("WARN", loggerIdentifier, HybrisIcons.Log.Level.WARN))
            add(LoggerAction("ERROR", loggerIdentifier, HybrisIcons.Log.Level.ERROR))
            add(LoggerAction("FATAL", loggerIdentifier, HybrisIcons.Log.Level.FATAL))
            add(LoggerAction("SEVERE", loggerIdentifier, HybrisIcons.Log.Level.SEVERE))
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
        val offset = element.endOffset
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
        val project = e.project ?: return

        val result = HybrisHacHttpClient.getInstance(project).executeLogUpdate(
            project,
            logIdentifier,
            logLevel,
            AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
        )

        val resultMessage = if (result.statusCode == 200) "Success" else "Failed"
        val title = "Logger - $resultMessage"
        Notifications.create(
            NotificationType.INFORMATION, title,
            "Set the log level: $logLevel for $logIdentifier. Server response: ${result.statusCode}"
        )
            .hideAfter(5)
            .notify(project)
    }
}

private fun PsiFile.packageName() = childrenOfType<PsiPackageStatement>()
    .firstOrNull()
    ?.packageName