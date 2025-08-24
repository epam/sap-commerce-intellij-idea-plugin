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
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.psi.ImpExUserRightsSingleValue
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.codeInsight.completion.TSCompletionService
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaRelation
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase
import sap.commerce.toolset.typeSystem.psi.reference.result.AttributeResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.OrderingAttributeResolveResult
import sap.commerce.toolset.typeSystem.psi.reference.result.RelationEndResolveResult

open class ImpExUserRightsTSAttributeReference : TSReferenceBase<PsiElement>, HighlightedReference {

    constructor(owner: PsiElement, soft: Boolean = false, rangeInElement: TextRange? = null) : super(owner, soft, rangeInElement)

    override fun getVariants() = getType()
        ?.let {
            TSCompletionService.getInstance(element.project)
                .getCompletions(
                    it,
                    TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION
                )
                .toTypedArray()
        }
        ?: emptyArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { getValidResults(it) }
    }

    open fun getType() = PsiTreeUtil.getPrevSiblingOfType(element, ImpExUserRightsSingleValue::class.java)
        ?.text

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExUserRightsTSAttributeReference>>("HYBRIS_TS_CACHED_TARGET_ATTRIBUTE_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExUserRightsTSAttributeReference> { ref ->
            val project = ref.project
            val metaModelAccess = TSMetaModelAccess.getInstance(project)
            val featureName = ref.value
            val type = ref.getType()
            val result: Array<ResolveResult> = metaModelAccess.findMetaClassifierByName(type)
                ?.let { meta ->
                    when (meta) {
                        is TSGlobalMetaEnum -> metaModelAccess.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE)
                            ?.let { it.allAttributes[featureName] }
                            ?.let { attr -> AttributeResolveResult(attr) }

                        is TSGlobalMetaItem -> resolve(meta, featureName)

                        is TSGlobalMetaRelation -> {
                            if (HybrisConstants.ATTRIBUTE_SOURCE.equals(featureName, ignoreCase = true)) {
                                RelationEndResolveResult(meta.source)
                            } else if (HybrisConstants.ATTRIBUTE_TARGET.equals(featureName, ignoreCase = true)) {
                                RelationEndResolveResult(meta.target)
                            } else {
                                metaModelAccess.findMetaItemByName(HybrisConstants.TS_TYPE_LINK)
                                    ?.let { resolve(it, featureName) }
                            }
                        }

                        else -> null
                    }
                }
                ?.let { arrayOf(it) }
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                TSModificationTracker.getInstance(project)
            )
        }

        private fun resolve(meta: TSGlobalMetaItem, featureName: String) = meta.allAttributes[featureName]
            ?.let { attr -> AttributeResolveResult(attr) }
            ?: meta.allOrderingAttributes[featureName]
                ?.let { attr -> OrderingAttributeResolveResult(attr) }
            ?: meta.allRelationEnds
                .find { relationEnd -> relationEnd.name.equals(featureName, true) }
                ?.let { relationEnd -> RelationEndResolveResult(relationEnd) }

    }

}