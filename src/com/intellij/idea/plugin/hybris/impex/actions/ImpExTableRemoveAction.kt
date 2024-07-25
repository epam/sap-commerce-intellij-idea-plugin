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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRights
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImpExTableRemoveAction : AbstractImpExTableAction() {

    init {
        with(templatePresentation) {
            text = "Remove Table"
            description = "Remove current table"
            icon = HybrisIcons.TABLE_REMOVE
        }
    }

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        if (element is ImpexUserRights) removeUserRightsTable(project, element)
        else removeTable(project, editor, psiFile, element)
    }

    private fun removeUserRightsTable(project: Project, element: PsiElement) {
        WriteCommandAction.runWriteCommandAction(project) {
            element.delete()
        }
    }

    private fun removeTable(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        currentThreadCoroutineScope().launch {
            val textRange = readAction {
                if (!psiFile.isValid) return@readAction null

                when (element) {
                    is ImpexHeaderLine -> element.tableRange
                    is ImpexValueLine -> element.headerLine
                        ?.tableRange
                        ?: return@readAction null

                    else -> return@readAction null
                }
            } ?: return@launch

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Removing table") {
                        WriteCommandAction.runWriteCommandAction(project) {
                            PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                                editor.document.deleteString(textRange.startOffset, textRange.endOffset)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getSuitableElement(element: PsiElement) = PsiTreeUtil
        .getParentOfType(element, ImpexValueLine::class.java, ImpexHeaderLine::class.java, ImpexUserRights::class.java)

    override fun isActionAllowed(project: Project, editor: Editor, element: PsiElement) = getSuitableElement(element) != null

}
