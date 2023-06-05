/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.codeInspection.fix.ImpexUpdateHeaderModeQuickFix
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class ImpexOnlyUpdateAllowedForNonDynamicEnumInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexOnlyUpdateAllowedForNonDynamicEnumVisitor(holder)

    private class ImpexOnlyUpdateAllowedForNonDynamicEnumVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitHeaderTypeName(parameter: ImpexHeaderTypeName) {
            val typeName = PsiTreeUtil.getParentOfType(parameter, ImpexHeaderLine::class.java)
                ?.fullHeaderType
                ?.headerTypeName
                ?.text
                ?: return

            val meta = TSMetaModelAccess.getInstance(parameter.firstChild.project).findMetaEnumByName(typeName)
                ?.takeUnless { it.isDynamic }
                ?: return

            val mode = PsiTreeUtil.getParentOfType(parameter, ImpexHeaderLine::class.java)
                ?.anyHeaderMode
                ?: return

            val impexModeUpdate = HybrisConstants.IMPEX_MODE_UPDATE

            val modeName = mode.text
                ?.uppercase()
                ?.takeUnless { it == impexModeUpdate }
                ?: return

            val enumName = meta.name ?: typeName

            problemsHolder.registerProblem(
                mode,
                message("hybris.inspections.impex.ImpexOnlyUpdateAllowedForNonDynamicEnumInspection.key", modeName, enumName, impexModeUpdate),
                ProblemHighlightType.ERROR,
                ImpexUpdateHeaderModeQuickFix(
                    headerMode = mode,
                    elementName = enumName,
                    headerModeToUpdateWith = impexModeUpdate)
            )
        }
    }
}