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
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.analyzer.*
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FxsColumnReferenceCompletionProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchJoinType
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchJoinedTable
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableReferenceList
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchWhereClause
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
object FSWhereClauseKeywordsAnalyzer {
    private val errorIdendifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED

    fun analyzeCompletions(parameters: CompletionParameters, completionResultSet: CompletionResultSet) {
        val context = parameters.originalPosition ?: parameters.position

        if (isColumnReferenceIdentifier(parameters)) {
            FxsColumnReferenceCompletionProvider.instance.addCompletionVariants(parameters, ProcessingContext(), completionResultSet)
        }

        if (context.parent != null && PsiTreeUtil.getParentOfType(parameters.position.parent, FlexibleSearchWhereClause::class.java) != null) {
            val siblingBackward = context.skipWhitespaceSiblingsBackward()

            if (siblingBackward != null && siblingBackward is FlexibleSearchTableReferenceList && isIdentifier(parameters) && !isJoinCondition(parameters)) {
                addSymbolToResult(hashSetOf("ON"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
                if (PsiTreeUtil.findChildrenOfAnyType(context.parent, FlexibleSearchJoinType::class.java, FlexibleSearchJoinedTable::class.java).isEmpty())
                    addToResult(hashSetOf("JOIN", "LEFT JOIN"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
            }
        }
        if (isColumnReferenceIdentifier(parameters) && context.parentIsWhereClause()) {
            completionResultSet.addElement(FSLookupElementFactory.buildReference())
        }
        if (isJoinCondition(parameters)) {
            addSymbolToResult(hashSetOf("=", ">", "<>", "<", "<=", ">="), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
            addToResult(hashSetOf("IS", "NOT"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }

    }
}

fun PsiElement.parentIsWhereClause() = PsiTreeUtil.getParentOfType(this, FlexibleSearchWhereClause::class.java) != null
