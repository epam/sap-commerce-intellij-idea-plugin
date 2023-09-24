/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag

class CngInitializePropertyReference : PsiReferenceBase.Poly<PsiElement>, PsiPolyVariantReference, HighlightedReference {

    constructor(element: PsiElement) : super(element)
    constructor(element: PsiElement, textRange: TextRange) : super(element, textRange, false)

    override fun getVariants() = CngCompletionService.getInstance(element.project)
        .getInitializeProperties(element)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val text = value

        return element.parentsOfType<XmlTag>()
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
                ?.takeIf { text == NEW_OBJECT }
                ?.getAttribute("type")
                ?.valueElement
                ?.let { PsiUtils.getValidResults(arrayOf(PsiElementResolveResult(it))) }
            ?: emptyArray()
    }

    companion object {
        const val NEW_OBJECT = "newObject"
    }
}
