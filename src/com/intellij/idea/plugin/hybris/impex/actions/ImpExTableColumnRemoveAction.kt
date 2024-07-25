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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.forEachWithProgress
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImpExTableColumnRemoveAction : AbstractImpExTableColumnAction() {

    private val commandName = "Remove Column"
    private val groupID = "action.sap.cx.impex.column.remove"

    init {
        with(templatePresentation) {
            text = commandName
            description = "Remove current column"
            icon = HybrisIcons.TABLE_COLUMN_REMOVE
        }
    }

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        currentThreadCoroutineScope().launch {
            val fullHeaderParameter = readAction {
                if (!psiFile.isValid) return@readAction null

                when (element) {
                    is ImpexFullHeaderParameter -> element
                    is ImpexValueGroup -> {
                        val headerParameter = element.fullHeaderParameter
                            ?: return@readAction null
                        return@readAction headerParameter
                    }

                    else -> return@readAction null
                }
            } ?: return@launch

            val elements = readAction {
                ImpexPsiUtils.getColumnForHeader(fullHeaderParameter)
                    .filter { it.isValid }
                    .reversed()
            }

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Removing '${fullHeaderParameter.text}' column") {
                        elements.forEachWithProgress {
                            WriteCommandAction.runWriteCommandAction(
                                project, commandName, groupID,
                                { it.delete() },
                                psiFile
                            )
                        }

                        WriteCommandAction.runWriteCommandAction(
                            project, commandName, groupID,
                            {
                                PsiTreeUtilExt.getPrevSiblingOfElementType(fullHeaderParameter, ImpexTypes.PARAMETERS_SEPARATOR)
                                    ?.delete()
                                fullHeaderParameter.delete()
                            },
                            psiFile
                        )
                    }
                }
            }
        }
    }
}
