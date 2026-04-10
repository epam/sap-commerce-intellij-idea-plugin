/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.impex.actionSystem

import com.intellij.codeInsight.AutoPopupController
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import kotlinx.coroutines.launch
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueGroup

abstract class AbstractImpExTableColumnInsertAction(private val position: ImpExColumnPosition) : AbstractImpExTableColumnAction() {

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        val headerParameter = when (element) {
            is ImpExFullHeaderParameter -> element
            is ImpExValueGroup -> element.fullHeaderParameter
                ?: return

            else -> return
        }

        currentThreadCoroutineScope().launch {
            val headerLine = readAction { headerParameter.headerLine }
                ?: return@launch
            val columnIndex = readAction { headerParameter.columnNumber }
            val parameter = readAction { headerLine.getFullHeaderParameter(columnIndex) }
                ?: return@launch
            val file = readAction { headerParameter.containingFile }

            val replacements = buildList {
                readAction { headerLine.valueLines }
                    .reversed()
                    .mapNotNull { readAction { it.getValueGroup(columnIndex) } }
                    .forEach { valueGroup ->
                        val valueGroupText = readAction { valueGroup.text }
                        val replacement = when (position) {
                            ImpExColumnPosition.LEFT -> ImpExConstants.FIELD_VALUE_SEPARATOR + valueGroupText
                            ImpExColumnPosition.RIGHT -> valueGroupText + ImpExConstants.FIELD_VALUE_SEPARATOR
                        }

                        add(valueGroup.startOffset..valueGroup.endOffset to replacement)
                    }

                val parameterText = readAction { parameter.text }
                val parameterReplacement = when (position) {
                    ImpExColumnPosition.LEFT -> ImpExConstants.PARAMETERS_SEPARATOR + parameterText
                    ImpExColumnPosition.RIGHT -> parameterText + ImpExConstants.PARAMETERS_SEPARATOR
                }

                add(parameter.startOffset..parameter.endOffset to parameterReplacement)
            }
            val newContent = readAction { file.fileDocument.text }
                .applyReplacements(replacements)

            writeCommandAction(project, "ImpEx - Insert Column") {
                file.fileDocument.setText(newContent)

                val moveToOffset = when (position) {
                    ImpExColumnPosition.LEFT -> parameter.startOffset - 1
                    ImpExColumnPosition.RIGHT -> parameter.endOffset + 1
                }
                editor.caretModel.currentCaret.moveToOffset(moveToOffset)
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            }
        }
    }
}
