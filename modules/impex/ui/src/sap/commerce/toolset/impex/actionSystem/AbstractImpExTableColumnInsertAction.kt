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

import com.intellij.codeInsight.AutoPopupController
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import sap.commerce.toolset.impex.psi.*

abstract class AbstractImpExTableColumnInsertAction(private val position: ImpExColumnPosition) : AbstractImpExTableColumnAction() {

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        val headerParameter = when (element) {
            is ImpExFullHeaderParameter -> element
            is ImpExValueGroup -> element.fullHeaderParameter
                ?: return

            else -> return
        }

        val placement = if (position == ImpExColumnPosition.LEFT) "before"
        else "after"

        run(project, "Inserting a new column $placement '${headerParameter.text}'") {
            WriteCommandAction.runWriteCommandAction(project) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    val headerLine = headerParameter.headerLine ?: return@disablePostprocessFormattingInside
                    val column = headerParameter.columnNumber

                    val newElementAtCaret: PsiElement? = insertHeaderParam(project, headerLine, column, position)
                    insertValueGroups(project, headerLine.valueLines, column, position)

                    newElementAtCaret
                        ?.let {
                            val offset = when (position) {
                                ImpExColumnPosition.LEFT -> it.startOffset
                                ImpExColumnPosition.RIGHT -> it.endOffset
                            }
                            editor.caretModel.currentCaret.moveToOffset(offset)
                            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
                        }
                }
            }
        }
    }

    private fun insertHeaderParam(project: Project, headerLine: ImpExHeaderLine, column: Int, position: ImpExColumnPosition): PsiElement? {
        val current = headerLine.fullHeaderParameterList.getOrNull(column)
            ?: return null

        return ImpExElementFactory.createParametersSeparator(project)
            ?.let {
                when (position) {
                    ImpExColumnPosition.LEFT -> headerLine.addBefore(it, current)
                    ImpExColumnPosition.RIGHT -> headerLine.addAfter(it, current)
                }
            }
    }

    private fun insertValueGroups(project: Project, valueLines: Collection<ImpExValueLine>, column: Int, position: ImpExColumnPosition) {
        valueLines
            .forEach {
                val valueGroup = it.getValueGroup(column) ?: return@forEach
                val newValueGroup = ImpExElementFactory.createValueGroup(project) ?: return@forEach

                when (position) {
                    ImpExColumnPosition.LEFT -> it.addBefore(newValueGroup, valueGroup)
                    ImpExColumnPosition.RIGHT -> it.addAfter(newValueGroup, valueGroup)
                }
            }
    }

}