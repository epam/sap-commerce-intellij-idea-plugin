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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase


abstract class AbstractImpExTableAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        var actionAllowed = false
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (project != null && editor != null && editor.caretModel.caretCount == 1) {
            PsiUtilBase.getElementAtCaret(editor)
                ?.let {
                    actionAllowed = isActionAllowed(project, editor, it)
                }
        }

        e.presentation.isEnabled = actionAllowed
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        PsiUtilBase.getElementAtCaret(editor)
            ?.let { getSuitableElement(it) }
            ?.takeIf { EditorModificationUtil.requestWriting(editor) }
            ?.let { performAction(project, editor, psiFile, it) }
    }

    abstract fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement)
    abstract fun getSuitableElement(element: PsiElement): PsiElement?
    abstract fun isActionAllowed(project: Project, editor: Editor, element: PsiElement): Boolean

    protected fun run(project: Project, title: String, block: (ProgressIndicator) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, title, false, DEAF) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                block.invoke(indicator)
            }
        })
    }
}
