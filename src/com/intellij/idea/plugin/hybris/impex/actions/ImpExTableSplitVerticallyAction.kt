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
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.childrenOfType

class ImpExTableSplitVerticallyAction : AbstractImpExTableColumnAction() {

    init {
        with(templatePresentation) {
            text = "Split Table"
            description = "Split current table vertically"
            icon = HybrisIcons.TABLE_SPLIT_VERTICALLY
        }
    }

    override fun isActionAllowed(project: Project, editor: Editor, element: PsiElement): Boolean {
        val suitableElement = getSuitableElement(element) ?: return false

        return when (suitableElement) {
            // Table with only-unique columns are not allowed for split
            is ImpexFullHeaderParameter -> canProcess(suitableElement.headerLine)
            is ImpexValueGroup -> canProcess(suitableElement.valueLine?.headerLine)
            else -> false
        }
    }

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        val headerParameter = when (element) {
            is ImpexFullHeaderParameter -> element
            is ImpexValueGroup -> element.fullHeaderParameter
                ?: return

            else -> return
        }

        run(project, "Splitting the table by '${headerParameter.text}' column") {
            WriteCommandAction.runWriteCommandAction(project) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    split(editor, headerParameter)
                }
            }
        }
    }

    private fun canProcess(headerLine: ImpexHeaderLine?): Boolean {
        headerLine ?: return false
        return headerLine.fullHeaderParameterList.size != headerLine.uniqueFullHeaderParameters.size
    }

    private fun split(editor: Editor, headerParameter: ImpexFullHeaderParameter) {
        val headerLine = headerParameter.headerLine ?: return
        val project = headerLine.project

        val columnNumber = headerParameter.columnNumber
        val headerParameters = headerLine.fullHeaderParameterList.size

        // collect numbers of all unique columns
        val uniqueColumns = headerLine.uniqueFullHeaderParameters
            .map { it.columnNumber }

        // calculate table range
        val tableRange = headerLine.tableRange

        // let's start cloning! left
        val cloneTableLeft = ImpExElementFactory.createFile(
            project,
            headerLine.containingFile.text.substring(tableRange.startOffset, tableRange.endOffset)
        )

        // grab header lines from both cloned future left and right tables
        val cloneLeftHeaderLine = cloneTableLeft.childrenOfType<ImpexHeaderLine>().firstOrNull() ?: return

        // before the actual split, we have to ensure that we have enough value groups
        cloneLeftHeaderLine.valueLines
            .forEach {
                val valueGroups = it.valueGroupList
                val missingValueGroups = headerParameters - valueGroups.size

                it.addValueGroups(missingValueGroups)
            }
        // and clone right
        val cloneTableRight = cloneTableLeft.copy()

        val cloneRightHeaderLine = cloneTableRight.childrenOfType<ImpexHeaderLine>().firstOrNull() ?: return

        // before deletion of the header params, we have to remove value groups
        // delete value groups from the left table
        deleteValueGroups(cloneLeftHeaderLine) {
            it >= columnNumber && !uniqueColumns.contains(it)
        }

        // and delete value groups from the right table
        deleteValueGroups(cloneRightHeaderLine) {
            it < columnNumber && !uniqueColumns.contains(it)
        }

        // delete header param from the left table
        deleteHeaderParams(cloneLeftHeaderLine) {
            it >= columnNumber && !uniqueColumns.contains(it)
        }

        // delete header param from the right table
        deleteHeaderParams(cloneRightHeaderLine) {
            it < columnNumber && !uniqueColumns.contains(it)
        }

        // perform actual modification of the document, also inject a few new lines for the right cloned table
        val cloneLeftTableText = CodeStyleManager.getInstance(project).reformat(cloneTableLeft).text
        val cloneRightTableText = "\n" + CodeStyleManager.getInstance(project).reformat(cloneTableRight).text

        editor.document.replaceString(tableRange.startOffset, tableRange.endOffset, cloneLeftTableText)
        editor.document.insertString(tableRange.startOffset + cloneLeftTableText.length, cloneRightTableText)

        val cloneStartRightTable = tableRange.startOffset + cloneLeftTableText.length + 1
        editor.caretModel.moveToOffset(cloneStartRightTable)
        editor.selectionModel.setSelection(cloneStartRightTable, cloneStartRightTable + cloneRightTableText.length - 2)
    }

    private fun deleteValueGroups(
        cloneRightHeaderLine: ImpexHeaderLine,
        canDelete: (Int) -> Boolean
    ) {
        cloneRightHeaderLine.valueLines
            .flatMap { it.valueGroupList }
            .filter { canDelete(it.columnNumber) }
            .forEach { it.delete() }
    }

    private fun deleteHeaderParams(
        cloneRightHeaderLine: ImpexHeaderLine,
        canDelete: (Int) -> Boolean
    ) {
        cloneRightHeaderLine.fullHeaderParameterList
            .filter { canDelete(it.columnNumber) }
            .forEach {
                PsiTreeUtilExt.getPrevSiblingOfElementType(it, ImpexTypes.MULTILINE_SEPARATOR)
                    ?.delete()
                PsiTreeUtilExt.getPrevSiblingOfElementType(it, ImpexTypes.PARAMETERS_SEPARATOR)
                    ?.delete()
                it.delete()
            }
    }

}
