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
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeValue
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExHeaderTypeName
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase

class ImpExUnknownTypeNameInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExHeaderTypeVisitor(holder)

    private class ImpExHeaderTypeVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitHeaderTypeName(parameter: ImpExHeaderTypeName) {
            validateReference(parameter)
        }

        override fun visitAnyAttributeValue(element: ImpExAnyAttributeValue) {
            if (TypeModifier.getModifier(element) != TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES) return

            validateReference(element)
        }

        private fun validateReference(parameter: PsiElement) {
            if (parameter.firstChild is ImpExDocumentIdUsage) return

            val firstReference = parameter.references.firstOrNull() ?: return
            if (firstReference !is TSReferenceBase<*>) return

            val result = firstReference.multiResolve(false)
            if (result.isNotEmpty()) return

            problemsHolder.registerProblem(
                parameter,
                i18n("hybris.inspections.UnknownTypeNameInspection.key", parameter.text),
                ProblemHighlightType.ERROR
            )
        }
    }
}