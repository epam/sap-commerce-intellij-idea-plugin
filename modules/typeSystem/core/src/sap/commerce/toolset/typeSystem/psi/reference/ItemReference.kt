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

package sap.commerce.toolset.typeSystem.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.codeInsight.completion.TSCompletionService
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import sap.commerce.toolset.typeSystem.psi.reference.result.ItemResolveResult

open class ItemReference(element: PsiElement) : TSReferenceBase<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement(): TextRange =
        if (element.textLength == 0) super.calculateDefaultRangeInElement()
        else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { getValidResults(it) }

    override fun getVariants(): Array<LookupElementBuilder> = TSCompletionService.getInstance(element.project)
        .getCompletions(TSMetaType.META_ITEM)
        .toTypedArray()

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ItemReference> { ref ->
            val project = ref.project
            val metaModelAccess = TSMetaModelAccess.getInstance(project)

            val name = ref.value
            val result: Array<ResolveResult> = metaModelAccess.findMetaItemByName(name)
                ?.declarations
                ?.map { ItemResolveResult(it) }
                ?.toTypedArray()
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                TSModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}
