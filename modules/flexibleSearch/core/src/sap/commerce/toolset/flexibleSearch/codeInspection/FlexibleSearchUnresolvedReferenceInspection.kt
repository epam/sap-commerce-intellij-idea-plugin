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
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import sap.commerce.toolset.codeInspection.verifyReferences
import sap.commerce.toolset.flexibleSearch.psi.*
import sap.commerce.toolset.i18n

class FlexibleSearchUnresolvedReferenceInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = FlexibleSearchPsiVisitor(holder)

    private class FlexibleSearchPsiVisitor(private val problemsHolder: ProblemsHolder) : FlexibleSearchVisitor() {

        override fun visitSelectedTableName(element: FlexibleSearchSelectedTableName) = problemsHolder.verifyReferences(element) {
            i18n("hybris.inspections.fxs.unresolved.tableAlias.key", canonicalText)
        }

        override fun visitDefinedTableName(element: FlexibleSearchDefinedTableName) = problemsHolder.verifyReferences(element) {
            i18n("hybris.inspections.fxs.unresolved.type.key", canonicalText)
        }

        override fun visitYColumnName(element: FlexibleSearchYColumnName) = problemsHolder.verifyReferences(element) {
            i18n("hybris.inspections.fxs.unresolved.attribute.key", canonicalText)
        }

        override fun visitColumnName(element: FlexibleSearchColumnName) = problemsHolder.verifyReferences(element) {
            i18n("hybris.inspections.fxs.unresolved.columnAlias.key", canonicalText)
        }
    }
}
