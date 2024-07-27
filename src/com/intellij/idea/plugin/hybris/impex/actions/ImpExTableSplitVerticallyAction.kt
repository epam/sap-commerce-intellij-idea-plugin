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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.forEachWithProgress
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.childrenOfType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImpExTableSplitVerticallyAction : AbstractImpExTableColumnAction() {

    private val commandName = "Split Table"
    private val groupID = "action.sap.cx.impex.table.split"

    init {
        with(templatePresentation) {
            text = commandName
            description = "Split current table vertically"
            icon = HybrisIcons.ImpEx.Actions.SPLIT_TABLE_VERTICALLY
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
        currentThreadCoroutineScope().launch {
            val headerParameter =
                readAction {
                    if (!psiFile.isValid) return@readAction null

                    when (element) {
                        is ImpexFullHeaderParameter -> element
                        is ImpexValueGroup -> element.fullHeaderParameter
                            ?: return@readAction null

                        else -> return@readAction null
                    }
                } ?: return@launch

            val headerLine = readAction { headerParameter.headerLine } ?: return@launch

            val columnNumber = readAction { headerParameter.columnNumber }
            val headerParameters = readAction { headerLine.fullHeaderParameterList.size }

            // collect numbers of all unique columns
            val uniqueColumns = readAction {
                headerLine.uniqueFullHeaderParameters
                    .map { it.columnNumber }
            }

            // calculate table range
            val tableRange = readAction { headerLine.tableRange }

            // let's start cloning! left
            val cloneTableLeft = readAction {
                ImpExElementFactory.createFile(
                    project,
                    psiFile.text.substring(tableRange.startOffset, tableRange.endOffset)
                )
            }

            // grab header lines from both cloned future left and right tables
            val cloneLeftHeaderLine = readAction { cloneTableLeft.childrenOfType<ImpexHeaderLine>().firstOrNull() }
                ?: return@launch
            val valueLines = readAction { cloneLeftHeaderLine.valueLines }

            // before the actual split, we have to ensure that we have enough value groups
            val addValueGroups = valueLines.associateWith {
                val valueGroups = readAction { it.valueGroupList }
                readAction { headerParameters - valueGroups.size }
            }
                .filter { it.value > 0 }

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Adding missing value groups before splitting") {
                        addValueGroups.entries.forEachWithProgress {
                            WriteCommandAction.runWriteCommandAction(
                                project, commandName, groupID,
                                { it.key.addValueGroups(it.value) },
                                psiFile
                            )
                        }
                    }
                }
            }

            // and clone right
            val cloneTableRight = readAction { cloneTableLeft.copy() }
            val cloneRightHeaderLine = readAction { cloneTableRight.childrenOfType<ImpexHeaderLine>().firstOrNull() }
                ?: return@launch

            val elementsToRemove = mutableListOf<PsiElement>()

            // before deletion of the header params, we have to remove value groups
            // delete value groups from the left table
            collectValueGroups(cloneLeftHeaderLine) {
                it >= columnNumber && !uniqueColumns.contains(it)
            }.also { elementsToRemove.addAll(it) }

            // and delete value groups from the right table
            collectValueGroups(cloneRightHeaderLine) {
                it < columnNumber && !uniqueColumns.contains(it)
            }.also { elementsToRemove.addAll(it) }

            // delete header param from the left table
            collectHeaderParams(cloneLeftHeaderLine) {
                it >= columnNumber && !uniqueColumns.contains(it)
            }.also { elementsToRemove.addAll(it) }

            // delete header param from the right table
            collectHeaderParams(cloneRightHeaderLine) {
                it < columnNumber && !uniqueColumns.contains(it)
            }.also { elementsToRemove.addAll(it) }

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Splitting table by '${headerParameter.text}' column") {
                        elementsToRemove.forEachWithProgress {
                            WriteCommandAction.runWriteCommandAction(
                                project, commandName, groupID,
                                { it.delete() },
                                psiFile
                            )
                        }
                    }
                }
            }

            // perform actual modification of the document, also inject a few new lines for the right cloned table
            val cloneLeftTableText = readAction { cloneTableLeft.text }
            val cloneRightTableText = readAction { "\n" + cloneTableRight.text }

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Splitting table by '${headerParameter.text}' column") {
                        WriteCommandAction.runWriteCommandAction(
                            project, commandName, groupID,
                            {
                                editor.document.replaceString(tableRange.startOffset, tableRange.endOffset, cloneLeftTableText)
                                editor.document.insertString(tableRange.startOffset + cloneLeftTableText.length, cloneRightTableText)
                            },
                            psiFile
                        )
                    }
                }

                val cloneStartRightTable = tableRange.startOffset + cloneLeftTableText.length + 1

                editor.caretModel.moveToOffset(cloneStartRightTable)
                editor.selectionModel.setSelection(cloneStartRightTable, cloneStartRightTable + cloneRightTableText.length - 2)
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            }
        }
    }

    private fun canProcess(headerLine: ImpexHeaderLine?): Boolean {
        headerLine ?: return false
        return headerLine.fullHeaderParameterList.size != headerLine.uniqueFullHeaderParameters.size
    }

    private suspend fun collectValueGroups(
        cloneRightHeaderLine: ImpexHeaderLine,
        canDelete: (Int) -> Boolean
    ) = readAction {
        cloneRightHeaderLine.valueLines
            .flatMap { it.valueGroupList }
            .filter { canDelete(it.columnNumber) }
    }

    private suspend fun collectHeaderParams(
        cloneRightHeaderLine: ImpexHeaderLine,
        canDelete: (Int) -> Boolean
    ) = readAction {
        cloneRightHeaderLine.fullHeaderParameterList
            .filter { canDelete(it.columnNumber) }
            .flatMap {
                listOfNotNull(
                    PsiTreeUtilExt.getPrevSiblingOfElementType(it, ImpexTypes.MULTILINE_SEPARATOR),
                    PsiTreeUtilExt.getPrevSiblingOfElementType(it, ImpexTypes.PARAMETERS_SEPARATOR),
                    it
                )
            }
    }

}
