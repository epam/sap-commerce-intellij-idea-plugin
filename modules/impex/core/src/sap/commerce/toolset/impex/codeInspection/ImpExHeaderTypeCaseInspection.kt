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

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.codeInspection.fix.ImpExCorrectHeaderTypeNameQuickFix
import sap.commerce.toolset.impex.psi.ImpExHeaderTypeName
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class ImpExHeaderTypeCaseInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpExVisitor() {

        override fun visitHeaderTypeName(element: ImpExHeaderTypeName) {
            if (element.macroUsageDecList.isNotEmpty() || element.possibleMacroUsageDecList.isNotEmpty()) return

            val typeName = element.text ?: return
            val canonicalName = TSMetaModelAccess.getInstance(element.project)
                .findMetaClassifierByName(typeName)
                ?.name
                ?: return
            if (canonicalName == typeName) return

            holder.registerProblem(
                element,
                i18n("hybris.inspections.impex.ImpExHeaderTypeCaseInspection.key", typeName, canonicalName),
                ProblemHighlightType.WEAK_WARNING,
                ImpExCorrectHeaderTypeNameQuickFix(element, canonicalName)
            )
        }
    }
}
