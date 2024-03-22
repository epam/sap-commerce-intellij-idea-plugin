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
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.codeInspection.fix.impex.ImpexDeleteModifierFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.parentOfType

class ImpexLanguageModifierIsNotAllowedInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : ImpexVisitor() {

        override fun visitAnyAttributeName(psi: ImpexAnyAttributeName) {
            if (psi.text != AttributeModifier.LANG.modifierName) return

            val attribute = psi.parentOfType<ImpexAttribute>(false) ?: return
            val meta = psi.parentOfType<ImpexFullHeaderParameter>(false)
                ?.anyHeaderParameterName
                ?.reference
                ?.let { it as PsiPolyVariantReference }
                ?.multiResolve(false)
                ?.firstOrNull()
                ?.let { it as? AttributeResolveResult }
                ?.meta
                ?.takeUnless { it.isLocalized }
                ?: return

            holder.registerProblem(
                psi,
                message("hybris.inspections.impex.ImpexLanguageModifierIsNotAllowedInspection.key", meta.name),
                ImpexDeleteModifierFix(attribute)
            )
        }
    }
}
