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
package com.intellij.idea.plugin.hybris.flexibleSearch.completion

import com.intellij.codeInsight.completion.*
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchResultColumns
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class FlexibleSearchCompletionContributor : CompletionContributor() {

    override fun beforeCompletion(context: CompletionInitializationContext) {
        if (context.file !is FlexibleSearchFile) return

        context.dummyIdentifier = DUMMY_IDENTIFIER
    }

    init {
        val placePattern = psiElement()
            .andNot(psiElement().inside(PsiComment::class.java))
            .withLanguage(FlexibleSearchLanguage.INSTANCE)

        extend(
            CompletionType.BASIC,
            placePattern
                .withElementType(IDENTIFIER)
                .withText(DUMMY_IDENTIFIER)
                .withParent(psiElement(COLUMN_NAME)),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildYColumn())

                    PsiTreeUtil.getParentOfType(parameters.position, FlexibleSearchResultColumns::class.java)
                        ?.let {
                            result.addElement(FxSLookupElementFactory.buildYColumnAll())
                        }
                }
            }
        )

        // <{} or ()> after FROM keyword
        extend(
            CompletionType.BASIC,
            placePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(FROM)
                )
                .withLanguage(FlexibleSearchLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildYFrom())
                    result.addElement(FxSLookupElementFactory.buildFromParen())
                }
            }
        )

        // <{{ }}> after paren `(` and not in the column element
        extend(
            CompletionType.BASIC,
            placePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(LPAREN)
                )
                .withParent(PlatformPatterns.not(psiElement(COLUMN_NAME)))
                .withLanguage(FlexibleSearchLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildYSubSelect())
                }
            }
        )

        // special case for root element -> `select`
        extend(
            CompletionType.BASIC,
            placePattern
                .withParent(PsiErrorElement::class.java)
                .withLanguage(FlexibleSearchLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    val psiErrorElement = parameters.position.parent as? PsiErrorElement
                        ?: return

                    // FlexibleSearchTokenType.toString()
                    when (psiErrorElement.errorDescription.substringBefore(">") + ">") {
                        "<statement>" -> result.addAllElements(
                            FxSLookupElementFactory.buildKeywords("SELECT")
                        )
                    }
                }
            }
        )

        // <AS or ? JOIN> after `Identifier` leaf in the `Defined table name`
        extend(
            CompletionType.BASIC,
            placePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(IDENTIFIER)
                        .withParent(psiElement(DEFINED_TABLE_NAME))
                )
                .withParent(PlatformPatterns.not(psiElement(COLUMN_NAME)))
                .withLanguage(FlexibleSearchLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addAllElements(
                        FxSLookupElementFactory.buildKeywords(
                            "AS", "LEFT JOIN", "LEFT OUTER JOIN", "INNER JOIN", "RIGHT JOIN", "JOIN"
                        )
                    )
                }
            }
        )
    }

    companion object {
        const val DUMMY_IDENTIFIER = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    }
}