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

package com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.checker

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.addToResult
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.isColumnReferenceIdentifier
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.skipWhitespaceSiblingsBackward
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchQuerySpecification
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectList
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
object FSSelectClauseKeywordsAnalyzer {
    private val errorIdendifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED

    fun analyzeCompletions(parameters: CompletionParameters, completionResultSet: CompletionResultSet) {
        val context = parameters.originalPosition ?: parameters.position

        if (context.parent != null && PsiTreeUtil.getParentOfType(parameters.position.parent, FlexibleSearchSelectList::class.java) != null) {
            val siblingBackward = context.skipWhitespaceSiblingsBackward()

            if (siblingBackward is FlexibleSearchQuerySpecification) {
                val tableNames = PsiTreeUtil.findChildrenOfType(siblingBackward, FlexibleSearchTableName::class.java)

                if (tableNames.isEmpty()) {
                    addToResult(hashSetOf("*", "COUNT", "DISTINCT", "FROM"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
                }
            }
        }

        if (isColumnReferenceIdentifier(parameters) && PsiTreeUtil.getParentOfType(context, FlexibleSearchSelectList::class.java) != null) {
            addToResult(hashSetOf("FROM"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Static)
            addToResult(hashSetOf("*", "COUNT", "DISTINCT", "FROM"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }
        if (isColumnReferenceIdentifier(parameters) && PsiTreeUtil.getPrevSiblingOfType(context, FlexibleSearchQuerySpecification::class.java) != null) {
            addToResult(hashSetOf("*", "COUNT", "DISTINCT", "FROM"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }
//            if (isColumnReferenceIdentifier(parameters)) {
//                addToResult(hashSetOf("*"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
//            }
    }
}