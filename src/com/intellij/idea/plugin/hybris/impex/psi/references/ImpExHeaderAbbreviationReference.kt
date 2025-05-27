/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.lang.refactoring.ImpExPsiElementManipulator
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class ImpExHeaderAbbreviationReference(owner: ImpexAnyHeaderParameterName) : PsiReferenceBase.Poly<PsiElement?>(owner, false) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun calculateDefaultRangeInElement() = TextRange.from(0, element.textLength)

    override fun handleElementRename(newElementName: String) = ImpExPsiElementManipulator().handleContentChange(element, rangeInElement, newElementName)

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExHeaderAbbreviationReference>>("HYBRIS_IMPEXHEADERABBREVIATIONREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExHeaderAbbreviationReference> { ref ->
            val element = ref.element
            val value = ref.value
            val currentText = value.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)

            val result = PropertyService.getInstance(element.project)
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
                ?.psiElement
                ?.let { PsiElementResolveResult.createResults(it) }
                ?.let { PsiUtils.getValidResults(it) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}
