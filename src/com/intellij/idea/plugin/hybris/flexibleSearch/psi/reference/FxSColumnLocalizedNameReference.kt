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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnLocalizedName
import com.intellij.idea.plugin.hybris.impex.utils.ProjectPropertiesUtils
import com.intellij.idea.plugin.hybris.properties.PropertiesService
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.pom.references.PomService
import com.intellij.psi.*
import com.intellij.psi.util.*


class FxSColumnLocalizedNameReference(owner: FlexibleSearchColumnLocalizedName) : PsiReferenceBase.Poly<FlexibleSearchColumnLocalizedName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val language = element.text.trim()
        return TextRange.from(element.text.indexOf(language), language.length)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun getVariants() = PropertiesService.getInstance(element.project).getLanguages()
        .map { FxSLookupElementFactory.buildLanguage(it) }
        .toTypedArray()

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSColumnLocalizedNameReference>>("HYBRIS_FXS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSColumnLocalizedNameReference> { ref ->
            val result: Array<ResolveResult> = ProjectPropertiesUtils.findMacroProperty(ref.element.project, HybrisConstants.PROPERTY_LANG_PACKS)
                ?.let {
                    val property = it as? PsiElement
                        ?: PomService.convertToPsi(it as PsiTarget)

                    PsiElementResolveResult(property)
                }
                ?.let { arrayOf(it) }
                ?: emptyArray()
            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}