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

package sap.commerce.toolset.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpexAnyAttributeValue
import sap.commerce.toolset.impex.psi.ImpexDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpexHeaderTypeName
import sap.commerce.toolset.impex.psi.ImpexVisitor
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase

class ImpexUnknownTypeNameInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderTypeVisitor(holder)

    private class ImpexHeaderTypeVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitHeaderTypeName(parameter: ImpexHeaderTypeName) {
            validateReference(parameter)
        }

        override fun visitAnyAttributeValue(element: ImpexAnyAttributeValue) {
            if (TypeModifier.getModifier(element) != TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES) return

            validateReference(element)
        }

        private fun validateReference(parameter: PsiElement) {
            if (parameter.firstChild is ImpexDocumentIdUsage) return

            val firstReference = parameter.references.firstOrNull() ?: return
            if (firstReference !is TSReferenceBase<*>) return

            val result = firstReference.multiResolve(false)
            if (result.isNotEmpty()) return

            problemsHolder.registerProblem(
                parameter,
                message("hybris.inspections.UnknownTypeNameInspection.key", parameter.text),
                ProblemHighlightType.ERROR
            )
        }
    }
}