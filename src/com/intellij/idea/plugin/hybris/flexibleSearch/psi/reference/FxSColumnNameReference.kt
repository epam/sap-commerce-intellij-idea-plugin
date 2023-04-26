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

import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.FlexibleSearchCompletionContributor
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FxSPsiUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference.result.FxSColumnAliasNameResolveResult
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxSColumnNameReference(owner: FlexibleSearchColumnName) : PsiReferenceBase.Poly<FlexibleSearchColumnName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSPsiUtils.getTableAliasName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun handleElementRename(newElementName: String) = element.setName(newElementName)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun getVariants() = PsiTreeUtil.getPrevSiblingOfType(element, FlexibleSearchSelectedTableName::class.java)
        ?.reference
        ?.resolve()
        ?.parent
        ?.let { PsiTreeUtil.findChildrenOfType(it, FlexibleSearchColumnAliasName::class.java) }
        ?.map { FxSLookupElementFactory.build(it) }
        ?.toTypedArray()
        ?: emptyArray()

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSColumnNameReference>>("HYBRIS_FXS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSColumnNameReference> { ref ->
            val lookingForName = ref.element.text
                .replace(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")
                .trim()

            val result: Array<ResolveResult> = PsiTreeUtil.getPrevSiblingOfType(ref.element, FlexibleSearchSelectedTableName::class.java)
                ?.reference
                ?.resolve()
                ?.parent
                ?.let {
                    PsiTreeUtil.findChildrenOfType(it, FlexibleSearchColumnAliasName::class.java)
                        .firstOrNull { alias -> alias.text.trim() == lookingForName }
                }
                ?.let { arrayOf(FxSColumnAliasNameResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}