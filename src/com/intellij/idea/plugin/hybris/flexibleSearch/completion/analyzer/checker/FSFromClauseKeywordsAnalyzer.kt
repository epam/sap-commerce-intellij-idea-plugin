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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
object FSFromClauseKeywordsAnalyzer {
    private val errorIdendifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED

    fun analyzeCompletions(parameters: CompletionParameters, completionResultSet: CompletionResultSet) {
        val context = parameters.originalPosition ?: parameters.position
        if (!isTableNameIdentifier(parameters) && parameters.position.parent != null /*&& parameters.position.parent is FlexibleSearchTableName*/ && !isJoinCondition(parameters) && PsiTreeUtil.getPrevSiblingOfType(context, FlexibleSearchTableReferenceList::class.java) != null) {
            addToResult(hashSetOf("AS"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }
        if (isIdentifier(parameters) && context.parentIsFromClause() && PsiTreeUtil.getPrevSiblingOfType(parameters.position, FlexibleSearchTableReferenceList::class.java) == null) {
            completionResultSet.addElement(FSLookupElementFactory.buildReference())
        }
        
        if (isIdentifier(parameters) && parameters.position.parent is PsiErrorElement) {
            if ((parameters.position.parent as PsiErrorElementImpl).errorDescription.contains("join")) {
                addToResult(hashSetOf("ON"), completionResultSet, AllIcons.Nodes.Function)
            }
        }
        if (isIdentifier(parameters) && context.prevSibling is FlexibleSearchQuerySpecification) {
            addToResult(hashSetOf("WHERE"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }
        if (context.parent != null && context.parent is FlexibleSearchFromClause) {
            val siblingBackward = context.skipWhitespaceSiblingsBackward()

            if (siblingBackward != null && siblingBackward is FlexibleSearchTableReferenceList && isIdentifier(parameters) && !isJoinCondition(parameters)) {
                addSymbolToResult(hashSetOf("ON"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
                if (PsiTreeUtil.findChildrenOfAnyType(context.parent, FlexibleSearchJoinType::class.java, FlexibleSearchJoinedTable::class.java).isEmpty())
                    addToResult(hashSetOf("JOIN", "LEFT JOIN"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
            }
        }
        if (isJoinCondition(parameters)) {
            addSymbolToResult(hashSetOf("=", ">", "<>", "<", "<=", ">="), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
            addToResult(hashSetOf("IS", "NOT"), completionResultSet.withPrefixMatcher(""), AllIcons.Nodes.Function)
        }

    }
}


fun PsiElement.parentIsFromClause() = PsiTreeUtil.getParentOfType(this, FlexibleSearchFromClause::class.java) != null
