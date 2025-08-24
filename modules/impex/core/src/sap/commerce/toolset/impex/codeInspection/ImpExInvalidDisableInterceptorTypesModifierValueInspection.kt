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
import com.intellij.psi.PsiElementVisitor
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.constants.InterceptorType
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeValue
import sap.commerce.toolset.impex.psi.ImpExVisitor

class ImpExInvalidDisableInterceptorTypesModifierValueInspection : LocalInspectionTool() {

    private val allowedValues = InterceptorType.entries.map { it.code }

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpExVisitor() {

        override fun visitAnyAttributeValue(element: ImpExAnyAttributeValue) {
            if (TypeModifier.getModifier(element) != TypeModifier.DISABLE_INTERCEPTOR_TYPES) return

            val text = element.text

            if (text in allowedValues) return

            holder.registerProblem(
                element,
                i18n(
                    "hybris.inspections.impex.ImpExInvalidDisableInterceptorTypesModifierValueInspection.key",
                    text,
                    allowedValues.joinToString { "'$it'" },
                ),
                ProblemHighlightType.ERROR
            )
        }
    }
}