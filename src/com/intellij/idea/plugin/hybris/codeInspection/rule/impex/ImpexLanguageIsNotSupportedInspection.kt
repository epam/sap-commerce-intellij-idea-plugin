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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.psi.util.PsiTreeUtil

class ImpexLanguageIsNotSupportedInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : ImpexVisitor() {

        override fun visitAnyAttributeValue(psi: ImpexAnyAttributeValue) {
            PsiTreeUtil.getPrevSiblingOfType(psi, ImpexAnyAttributeName::class.java)
                ?.takeIf { AttributeModifier.LANG.modifierName == it.text }
                ?: return

            val language = if (psi.firstChild is ImpexMacroUsageDec) {
                PsiTreeUtil.getNextSiblingOfType(psi.firstChild.reference
                    ?.resolve(), ImpexMacroValueDec::class.java)
                    ?.text
                    ?: psi.text
            } else {
                psi.text
            }
                .trim()

            val propertyService = PropertyService.getInstance(psi.project)
            val supportedLanguages = propertyService.getLanguages()

            if (propertyService.containsLanguage(language, supportedLanguages)) return

            holder.registerProblem(
                psi,
                message(
                    "hybris.inspections.language.unsupported",
                    language,
                    supportedLanguages.joinToString()
                )
            )
        }
    }
}
