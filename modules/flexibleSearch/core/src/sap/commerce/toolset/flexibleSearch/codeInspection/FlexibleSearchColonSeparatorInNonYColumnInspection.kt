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
package sap.commerce.toolset.flexibleSearch.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.flexibleSearch.codeInspection.fix.FlexibleSearchReplaceColumnSeparatorQuickFix
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnSeparator
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchResultColumn
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchVisitor

class FlexibleSearchColonSeparatorInNonYColumnInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.GENERIC_SERVER_ERROR_OR_WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = FlexibleSearchPsiVisitor(holder)

    private class FlexibleSearchPsiVisitor(private val problemsHolder: ProblemsHolder) : FlexibleSearchVisitor() {

        override fun visitColumnSeparator(element: FlexibleSearchColumnSeparator) {
            element.parentOfType<FlexibleSearchResultColumn>() ?: return

            if (element.firstLeaf().elementType != FlexibleSearchTypes.COLON) return

            problemsHolder.registerProblem(
                element,
                "Test",
                ProblemHighlightType.GENERIC_ERROR,
                FlexibleSearchReplaceColumnSeparatorQuickFix(
                    element = element,
                    oldValue = ":",
                    newValue = "."
                )
            )
        }
    }
}

