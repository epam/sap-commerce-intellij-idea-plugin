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

import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueGroup

abstract class AbstractImpExTableColumnMoveAction(private val direction: ImpExColumnPosition) : AbstractImpExTableColumnAction() {

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        val headerParameter = when (element) {
            is ImpExFullHeaderParameter -> element
            is ImpExValueGroup -> element.fullHeaderParameter
                ?: return

            else -> return
        }

        CoroutineScope(Dispatchers.Default).launch {
            val headerLine = readAction { headerParameter.headerLine }
                ?: return@launch
            val firstColumnIndex = readAction { headerParameter.columnNumber }
            val previousOffset = readAction { editor.caretModel.currentCaret.offset }
            val secondColumnIndex = firstColumnIndex + direction.step
            val file = readAction { headerParameter.containingFile }
            val fileText = readAction { file.fileDocument.text }
            val replacements = buildList {
                readAction { headerLine.valueLines }
                    .forEach { valueLine ->
                        val firstValueGroup = readAction { valueLine.getValueGroup(firstColumnIndex) }
                            ?: return@forEach
                        val secondValueGroup = readAction { valueLine.getValueGroup(secondColumnIndex) }
                            ?: return@forEach

                        add(firstValueGroup.replacement(secondValueGroup))
                        add(secondValueGroup.replacement(firstValueGroup))
                    }

                val firstParameter = readAction { headerLine.getFullHeaderParameter(firstColumnIndex) }
                    ?: return@launch
                val secondParameter = readAction { headerLine.getFullHeaderParameter(secondColumnIndex) }
                    ?: return@launch

                add(firstParameter.replacement(secondParameter, true))
                add(secondParameter.replacement(firstParameter, true))
            }

            val newContent = fileText.applyReplacements(replacements)

            writeCommandAction(project, "ImpEx - Move Column") {
                file.fileDocument.setText(newContent)

                val replacementTextLength = replacements
                    .find { (range, _) -> previousOffset in range }
                    ?.second
                    ?.length
                    // include ; length for header parameter
                    ?.let { if (element is ImpExFullHeaderParameter) it + 1 else it }
                    ?: 0

                val moveToOffset = previousOffset + (replacementTextLength * direction.step)
                editor.caretModel.currentCaret.moveToOffset(moveToOffset)
            }
        }
    }

    private suspend fun PsiElement.replacement(
        replacement: PsiElement,
        withLeftSpaces: Boolean = false,
    ): Pair<IntRange, String> = readAction {
        val leftSpaces = if (withLeftSpaces) replacement.prevSibling
            ?.asSafely<PsiWhiteSpace>()
            ?.textLength
            ?: 0
        else 0
        val rightSpaces = replacement.nextSibling
            ?.asSafely<PsiWhiteSpace>()
            ?.textLength
            ?: 0

        val replaceWith = " ".repeat(leftSpaces) + replacement.text + " ".repeat(rightSpaces)

        val actualStartOffset = if (withLeftSpaces) this.prevSibling
            ?.asSafely<PsiWhiteSpace>()
            ?.startOffset
            ?: startOffset
        else startOffset

        val actualEndOffset = this.nextSibling
            ?.asSafely<PsiWhiteSpace>()
            ?.endOffset
            ?: endOffset

        actualStartOffset..actualEndOffset to replaceWith
    }
}
