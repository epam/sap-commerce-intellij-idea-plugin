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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class ImpexPropertyReference(owner: ImpexMacroUsageDec) : PsiReferenceBase.Poly<ImpexMacroUsageDec>(owner, false) {

    override fun calculateDefaultRangeInElement() = element
        .configPropertyKey
        ?.let {
            TextRange.from(
                HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length,
                it.length
            )
        }
        ?: TextRange.from(
            HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length,
            element.textLength - HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length
        )

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val propertiesService = PropertyService.getInstance(element.project)
            ?: return emptyArray()

        return getPropertyKey()
            ?.let { propertiesService.findMacroProperty(it) }
            ?.let { PsiElementResolveResult.createResults(it.psiElement) }
            ?: ResolveResult.EMPTY_ARRAY
    }

    private fun getPropertyKey() = element.text
        .replace(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX, "")
        .takeUnless { it.isBlank() }

}