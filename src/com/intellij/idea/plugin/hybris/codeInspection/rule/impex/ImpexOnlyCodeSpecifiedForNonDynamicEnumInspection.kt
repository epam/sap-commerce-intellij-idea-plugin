/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.codeInspection.fix.ImpexDeleteStatementQuickFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class ImpexOnlyCodeSpecifiedForNonDynamicEnumInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexOnlyCodeSpecifiedForNonDynamicEnumInspection(holder)

    private class ImpexOnlyCodeSpecifiedForNonDynamicEnumInspection(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitHeaderTypeName(parameter: ImpexHeaderTypeName) {
            val typeName = parameter.references.firstOrNull()
                ?.element
                ?.text
                ?: return

            TSMetaModelAccess.getInstance(parameter.project)
                .findMetaEnumByName(typeName)
                ?.takeUnless { it.isDynamic }
                ?: return

            PsiTreeUtil.getParentOfType(parameter, ImpexHeaderLine::class.java)
                ?.fullHeaderParameterList
                ?.takeIf { it.size == 1 }
                ?.firstOrNull()
                ?.firstChild
                ?.references
                ?.firstOrNull()
                ?.element
                ?.text
                ?.equals(CODE)
                ?: return

            problemsHolder.registerProblem(
                parameter,
                message("hybris.inspections.impex.ImpexOnlyCodeSpecifiedForNonDynamicEnumInspection.key", CODE),
                ProblemHighlightType.WARNING,
                ImpexDeleteStatementQuickFix(
                    parameter = parameter,
                    elementName = typeName,
                )
            )
        }

        companion object {
            const val CODE = "code"
        }
    }
}