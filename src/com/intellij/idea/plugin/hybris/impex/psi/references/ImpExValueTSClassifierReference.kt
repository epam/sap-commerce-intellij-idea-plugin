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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.completion.TSCompletionService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.*
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class ImpExValueTSClassifierReference(
    owner: ImpexValue,
    textRange: TextRange,
    private val allowedTypes: List<TSMetaType> = TSMetaType.entries
) : TSReferenceBase<ImpexValue>(owner, false, textRange), HighlightedReference {

    private val cacheKey = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExValueTSClassifierReference>>("HYBRIS_TS_CACHED_REFERENCE_$textRange")

    fun getTargetElement(): PsiElement? = element

    override fun getVariants(): Array<LookupElementBuilder> = TSCompletionService.getInstance(element.project)
        .getCompletions(*allowedTypes.toTypedArray())
        .toTypedArray()

//    override fun resolve(): PsiElement? = multiResolve(false)
//        .lastOrNull()
//        ?.element

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, cacheKey, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExValueTSClassifierReference> { ref ->
            val project = ref.project
            val lookingForName = ref.value
            val allowedTypes = ref.allowedTypes

            val results: Array<ResolveResult> = TSMetaModelAccess.getInstance(project).findMetaClassifierByName(lookingForName)
                ?.let {
                    when {
                        it is TSGlobalMetaItem && allowedTypes.contains(TSMetaType.META_ITEM) -> it.declarations.map { meta -> ItemResolveResult(meta) }
                        it is TSGlobalMetaEnum && allowedTypes.contains(TSMetaType.META_ENUM) -> it.declarations.map { meta -> EnumResolveResult(meta) }
                        it is TSGlobalMetaRelation && allowedTypes.contains(TSMetaType.META_RELATION) -> it.declarations.map { meta -> RelationResolveResult(meta) }
                        it is TSGlobalMetaMap && allowedTypes.contains(TSMetaType.META_MAP) -> it.declarations.map { meta -> MapResolveResult(meta) }
                        it is TSGlobalMetaAtomic && allowedTypes.contains(TSMetaType.META_ATOMIC) -> it.declarations.map { meta -> AtomicResolveResult(meta) }
                        it is TSGlobalMetaCollection && allowedTypes.contains(TSMetaType.META_COLLECTION) -> it.declarations.map { meta -> CollectionResolveResult(meta) }
                        else -> null
                    }
                }
                ?.toTypedArray()
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                results,
                project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT,
            )
        }
    }
}
