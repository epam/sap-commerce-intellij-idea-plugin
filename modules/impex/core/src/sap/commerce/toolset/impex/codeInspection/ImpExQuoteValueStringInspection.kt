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
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.impex.psi.references.ImpExTSAttributeReference
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult

class ImpExQuoteValueStringInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WEAK_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : ImpExVisitor() {
        override fun visitValue(value: ImpExValue) {
            val trimmedText = value.text.trim()
            if (trimmedText.startsWith('\"')) return
            if (trimmedText.isBlank()) return

            val valueGroup = value.valueGroup ?: return

            valueGroup
                .fullHeaderParameter
                ?.anyHeaderParameterName
                ?.reference
                ?.asSafely<ImpExTSAttributeReference>()
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.asSafely<AttributeResolveResult>()
                ?.meta
                ?.takeIf {
                    "localized:java.lang.String" == it.type
                        || "java.lang.String" == it.type && !it.modifiers.isUnique
                }
                ?: return

            holder.registerProblem(
                value,
                i18n("hybris.inspections.impex.ImpExQuoteValueStringInspection.key"),
                ProblemHighlightType.WEAK_WARNING,
                LocalFix(value)
            )
        }

        private class LocalFix(element: PsiElement) : LocalQuickFixOnPsiElement(element) {

            override fun getFamilyName() = "[y] Quote value string"
            override fun getText() = "Wrap unquoted value string in quotes"

            override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
                val newValue = startElement.text.replace("\"", "\"\"")

                val newElement = ImpExElementFactory.createFile(
                    project, """
                        UPDATE Title;name;
                        ; "$newValue"

                """.trimIndent()
                )
                    .childrenOfType<ImpExValueLine>()
                    .flatMap { it.valueGroupList }
                    .mapNotNull { it.value }
                    .lastOrNull()
                    ?: return

                startElement.replace(newElement)
            }
        }
    }
}