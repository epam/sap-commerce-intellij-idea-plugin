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
import com.intellij.codeInspection.*
import com.intellij.lang.properties.IProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.ImpExHeaderAbbreviationReference

class ImpExIncompleteHeaderAbbreviationUsageInspection : LocalInspectionTool() {

    private val cachedMacros = mutableSetOf<String>()
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExHeaderLineVisitor(holder, cachedMacros)
    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        cachedMacros.clear()

        PsiTreeUtil.findChildrenOfAnyType(session.file, ImpExMacroDeclaration::class.java)
            .map { it.firstChild.text }
            .let { cachedMacros.addAll(it) }
    }

    private class ImpExHeaderLineVisitor(
        private val problemsHolder: ProblemsHolder,
        private val cachedMacros: Set<String>
    ) : ImpExVisitor() {

        override fun visitParameter(parameter: ImpExParameter) = visit(parameter)
        override fun visitAnyHeaderParameterName(parameter: ImpExAnyHeaderParameterName) = visit(parameter)

        private fun visit(element: PsiElement) {
            val reference = element.reference.asSafely<ImpExHeaderAbbreviationReference>() ?: return

            val headerAbbreviationValue = reference
                .resolve()
                ?.asSafely<IProperty>()
                ?.value
                ?: return

            val missingExpectedMacros = headerAbbreviationValue.split("...", " ", "'", "\\\\", "]", "[", ":")
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .filter { it.startsWith('$') }
                .distinct()
                .filterNot { cachedMacros.contains(it) }
                .toList()
                .takeIf { it.isNotEmpty() }
                ?: return

            problemsHolder.registerProblemForReference(
                reference,
                ProblemHighlightType.WARNING,
                i18n("hybris.inspections.impex.ImpExIncompleteHeaderAbbreviationUsageInspection.key", element.text, missingExpectedMacros.joinToString()),
                *missingExpectedMacros
                    .map { LocalFix(element, it) }
                    .toTypedArray()
            )
        }

        private class LocalFix(
            element: PsiElement,
            private val macroName: String
        ) : LocalQuickFixOnPsiElement(element) {

            override fun getFamilyName() = "[y] Missing macro declarations"
            override fun getText() = "Add macro declaration '$macroName'"

            override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
                val firstLeaf = startElement
                    .parentOfType<ImpExHeaderLine>()
                    ?: return

                val snippetFile = ImpExElementFactory.createFile(
                    project, """
                    $macroName =  

                """.trimIndent()
                )

                file.addBefore(snippetFile, firstLeaf)
            }
        }
    }
}