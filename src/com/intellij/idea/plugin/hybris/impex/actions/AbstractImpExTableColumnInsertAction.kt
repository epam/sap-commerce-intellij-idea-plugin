/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.codeInsight.AutoPopupController
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

abstract class AbstractImpExTableColumnInsertAction(private val position: ImpExColumnPosition) : AbstractImpExTableColumnAction() {

    override fun performCommand(project: Project, editor: Editor, element: PsiElement) {
        when (element) {
            is ImpexFullHeaderParameter -> insert(project, editor, element, position)
            is ImpexValueGroup -> element.fullHeaderParameter
                ?.let { insert(project, editor, it, position) }
        }
    }

    private fun insert(project: Project, editor: Editor, headerParameter: ImpexFullHeaderParameter, position: ImpExColumnPosition) {
        val headerLine = headerParameter.headerLine ?: return
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

    private fun insertHeaderParam(project: Project, headerLine: ImpexHeaderLine, column: Int, position: ImpExColumnPosition): PsiElement? {
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

    private fun insertValueGroups(project: Project, valueLines: Collection<ImpexValueLine>, column: Int, position: ImpExColumnPosition) {
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