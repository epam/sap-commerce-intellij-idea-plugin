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

package com.intellij.idea.plugin.hybris.system.type.psi.reference

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.xml.GenericDomValue

class PlainXmlReference(
    element: PsiElement,
    val value: GenericDomValue<String>,
    private val project: Project = element.project
) : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {

    override fun getRangeInElement() = TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val psiSearchHelper = PsiSearchHelper.getInstance(project)
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return ResolveResult.EMPTY_ARRAY
        val searchText = value.stringValue?.trim() ?: return ResolveResult.EMPTY_ARRAY
        val foundEls = mutableListOf<PsiElement>()

        psiSearchHelper.processElementsWithWord({ el, _ ->
            if (el.containingFile.name.contains("-spring") && el is XmlAttribute && el.name == "id") foundEls.add(el)
            true
        },
            GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.moduleScope(module), XmlFileType.INSTANCE), searchText,
            UsageSearchContext.ANY, true)

        return if (foundEls.isEmpty()) ResolveResult.EMPTY_ARRAY else PsiElementResolveResult.createResults(foundEls)
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY
}