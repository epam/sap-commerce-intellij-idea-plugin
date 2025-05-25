/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaMap
import com.intellij.idea.plugin.hybris.system.type.model.MapType
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.MapResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.OrderingAttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider

class ImpexFunctionTSAttributeReference(owner: ImpexParameter) : TSReferenceBase<ImpexParameter>(owner) {

    override fun calculateDefaultRangeInElement(): TextRange {
        val attributeName = element.attributeName
        return TextRange.from(element.text.indexOf(attributeName), attributeName.length)
    }

    override fun getVariants(): Array<LookupElement> {
        // if an inline type already present, we should not suggest any other types
        if (element.inlineTypeName != null) return emptyArray()
        return TSCompletionService.getInstance(element.project)
            .getImpExInlineTypeCompletions(element.project, element)
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpexFunctionTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpexFunctionTSAttributeReference> { ref ->
            val project = ref.project
            val metaService = TSMetaModelAccess.getInstance(project)
            val featureName = ref.element.attributeName
            val typeName = ref.element.itemTypeName

            val result: Array<ResolveResult> = resolveType(ref.element, typeName, featureName, metaService)
                ?.let { arrayOf(it) }
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                project.service<TSModificationTracker>()
            )
        }

        private fun resolveType(
            element: PsiElement,
            typeName: String?,
            featureName: String,
            metaService: TSMetaModelAccess,
            recursionLevel: Int = 0
        ): ResolveResult? {
            // If we've entered this method, 2+ times it may sound like a bidirectional relation, return right away
            if (recursionLevel > HybrisConstants.TS_MAX_RECURSION_LEVEL) return null
            return metaService.findMetaClassifierByName(typeName)
                ?.let { meta ->
                    when (meta) {
                        is TSGlobalMetaEnum -> metaService.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE)
                            ?.let { it.allAttributes[featureName] }
                            ?.let { attr -> AttributeResolveResult(attr) }

                        is TSGlobalMetaItem -> meta.allAttributes[featureName]
                            ?.let { attr -> AttributeResolveResult(attr) }
                            ?: meta.allOrderingAttributes[featureName]
                                ?.let { attr -> OrderingAttributeResolveResult(attr) }
                            ?: meta.allRelationEnds
                                .find { relationEnd -> relationEnd.name.equals(featureName, true) }
                                ?.let { relationEnd -> RelationEndResolveResult(relationEnd) }

                        is TSGlobalMetaCollection -> resolveType(
                            element,
                            meta.elementType,
                            featureName,
                            metaService,
                            recursionLevel + 1
                        )

                        is TSGlobalMetaMap -> {
                            if (PsiTreeUtilExt.getPrevSiblingOfElementType(element, ImpexTypes.COMMA) == null) {
                                MapResolveResult(meta, MapType.ARGUMENTTYPE)
                            } else {
                                MapResolveResult(meta, MapType.RETURNTYPE)
                            }
                        }

                        else -> null
                    }
                }
        }

    }

}
