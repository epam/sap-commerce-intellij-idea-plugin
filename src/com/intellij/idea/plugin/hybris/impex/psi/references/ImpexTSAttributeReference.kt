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
package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider

internal class ImpexTSAttributeReference(owner: ImpexAnyHeaderParameterNameMixin) : TSReferenceBase<ImpexAnyHeaderParameterName>(owner) {
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
            val metaModelAccess = TSMetaModelAccess.getInstance(ref.project)
            val metaItemService = TSMetaItemService.getInstance(ref.project)
            val featureName = ref.value
            var result = tryResolveForItemType(ref.element, metaModelAccess, metaItemService, featureName)
            if (result == null) {
                result = tryResolveForRelationType(ref.element, metaModelAccess, metaItemService, featureName)
            }
            if (result == null) {
                result = tryResolveForEnumType(ref.element, metaModelAccess, featureName)
            }
            if (result == null) {
                result = ResolveResult.EMPTY_ARRAY
            }

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                metaModelAccess.getMetaModel()
            )
        }

        private fun tryResolveForEnumType(
            element: ImpexAnyHeaderParameterName,
            metaService: TSMetaModelAccess,
            featureName: String
        ): Array<ResolveResult>? = if (HybrisConstants.CODE_ATTRIBUTE_NAME == featureName || HybrisConstants.NAME_ATTRIBUTE_NAME == featureName)
            ImpexPsiUtils.findHeaderItemTypeName(element)
                ?.text
                ?.let { metaService.findMetaEnumByName(it) }
                ?.let { arrayOf(EnumResolveResult(it)) }
        else null

        private fun tryResolveForItemType(
            element: ImpexAnyHeaderParameterName,
            metaModelService: TSMetaModelAccess,
            metaItemService: TSMetaItemService,
            featureName: String
        ): Array<ResolveResult>? = ImpexPsiUtils.findHeaderItemTypeName(element)
            ?.text
            ?.let { metaModelService.findMetaItemByName(it) }
            ?.let {
                val attributes = resolveMetaItemAttributes(metaItemService, featureName, it)
                val relationEnds = metaItemService.findRelationEndsByQualifier(it, featureName, true)
                    .map { relation -> RelationEndResolveResult(relation) }
                (attributes + relationEnds)
            }

        private fun tryResolveForRelationType(
            element: ImpexAnyHeaderParameterName,
            metaService: TSMetaModelAccess,
            metaItemService: TSMetaItemService,
            featureName: String
        ): Array<ResolveResult>? = ImpexPsiUtils.findHeaderItemTypeName(element)
            ?.text
            ?.let { metaService.findMetaRelationByName(it) }
            ?.let {
                if (HybrisConstants.SOURCE_ATTRIBUTE_NAME.equals(featureName, ignoreCase = true)) {
                    return@let arrayOf(RelationEndResolveResult(it.source))
                } else if (HybrisConstants.TARGET_ATTRIBUTE_NAME.equals(featureName, ignoreCase = true)) {
                    return@let arrayOf(RelationEndResolveResult(it.target))
                }

                return@let metaService.findMetaItemByName(HybrisConstants.TS_TYPE_LINK)
                    ?.let { metaLink -> resolveMetaItemAttributes(metaItemService, featureName, metaLink) }
            }

        private fun resolveMetaItemAttributes(
            metaItemService: TSMetaItemService,
            featureName: String,
            metaItem: TSGlobalMetaItem
        ): Array<ResolveResult> = metaItemService.findAttributesByName(metaItem, featureName, true)
            .map { AttributeResolveResult(it) }
            .toTypedArray()
    }
}
