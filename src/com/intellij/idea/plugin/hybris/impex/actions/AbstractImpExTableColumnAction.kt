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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilBase
import javax.swing.Icon

abstract class AbstractImpExTableColumnAction(text: String, description: String, icon: Icon) : AbstractImpExTableAction(text, description, icon) {

    override fun isActionAllowed(project: Project, editor: Editor, element: PsiElement) = getSuitableImpExElement(element) != null

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        PsiUtilBase.getElementAtCaret(editor)
            ?.let { getSuitableImpExElement(it) }
            ?.let {
                WriteCommandAction.runWriteCommandAction(project) {
                    actionPerformed(project, editor, it)
                }
            }
    }

    private fun getSuitableImpExElement(element: PsiElement) = PsiTreeUtil
        .getParentOfType(element, ImpexFullHeaderParameter::class.java, ImpexValueGroup::class.java)

    abstract fun actionPerformed(project: Project, editor: Editor, element: PsiElement)

}
