/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.search

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacroReference
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor

class ImpexReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val elementToSearch = queryParameters.elementToSearch

        if (!elementToSearch.isValid
            && queryParameters.effectiveSearchScope !is GlobalSearchScope
            && !ImpexPsiUtils.isMacroUsage(elementToSearch)
            && !ImpexPsiUtils.isMacroNameDeclaration(elementToSearch)
        ) return

        PsiTreeUtil.collectElements(elementToSearch.containingFile)
        {
            it is ImpexMacroUsageDec
                && it.reference?.resolve()?.textMatches(elementToSearch) ?: false
        }
            .map { ImpexMacroReference(it) }
            .forEach { consumer.process(it) }
    }
}
