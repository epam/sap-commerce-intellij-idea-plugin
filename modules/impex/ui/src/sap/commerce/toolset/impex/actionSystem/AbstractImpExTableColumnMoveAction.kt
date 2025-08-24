/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.startOffset
import sap.commerce.toolset.impex.editor.event.ImpExHighlightingCaretListener
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExValueGroup
import sap.commerce.toolset.impex.psi.ImpExValueLine
import java.util.*

abstract class AbstractImpExTableColumnMoveAction(private val direction: ImpExColumnPosition) : AbstractImpExTableColumnAction() {

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        val headerParameter = when (element) {
            is ImpExFullHeaderParameter -> element
            is ImpExValueGroup -> element.fullHeaderParameter
                ?: return

            else -> return
        }

        run(project, "Moving the '${headerParameter.text}' column ${direction.name.lowercase(Locale.ROOT)}") {
            WriteCommandAction.runWriteCommandAction(project) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    ImpExHighlightingCaretListener.getInstance().clearHighlightedArea(editor)

                    val headerLine = headerParameter.headerLine ?: return@disablePostprocessFormattingInside
                    val column = headerParameter.columnNumber

                    val previousOffset = editor.caretModel.currentCaret.offset
                    val previousElementStartOffset = element.startOffset

                    var newElementAtCaret: PsiElement? = null
                    moveHeaderParam(headerLine, column, direction)
                        ?.let { newElementAtCaret = it }
                    moveValueGroups(headerLine.valueLines, element, column, direction)
                        ?.let { newElementAtCaret = it }

                    newElementAtCaret
                        ?.let {
                            val caretOffsetInText = previousOffset - previousElementStartOffset
                            editor.caretModel.currentCaret.moveToOffset(it.startOffset + caretOffsetInText)
                        }
                }
            }
        }
    }

    private fun moveHeaderParam(headerLine: ImpExHeaderLine, column: Int, direction: ImpExColumnPosition): ImpExFullHeaderParameter? {
        val newColumn = column + direction.step
        val previous = headerLine.fullHeaderParameterList.getOrNull(newColumn)
            ?: return null
        val current = headerLine.fullHeaderParameterList.getOrNull(column)
            ?: return null

        replacePsiElement(previous, current)

        return headerLine.fullHeaderParameterList.getOrNull(newColumn)
    }

    private fun moveValueGroups(valueLines: Collection<ImpExValueLine>, elementAtCaret: PsiElement, column: Int, direction: ImpExColumnPosition): PsiElement? {
        val newColumn = column + direction.step
        var newElementAtCaret: PsiElement? = null
        valueLines.forEach {
            val first = it.getValueGroup(newColumn)
            val second = it.getValueGroup(column)

            if (first != null && second != null) {
                replacePsiElement(first, second)

                if (first == elementAtCaret || second == elementAtCaret) {
                    newElementAtCaret = it.getValueGroup(newColumn)
                }
            }
        }
        return newElementAtCaret
    }

    private fun replacePsiElement(first: PsiElement, second: PsiElement) {
        val firstCopy = first.copy()
        val secondCopy = second.copy()

        first.replace(secondCopy)
        second.replace(firstCopy)
    }

}

