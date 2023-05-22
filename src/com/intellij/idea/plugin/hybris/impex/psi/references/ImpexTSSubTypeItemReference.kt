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

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.impex.psi.ImpexSubTypeName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup.TSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider

class ImpexTSSubTypeItemReference(owner: ImpexSubTypeName) : TSReferenceBase<ImpexSubTypeName>(owner), HighlightedReference {

    override fun getVariants() = getAllowedVariants(element)
        .mapNotNull { TSLookupElementFactory.build(it) }
        .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpexTSSubTypeItemReference>>("HYBRIS_TS_SUB_TYPE_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpexTSSubTypeItemReference> { ref ->
            val lookingForName = ref.value
            val metaService = TSMetaModelAccess.getInstance(ref.project)

            val result: Array<ResolveResult> = metaService.findMetaItemByName(lookingForName)
                ?.takeIf { getAllowedVariants(ref.element).contains(it) }
                ?.declarations
                ?.map { ItemResolveResult(it) }
                ?.toTypedArray()
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                metaService.getMetaModel()
            )
        }

        private fun getAllowedVariants(element: ImpexSubTypeName): List<TSGlobalMetaItem> {
            val headerTypeName = element.headerTypeName
                ?.text
                ?: return emptyList()

            return TSMetaModelAccess.getInstance(element.project).getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
                .filter {
                    it.allExtends.find { it.name == headerTypeName } != null
                        // or itself, it will be highlighted as unnecessary via Inspection
                        || it.name == headerTypeName
                }
        }
    }
}
