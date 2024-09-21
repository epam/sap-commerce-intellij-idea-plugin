/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.CngCompletionService
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlTag

class CngInitializePropertyReference : PsiReferenceBase.Poly<PsiElement>, PsiPolyVariantReference, HighlightedReference {

    constructor(element: PsiElement) : super(element)
    constructor(element: PsiElement, textRange: TextRange) : super(element, textRange, false)

    override fun getVariants() = CngCompletionService.getInstance(element.project)
        .getInitializeProperties(element)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, cacheKey(rangeInElement), provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        const val NEW_OBJECT = "newObject"

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngInitializePropertyReference> { ref ->
            val element = ref.element
            val text = ref.value

            val result = element.parentsOfType<XmlTag>()
                .firstOrNull { it.localName == "flow" }
                ?.childrenOfType<XmlTag>()
                ?.filter { it.localName == "prepare" }
                ?.flatMap { it.childrenOfType<XmlTag>() }
                ?.asSequence()
                ?.filter { it.localName == "initialize" }
                ?.mapNotNull { it.getAttribute("property") }
                ?.mapNotNull { it.valueElement }
                ?.filter { it.value == text }
                ?.map { PsiElementResolveResult(it) }
                ?.toList()
                ?.let { PsiUtils.getValidResults(it.toTypedArray()) }
                ?.takeIf { it.isNotEmpty() }
                ?: CngPsiHelper.resolveContextTag(element)
                    ?.getAttribute("type")
                    ?.valueElement
                    ?.let { PsiUtils.getValidResults(arrayOf(PsiElementResolveResult(it))) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    private fun cacheKey(range: TextRange) =
        Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngInitializePropertyReference>>("HYBRIS_CNGINITIALIZEPROPERTYREFERENCE_" + range)
}
