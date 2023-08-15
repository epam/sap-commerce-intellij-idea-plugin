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

package com.intellij.idea.plugin.hybris.system.bean.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.BSCompletionService
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.jetbrains.kotlin.psi.psiUtil.parents

class OccLevelMappingReference(
    private val meta: BSGlobalMetaBean,
    element: PsiElement,
    range: TextRange
) : PsiReferenceBase.Poly<PsiElement>(element, range, false), PsiPolyVariantReference, HighlightedReference {

    override fun getVariants() = BSCompletionService.getInstance(element.project)
        .getCompletions(meta)
        .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val levelMapping = value

        return element.parents
            .mapNotNull { it as? XmlTag }
            .filter { it.localName == "bean" }
            .firstOrNull()
            ?.childrenOfType<XmlTag>()
            ?.filter { it.localName == "property" }
            ?.firstOrNull { it.getAttributeValue("name") == "levelMapping" }
            ?.let { PsiTreeUtil.collectElements(it) { element -> element is XmlAttribute && element.localName == "key" } }
            ?.map { it as XmlAttribute }
            ?.mapNotNull { it.valueElement }
            ?.filter { it.value == levelMapping }
            ?.let { PsiElementResolveResult.createResults(it) }
            ?: emptyArray()
    }
}