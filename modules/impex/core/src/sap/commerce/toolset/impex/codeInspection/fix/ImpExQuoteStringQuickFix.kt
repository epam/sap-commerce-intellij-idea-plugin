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

package sap.commerce.toolset.impex.codeInspection.fix

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import sap.commerce.toolset.impex.psi.*

class ImpExQuoteStringQuickFix(
    element: PsiElement,
    private val presentationText: String,
    private val overridePreviewInfo: IntentionPreviewInfo? = null
) : LocalQuickFixOnPsiElement(element), LowPriorityAction {

    override fun getFamilyName() = "[y] Quote value string"
    override fun getText() = presentationText
    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = overridePreviewInfo
        ?: super.generatePreview(project, previewDescriptor)

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        when (startElement) {
            is ImpExValue -> quoteValue(file, startElement, project)
            is ImpExAnyHeaderParameterName -> startElement
                .parentOfType<ImpExFullHeaderParameter>()
                ?.valueGroups
                ?.mapNotNull { it.value }
                ?.filter { it.isQuotable }
                ?.reversed()
                ?.forEach { quoteValue(file, it, project) }
        }
    }

    private fun quoteValue(file: PsiFile, value: ImpExValue, project: Project) {
        val newValue = value.text
            .replace("\"", "\"\"")
            .replace("\\\n", "")
            .replace("\\\r", "")
            .replace("\\\n\r", "")

        val newElement = ImpExElementFactory.createFile(
            project, """
                            UPDATE Title;name
                            ; "$newValue"
    
                    """.trimIndent()
        )
            .childrenOfType<ImpExValueLine>()
            .flatMap { it.valueGroupList }
            .mapNotNull { it.value }
            .lastOrNull()
            ?: return

        file.fileDocument.replaceString(value.startOffset, value.endOffset, newElement.text)
    }
}