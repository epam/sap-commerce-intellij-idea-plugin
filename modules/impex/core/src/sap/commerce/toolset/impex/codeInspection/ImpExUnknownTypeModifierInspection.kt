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
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeName
import sap.commerce.toolset.impex.psi.ImpExFullHeaderType
import sap.commerce.toolset.impex.psi.ImpExVisitor

class ImpExUnknownTypeModifierInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WEAK_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpExTypeModifierVisitor(holder)

    private class ImpExTypeModifierVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {

        override fun visitAnyAttributeName(attribute: ImpExAnyAttributeName) {
            PsiTreeUtil.getParentOfType(attribute, ImpExFullHeaderType::class.java) ?: return
            if (TypeModifier.getModifier(attribute) != null) return

            problemsHolder.registerProblem(
                attribute,
                i18n("hybris.inspections.impex.ImpexUnknownTypeModifierInspection.key", attribute.text),
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
            )
        }

    }
}