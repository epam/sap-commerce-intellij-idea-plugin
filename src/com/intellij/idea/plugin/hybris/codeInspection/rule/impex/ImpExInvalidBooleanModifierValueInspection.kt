/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.psi.PsiElementVisitor

class ImpExInvalidBooleanModifierValueInspection : LocalInspectionTool() {

    private val booleanModifiers = setOf(
        AttributeModifier.UNIQUE,
        AttributeModifier.ALLOW_NULL,
        AttributeModifier.FORCE_WRITE,
        AttributeModifier.IGNORE_KEY_CASE,
        AttributeModifier.IGNORE_NULL,
        AttributeModifier.VIRTUAL,
        TypeModifier.BATCH_MODE,
        TypeModifier.SLD_ENABLED,
        TypeModifier.CACHE_UNIQUE,
        TypeModifier.IMPEX_LEGACY_MODE
    )
        .map { it.modifierName }

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpexVisitor() {

        override fun visitAnyAttributeValue(element: ImpexAnyAttributeValue) {
            val modifierName = element.anyAttributeName?.text ?: return
            if (modifierName !in booleanModifiers) return

            val text = element.text

            if (text in HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES) return

            holder.registerProblem(
                element,
                message(
                    "hybris.inspections.impex.ImpExInvalidBooleanModifierValueInspection.key",
                    modifierName,
                    text,
                ),
                ProblemHighlightType.ERROR
            )
        }
    }
}