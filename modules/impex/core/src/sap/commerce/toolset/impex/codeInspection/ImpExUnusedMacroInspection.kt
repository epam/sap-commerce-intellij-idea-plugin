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
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.IProperty
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.codeInspection.fix.ImpExDeleteMacroDeclarationFix
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.ImpExHeaderAbbreviationReference

class ImpExUnusedMacroInspection : LocalInspectionTool() {

    private val expectedMacrosByAbbreviations = mutableSetOf<String>()

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = UnknownMacrosVisitor(holder, expectedMacrosByAbbreviations)

    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        expectedMacrosByAbbreviations.clear()

        PsiTreeUtil.findChildrenOfType(session.file, ImpExAnyHeaderParameterName::class.java)
            .forEach { parameter ->
                parameter.reference.asSafely<ImpExHeaderAbbreviationReference>()
                    ?.resolve()
                    ?.asSafely<IProperty>()
                    ?.value
                    ?.split("...", " ", "'", "\\\\", "]", "[", ":")
                    ?.asSequence()
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.filter { it.startsWith('$') }
                    ?.forEach { expectedMacrosByAbbreviations.add(it) }
            }
    }

    private class UnknownMacrosVisitor(
        private val problemsHolder: ProblemsHolder,
        private val expectedMacrosByAbbreviations: MutableSet<String>
    ) : ImpExVisitor() {

        override fun visitMacroNameDec(macro: ImpExMacroNameDec) {
            val macroName = macro.text
            if (expectedMacrosByAbbreviations.contains(macroName)) return

            val macroDeclaration = macro.parentOfType<ImpExMacroDeclaration>()
                ?: return
            val endElement = macroDeclaration.nextLeaf({ it.elementType == ImpExTypes.CRLF })
            val file = problemsHolder.file

            val scope: SearchScope = LocalSearchScope(file)
            val usages = ReferencesSearch.search(macro, scope)
                .findFirst()

            if (usages == null) {
                problemsHolder.registerProblem(
                    macro,
                    i18n("hybris.inspections.impex.ImpExUnusedMacroInspection.key", macroName),
                    ProblemHighlightType.WARNING,
                    ImpExDeleteMacroDeclarationFix(
                        macroName,
                        macroDeclaration,
                        endElement,
                    )
                )
            }
        }
    }
}
