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
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumValueResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class ImpExValueTSDynamicEnumReference(
    owner: ImpexValue,
    metaName: String,
    textRange: TextRange,
) : ImpExValueTSEnumReference(owner, metaName, true, textRange)

class ImpExValueTSStaticEnumReference(
    owner: ImpexValue,
    metaName: String,
    textRange: TextRange,
) : ImpExValueTSEnumReference(owner, metaName, false, textRange)

abstract class ImpExValueTSEnumReference(
    owner: ImpexValue,
    private val metaName: String,
    soft: Boolean,
    textRange: TextRange,
) : TSReferenceBase<ImpexValue>(owner, soft, textRange), HighlightedReference {

    private val cacheKey = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExValueTSEnumReference>>("HYBRIS_TS_CACHED_REFERENCE_$textRange")

    fun getTargetElement(): PsiElement? = element

    override fun getVariants(): Array<LookupElementBuilder> = TSMetaModelAccess.getInstance(project)
        .findMetaEnumByName(metaName)
        ?.let { TSCompletionService.getInstance(project).getCompletions(it) }
        ?.toTypedArray()
        ?: emptyArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, cacheKey, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExValueTSEnumReference> { ref ->
            val lookingForName = ref.value
            val project = ref.project
            val metaService = TSMetaModelAccess.getInstance(project)

            val result: Array<ResolveResult> = metaService.findMetaEnumByName(ref.metaName)
                ?.values[lookingForName]
                ?.let { arrayOf(EnumValueResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                project.service<TSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT,
            )
        }
    }
}
