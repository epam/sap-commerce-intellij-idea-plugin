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
package sap.commerce.toolset.impex.psi.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider
import sap.commerce.toolset.impex.psi.ImpExParameter
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.codeInsight.completion.TSCompletionService
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.*
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase
import sap.commerce.toolset.typeSystem.psi.reference.result.*

class ImpExFunctionTSItemReference(owner: ImpExParameter) : TSReferenceBase<ImpExParameter>(owner), HighlightedReference {

    override fun calculateDefaultRangeInElement(): TextRange = element.inlineTypeName
        ?.let { TextRange.from(0, it.length) }
        ?: super.calculateDefaultRangeInElement()

    override fun getVariants(): Array<LookupElement> = TSCompletionService.getInstance(element.project)
        .getSubTypeCompletions(element.project, element.referenceItemTypeName, element.inlineTypeName)
        .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExFunctionTSItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExFunctionTSItemReference> { ref ->
            val lookingForName = ref.value.trim()
            val project = ref.project

            val result: Array<ResolveResult> = TSMetaModelAccess.getInstance(project).findMetaClassifierByName(lookingForName)
                ?.declarations
                ?.mapNotNull {
                    when (it) {
                        is TSMetaItem -> ItemResolveResult(it)
                        is TSMetaEnum -> EnumResolveResult(it)
                        is TSMetaRelation -> RelationResolveResult(it)
                        is TSMetaMap -> MapResolveResult(it)
                        is TSMetaCollection -> CollectionResolveResult(it)
                        else -> null
                    }
                }
                ?.toTypedArray()
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                TSModificationTracker.getInstance(project)
            )
        }
    }
}
