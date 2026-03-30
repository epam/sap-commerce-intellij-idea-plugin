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

package sap.commerce.toolset.impex.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.psi.ImpExDocIdGenerationContext
import sap.commerce.toolset.impex.psi.ImpExHeaderTypeName
import sap.commerce.toolset.impex.psi.ImpExVisitor

class ImpExMissingDocumentIdColumnInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WEAK_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    private class Visitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitHeaderTypeName(element: ImpExHeaderTypeName) {
            val headerLine = element.headerLine ?: return
            if (headerLine.hasDocumentIdDec()) return

            val uniqueColumnNumbers = headerLine.uniqueFullHeaderParameters
                .map { it.columnNumber }
                .takeIf { it.isNotEmpty() }
                ?: return

            val typeName = element.text.trim()

            problemsHolder.registerProblem(
                element,
                "Declare documentId for '$typeName'",
                ProblemHighlightType.INFORMATION,
                LocalFix(element, typeName)
            )
        }

        private class LocalFix(
            el: ImpExHeaderTypeName,
            private val typeName: String
        ) : LocalQuickFixOnPsiElement(el) {

            override fun getFamilyName() = "[y] DocId column is not declared "
            override fun getText() = "Generate &docId column for '$typeName'"
            override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

            override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
                val typeName = startElement.asSafely<ImpExHeaderTypeName>() ?: return
                val headerLine = typeName.headerLine ?: return
                val uniqueColumnNumbers = headerLine.uniqueFullHeaderParameters
                    .map { it.columnNumber }
                    .takeIf { it.isNotEmpty() }
                    ?: return

                val rangeAwareContent = headerLine.generateDocId(
                    ImpExDocIdGenerationContext(
                        includedColumnIds = uniqueColumnNumbers
                    )
                ) ?: return
                val textRange = rangeAwareContent.textRange
                val newContent = rangeAwareContent.content

                file.fileDocument.replaceString(textRange.startOffset, textRange.endOffset, newContent)
            }
        }
    }
}

