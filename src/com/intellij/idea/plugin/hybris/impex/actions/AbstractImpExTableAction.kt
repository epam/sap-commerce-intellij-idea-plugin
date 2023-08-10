/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilBase
import javax.swing.Icon

abstract class AbstractImpExTableAction(text: String, description: String, icon: Icon) : AnAction(text, description, icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

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

    abstract fun isActionAllowed(project: Project, editor: Editor, element: PsiElement): Boolean
}
