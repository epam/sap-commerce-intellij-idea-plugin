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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectCoreSelect
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FxSPsiUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference.result.FxSTableAliasNameResolveResult
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxSSelectedTableNameReference(owner: FlexibleSearchSelectedTableName) : PsiReferenceBase.Poly<FlexibleSearchSelectedTableName>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val originalType = element.text
        val type = FxSPsiUtils.getTableAliasName(element.text)
        return TextRange.from(originalType.indexOf(type), type.length)
    }

    override fun handleElementRename(newElementName: String) = element.setName(newElementName)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    override fun getVariants() = PsiTreeUtil.getParentOfType(element, FlexibleSearchSelectCoreSelect::class.java)
        ?.fromClause
        ?.let {
            PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java)
                .mapNotNull { tableAlias -> FxSLookupElementFactory.build(tableAlias) }
                .toTypedArray()
        }
        ?: emptyArray()

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxSSelectedTableNameReference>>("HYBRIS_FXS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxSSelectedTableNameReference> { ref ->
            val lookingForName = ref.element.text
                .replace(FlexibleSearchCompletionContributor.DUMMY_IDENTIFIER, "")
                .trim()

            val result: Array<ResolveResult> = PsiTreeUtil.getParentOfType(ref.element, FlexibleSearchSelectCoreSelect::class.java)
                ?.fromClause
                ?.let {
                    PsiTreeUtil.findChildrenOfType(it, FlexibleSearchTableAliasName::class.java)
                        .firstOrNull { alias -> alias.text.trim() == lookingForName }
                }
                ?.let { arrayOf(FxSTableAliasNameResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}