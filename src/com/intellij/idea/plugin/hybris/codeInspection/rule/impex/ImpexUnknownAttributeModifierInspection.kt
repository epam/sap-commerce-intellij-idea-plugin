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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class ImpexUnknownAttributeModifierInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexTypeModifierVisitor(holder)

    private class ImpexTypeModifierVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {

        override fun visitAnyAttributeName(attribute: ImpexAnyAttributeName) {
            PsiTreeUtil.getParentOfType(attribute, ImpexFullHeaderParameter::class.java) ?: return

            if (AttributeModifier.getByModifierName(attribute.text) == null) {
                problemsHolder.registerProblem(
                    attribute,
                    HybrisI18NBundleUtils.message("hybris.inspections.impex.ImpexUnknownAttributeModifierInspection.key", attribute.text),
                    ProblemHighlightType.WARNING
                )
            }
        }

    }

}