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

package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsSingleValue
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.OrderingAttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class ImpexUserRightsTSAttributeReference(owner: ImpexUserRightsAttributeValue) : TSReferenceBase<ImpexUserRightsAttributeValue>(owner), HighlightedReference {

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
            .let { PsiUtils.getValidResults(it) }
    }

    fun getType() = PsiTreeUtil.getPrevSiblingOfType(element, ImpexUserRightsSingleValue::class.java)
        ?.text

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpexUserRightsTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpexUserRightsTSAttributeReference> { ref ->
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
                project.service<TSModificationTracker>()
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