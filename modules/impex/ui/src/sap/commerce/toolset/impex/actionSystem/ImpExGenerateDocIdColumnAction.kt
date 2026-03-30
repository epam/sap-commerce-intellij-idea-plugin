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

import com.intellij.openapi.actionSystem.AnActionEvent
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
import com.intellij.util.asSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.psi.ImpExDocIdGenerationContext
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExValueLine

class ImpExGenerateDocIdColumnAction : AbstractImpExTableAction() {

    init {
        with(templatePresentation) {
            text = "Generate Document Id"
            description = "Define new reference column based on unique columns"
            icon = HybrisIcons.ImpEx.Actions.GENERATE_DOC_ID
        }
    }

    override fun update(e: AnActionEvent, suitableElement: PsiElement, actionAllowed: Boolean) {
        if (!actionAllowed) {
            val headerLine = suitableElement.asSafely<ImpExHeaderLine>() ?: return
            val docIds = headerLine.documentIdDeclarations
                .map { it.text.trim() }
                .joinToString(", ") { "'$it'" }
            e.presentation.text = "Declared Document Id: $docIds"
        } else {
            e.presentation.text = "Generate Document Id"
        }
    }

    override fun performAction(project: Project, editor: Editor, psiFile: PsiFile, element: PsiElement) {
        currentThreadCoroutineScope().launch {
            if (readAction { !psiFile.isValid }) return@launch

            val headerLine = element.asSafely<ImpExHeaderLine>()
                ?: return@launch

            val includedColumnIds = readAction {
                headerLine.uniqueFullHeaderParameters
                    .map { it.columnNumber }
                    .takeIf { it.isNotEmpty() }
            } ?: return@launch

            val rangeAwareContent = readAction {
                headerLine.generateDocId(
                    ImpExDocIdGenerationContext(
                        includedColumnIds = includedColumnIds,
                    )
                )
            } ?: return@launch

            withContext(Dispatchers.EDT) {
                PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                    runWithModalProgressBlocking(project, "Generating &docId") {
                        WriteCommandAction.runWriteCommandAction(project) {
                            PostprocessReformattingAspect.getInstance(project).disablePostprocessFormattingInside {
                                val textRange = rangeAwareContent.textRange
                                val newContent = rangeAwareContent.content

                                psiFile.fileDocument.replaceString(textRange.startOffset, textRange.endOffset, newContent)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getSuitableElement(e: AnActionEvent, element: PsiElement) = PsiTreeUtil
        .getParentOfType(element, ImpExHeaderLine::class.java, ImpExValueLine::class.java)
        ?.let { it.asSafely<ImpExHeaderLine>() ?: it.asSafely<ImpExValueLine>()?.headerLine }

    override fun isActionAllowed(project: Project, editor: Editor, e: AnActionEvent, suitableElement: PsiElement) = suitableElement
        .asSafely<ImpExHeaderLine>()
        ?.let { !it.hasDocumentIdDec() }
        ?: false

}
