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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.OrderingAttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider

internal class ImpexTSAttributeReference(owner: ImpexAnyHeaderParameterName) : TSReferenceBase<ImpexAnyHeaderParameterName>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpexTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpexTSAttributeReference> { ref ->
            val project = ref.project
            val metaModelAccess = TSMetaModelAccess.getInstance(project)
            val featureName = ref.value
            val result = (
                tryResolveForItemType(metaModelAccess, featureName, ref.element.headerItemTypeName?.text)
                    ?: tryResolveForRelationType(ref.element, metaModelAccess, featureName)
                    ?: tryResolveByEnumType(ref.element, metaModelAccess, featureName)
                )
                ?.let { arrayOf(it) }
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                project.service<TSModificationTracker>()
            )
        }

        private fun tryResolveByEnumType(
            element: ImpexAnyHeaderParameterName,
            metaService: TSMetaModelAccess,
            refName: String
        ): ResolveResult? = element.headerItemTypeName
            ?.text
            ?.let { metaService.findMetaEnumByName(it) }
            ?.let { metaService.findMetaItemByName(HybrisConstants.TS_TYPE_ENUMERATION_VALUE) }
            ?.let { it.allAttributes[refName] }
            ?.let { AttributeResolveResult(it) }

        private fun tryResolveForItemType(
            metaModelService: TSMetaModelAccess,
            featureName: String,
            itemTypeCode: String?
        ): ResolveResult? = itemTypeCode
            ?.let { metaModelService.findMetaItemByName(it) }
            ?.let { meta ->
                meta.allAttributes[featureName]
                    ?.let { AttributeResolveResult(it) }
                    ?: meta.allOrderingAttributes[featureName]
                        ?.let { OrderingAttributeResolveResult(it) }
                    ?: meta.allRelationEnds
                        .find { it.name.equals(featureName, true) }
                        ?.let { RelationEndResolveResult(it) }
            }

        private fun tryResolveForRelationType(
            element: ImpexAnyHeaderParameterName,
            metaService: TSMetaModelAccess,
            featureName: String
        ): ResolveResult? = element.headerItemTypeName
            ?.text
            ?.let { metaService.findMetaRelationByName(it) }
            ?.let {
                if (HybrisConstants.ATTRIBUTE_SOURCE.equals(featureName, ignoreCase = true)) {
                    return@let RelationEndResolveResult(it.source)
                } else if (HybrisConstants.ATTRIBUTE_TARGET.equals(featureName, ignoreCase = true)) {
                    return@let RelationEndResolveResult(it.target)
                }

                return@let tryResolveForItemType(metaService, featureName, HybrisConstants.TS_TYPE_LINK)
            }
    }
}
