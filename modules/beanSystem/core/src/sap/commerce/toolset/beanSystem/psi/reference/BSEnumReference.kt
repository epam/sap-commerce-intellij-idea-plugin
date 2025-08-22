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

package sap.commerce.toolset.beanSystem.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.beanSystem.codeInsight.completion.BSCompletionService
import sap.commerce.toolset.beanSystem.meta.model.BSMetaType
import sap.commerce.toolset.psi.getValidResults

class BSEnumReference(
    element: PsiElement,
    range: TextRange
) : PsiReferenceBase.Poly<PsiElement>(element, range, false), PsiPolyVariantReference, HighlightedReference {

    override fun getVariants() = BSCompletionService.getInstance(element.project)
        .getCompletions(BSMetaType.META_ENUM)
        .toTypedArray()

    override fun multiResolve(p0: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, cacheKey(value), provider, false, this)
        .let { getValidResults(it) }

    companion object {
        fun cacheKey(postfix: String) = Key.create<ParameterizedCachedValue<Array<ResolveResult>, BSEnumReference>>("HYBRIS_BS_CACHED_REFERENCE_" + postfix)

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, BSEnumReference> { ref ->
            val project = ref.element.project
            val metaModelAccess = sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess.getInstance(project)
            val classFQN = ref.value
            val result: Array<ResolveResult> = metaModelAccess.findMetaEnumByName(classFQN)
                ?.let { _root_ide_package_.sap.commerce.toolset.beanSystem.psi.reference.result.EnumResolveResult(it) }
                ?.let { arrayOf(it) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                sap.commerce.toolset.beanSystem.meta.BSModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }
}