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

package com.intellij.idea.plugin.hybris.codeInspection.rule.flexibleSearch

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnReference
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchVisitor
import com.intellij.idea.plugin.hybris.flexibleSearch.references.FxsTSAttributeReference
import com.intellij.psi.PsiElementVisitor

class FxsUnknownTypeAttributeInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : FlexibleSearchVisitor() {

        override fun visitColumnReference(parameter: FlexibleSearchColumnReference) {
            val references = parameter.references
            if (references.isEmpty()) return

            val firstReference = references.firstOrNull() ?: return
            if (firstReference !is FxsTSAttributeReference) return

            val result = firstReference.multiResolve(false)
            if (result.isNotEmpty()) return
            val itemType = firstReference.getType()
                ?: "unknown"

            holder.registerProblem(
                parameter,
                HybrisI18NBundleUtils.message("hybris.inspections.UnknownTypeAttributeInspection.key", parameter.text, itemType),
                ProblemHighlightType.ERROR
            )
        }
    }
}
