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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import kotlin.math.min

class ImpExTableColumnRemoveAction : AbstractImpExTableColumnAction() {

    init {
        with(templatePresentation) {
            text = "Remove Column"
            description = "Remove current column"
            icon = HybrisIcons.TABLE_COLUMN_REMOVE
        }
    }

    override fun performAction(project: Project, editor: Editor, element: PsiElement) {
        when (element) {
            is ImpexFullHeaderParameter -> {
                run(project, "Removing '${element.text}' column") {
                    val elements = runReadAction {
                        ImpexPsiUtils.getColumnForHeader(element)
                            .filter { it.isValid }
                            .reversed()
                    }

                    WriteCommandAction.runWriteCommandAction(project) {
                        PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                            elements.forEach { it.delete() }

                            PsiTreeUtilExt.getPrevSiblingOfElementType(element, ImpexTypes.PARAMETERS_SEPARATOR)
                                ?.delete()
                            element.delete()
                        }
                    }
                }
            }

            is ImpexValueGroup -> {
                val headerParameter = element.fullHeaderParameter
                    ?: return
                return performAction(project, editor, headerParameter)
            }

            else -> return
        }
    }

    private fun createTask(project: Project, element: ImpexFullHeaderParameter) = object : Task.Modal(project, "Removing Column", false) {
        override fun run(indicator: ProgressIndicator) {
            val elements = runReadAction {
                ImpexPsiUtils.getColumnForHeader(element)
                    .reversed()
            }
            val batchSize = 25
            val totalElements = elements.size
            var processedElements = 0

            while (processedElements < totalElements) {
                val end = min((processedElements + batchSize).toDouble(), totalElements.toDouble()).toInt()
                val batch = elements.subList(processedElements, end)

                processedElements = end

                ApplicationManager.getApplication().invokeAndWait {
                    indicator.fraction = processedElements.toDouble() / totalElements
                    indicator.text = "Removing elements: $processedElements of $totalElements"
                }

                WriteCommandAction.runWriteCommandAction(project) {
                    PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                        batch
                            .filter { it.isValid }
                            .forEach { it.delete() }
                    }
                }
            }

            WriteCommandAction.runWriteCommandAction(project) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    PsiTreeUtilExt.getPrevSiblingOfElementType(element, ImpexTypes.PARAMETERS_SEPARATOR)
                        ?.delete()
                    element.delete()
                }
            }
        }
    }
}
