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
package com.intellij.idea.plugin.hybris.flexibleSearch.lang.findUsages

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchColumnName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.COLUMN_ALIAS_NAME
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.TABLE_ALIAS_NAME
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Processor

class FlexibleSearchReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {

    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        ReadAction.run<Throwable> {
            val elementToSearch = queryParameters.elementToSearch;
            if (!elementToSearch.isValid) return@run
            if (!FlexibleSearchFindUsagesProvider.SUPPORTED_ELEMENT_TYPES.contains(elementToSearch.elementType)) return@run

            when (elementToSearch.elementType) {
                TABLE_ALIAS_NAME -> processTableAlias(elementToSearch, consumer)
                COLUMN_ALIAS_NAME -> processColumnAlias(elementToSearch, consumer)
                else -> throw UnsupportedOperationException("Missing implementation for ${elementToSearch.elementType}")
            }
        }

    }

    private fun processTableAlias(elementToSearch: PsiElement, consumer: Processor<in PsiReference>) {
        val file = elementToSearch.containingFile;

        PsiTreeUtil.collectElements(file) {
            it is FlexibleSearchSelectedTableName && it.textMatches(elementToSearch)
        }
            .mapNotNull { it.reference }
            .forEach { consumer.process(it) }
    }

    private fun processColumnAlias(elementToSearch: PsiElement, consumer: Processor<in PsiReference>) {
        val file = elementToSearch.containingFile;

        PsiTreeUtil.collectElements(file) {
            it is FlexibleSearchColumnName && it.textMatches(elementToSearch)
        }
            .mapNotNull { it.reference }
            .forEach { consumer.process(it) }
    }
}
