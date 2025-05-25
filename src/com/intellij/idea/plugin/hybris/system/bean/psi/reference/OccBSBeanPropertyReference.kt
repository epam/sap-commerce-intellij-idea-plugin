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

package com.intellij.idea.plugin.hybris.system.bean.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.BSCompletionService
import com.intellij.idea.plugin.hybris.system.bean.meta.BSModificationTracker
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.psi.OccPropertyMapping
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.result.BeanPropertyResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class OccBSBeanPropertyReference(
    private val meta: BSGlobalMetaBean,
    element: PsiElement,
    mapping: OccPropertyMapping
) : PsiReferenceBase.Poly<PsiElement>(element, mapping.textRange, false), PsiPolyVariantReference, HighlightedReference {

    override fun getVariants() = BSCompletionService.getInstance(element.project)
        .getCompletions(meta)
        .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, cacheKey(rangeInElement), provider, false, this to meta)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, Pair<OccBSBeanPropertyReference, BSGlobalMetaBean>> { param ->
            val ref = param.first
            val meta = param.second
            val element = ref.element
            val propertyName = ref.value

            val result = meta.allProperties[propertyName]
                ?.let { BeanPropertyResolveResult(it) }
                ?.let { arrayOf(it) }
                ?.let { PsiUtils.getValidResults(it) }
                ?: emptyArray()

            val project = element.project
            CachedValueProvider.Result.create(
                result,
                project.service<BSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    private fun cacheKey(range: TextRange) = Key.create<ParameterizedCachedValue<Array<ResolveResult>, Pair<OccBSBeanPropertyReference, BSGlobalMetaBean>>>("HYBRIS_OCCBSBEANPROPERTYREFERENCE_" + range)
}