/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.rename.manipulator.ImpexMacrosManipulator
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class ImpExHeaderAbbreviationReference(owner: ImpexAnyHeaderParameterName) : PsiReferenceBase.Poly<PsiElement?>(owner, false) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        findHeaderAbbreviation(element.project, value.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED))
            ?.psiElement
            ?.let { PsiElementResolveResult.createResults(it) }
            ?.let { PsiUtils.getValidResults(it) }
            ?: emptyArray()

    override fun calculateDefaultRangeInElement() = TextRange.from(0, element.textLength)

    private fun findHeaderAbbreviation(project: Project, currentText: String) = PropertyService.getInstance(project)
        ?.findAutoCompleteProperties(HybrisConstants.PROPERTY_IMPEX_HEADER_REPLACEMENT)
        ?.firstOrNull { property ->
            property.value
                ?.split("...")
                ?.takeIf { it.size == 2 }
                ?.firstOrNull()
                ?.trim()
                ?.replace("\\\\", "\\")
                ?.toRegex()
                ?.let { currentText.matches(it) }
                ?: false
        }

    override fun handleElementRename(newElementName: String) = ImpexMacrosManipulator().handleContentChange(element, rangeInElement, newElementName)

}
