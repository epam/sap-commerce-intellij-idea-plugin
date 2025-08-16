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
import sap.commerce.toolset.impex.psi.ImpexDocumentIdDec
import sap.commerce.toolset.impex.psi.ImpexFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpexValueGroup
import sap.commerce.toolset.impex.psi.ImpexVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.HybrisI18NBundleUtils.message

class ImpexUniqueDocumentIdInspection : LocalInspectionTool() {

    override fun getDefaultLevel() = HighlightDisplayLevel.ERROR

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpexVisitor() {
        override fun visitDocumentIdDec(parameter: ImpexDocumentIdDec) {
            val impexFullHeaderParameter = parameter.parentOfType<ImpexFullHeaderParameter>() ?: return
            val set = HashSet<String>()

            impexFullHeaderParameter.valueGroups
                .forEach {
                    if (!set.add(it.text)) {
                        val qualifier = (it as ImpexValueGroup).value
                            ?.text
                            ?: it.text

                        holder.registerProblem(
                            it,
                            message("hybris.inspections.impex.ImpexUniqueDocumentIdInspection.key", qualifier, parameter.text),
                            ProblemHighlightType.ERROR
                        )
                    }
                }

        }
    }
}
