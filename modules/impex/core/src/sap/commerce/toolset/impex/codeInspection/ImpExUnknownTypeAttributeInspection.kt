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

package sap.commerce.toolset.impex.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.asSafely
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSAttributeReference
import sap.commerce.toolset.impex.psi.references.ImpExTSAttributeReference
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase

class ImpExUnknownTypeAttributeInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExHeaderLineVisitor(holder)

    private class ImpExHeaderLineVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitParameter(parameter: ImpExParameter) {
            if (parameter.firstChild is ImpExMacroUsageDec || parameter.firstChild is ImpExDocumentIdUsage) return

            parameter.references
                .find { it is ImpExFunctionTSAttributeReference }
                ?.asSafely<ImpExFunctionTSAttributeReference>()
                ?.takeIf { it.multiResolve(false).isEmpty() }
                ?.let { ref ->
                    parameter.itemTypeName
                        ?.let { registerProblem(ref, it) }
                }
                ?: return
        }

        override fun visitAnyHeaderParameterName(parameter: ImpExAnyHeaderParameterName) {
            if (parameter.firstChild is ImpExMacroUsageDec || parameter.firstChild is ImpExDocumentIdUsage) return

            parameter.references
                .find { it is ImpExTSAttributeReference }
                ?.asSafely<ImpExTSAttributeReference>()
                ?.takeIf { it.multiResolve(false).isEmpty() }
                ?.let { ref ->
                    parameter.headerItemTypeName
                        ?.text
                        ?.let { registerProblem(ref, it) }
                }
                ?: return
        }

        private fun registerProblem(reference: TSReferenceBase<out PsiElement>, typeName: String) {
            problemsHolder.registerProblemForReference(
                reference,
                ProblemHighlightType.ERROR,
                i18n("hybris.inspections.UnknownTypeAttributeInspection.key", reference.value, typeName),
            )
        }
    }
}