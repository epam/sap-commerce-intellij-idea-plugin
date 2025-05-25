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
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.BSModificationTracker
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.result.BeanResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class BSBeanReference(
    element: PsiElement,
    range: TextRange
) : PsiReferenceBase.Poly<PsiElement>(element, range, false), PsiPolyVariantReference, HighlightedReference {

    override fun getVariants() = BSCompletionService.getInstance(element.project)
        .getCompletions(BSMetaType.META_BEAN, BSMetaType.META_WS_BEAN, BSMetaType.META_EVENT)
        .toTypedArray()

    override fun multiResolve(p0: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, cacheKey(value), provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        fun cacheKey(postfix: String) = Key.create<ParameterizedCachedValue<Array<ResolveResult>, BSBeanReference>>("HYBRIS_BS_CACHED_REFERENCE_" + postfix)

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, BSBeanReference> { ref ->
            val project = ref.element.project
            val metaModelAccess = BSMetaModelAccess.getInstance(project)
            val classFQN = ref.value
            val result: Array<ResolveResult> = metaModelAccess.findMetaBeanByName(classFQN)
                ?.let { BeanResolveResult(it) }
                ?.let { arrayOf(it) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                project.service<BSModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }
}