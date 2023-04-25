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
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FxSHybrisColumnCompletionProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FxSKeywordsCompletionProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FxSRootCompletionProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FxSTablesAliasCompletionProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFile
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchExpression
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.util.ProcessingContext

class FlexibleSearchCompletionContributor : CompletionContributor() {

    override fun beforeCompletion(context: CompletionInitializationContext) {
        if (context.file !is FlexibleSearchFile) return

        context.dummyIdentifier = DUMMY_IDENTIFIER
    }

    init {
        val fxsBasePattern = psiElement()
            .andNot(psiElement().inside(PsiComment::class.java))
            .withLanguage(FlexibleSearchLanguage.INSTANCE)

//        extend(
//            CompletionType.BASIC,
//            fxsBasePattern,
//            object : CompletionProvider<CompletionParameters>() {
//                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
//                    result.addAllElements(FxSLookupElementFactory.buildKeywords(setOf("_test_")))
//                }
//            }
//        )

//        extend(
//            CompletionType.BASIC,
//            fxsBasePattern
//                .withText(DUMMY_IDENTIFIER)
//                .afterLeafSkipping(
//                    psiElement(TokenType.WHITE_SPACE),
//                    psiElement().withParent(FlexibleSearchExpression::class.java),
//                ),
//            FxSKeywordsCompletionProvider(setOf("AND", "OR"))
//        )

        // suggest table alias after `as`
        // TODO: if there multiple joins on a same table - add # postfix to the name
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(DUMMY_IDENTIFIER)
                .withParent(
                    psiElement(TABLE_ALIAS_NAME)
                        .withParent(
                            psiElement(FROM_TABLE)
                                .withText(

                                    StandardPatterns.or(
                                        // no idea how to make it case insensitive
                                        StandardPatterns.string().contains(" AS "),
                                        StandardPatterns.string().contains(" as "),
                                        StandardPatterns.string().contains(" As "),
                                        StandardPatterns.string().contains(" aS "),
                                    )
                                )
                        )
                ),
            FxSTablesAliasCompletionProvider()
        )

        // <{}> and optional <*> inside the [y] column
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withElementType(IDENTIFIER)
                .withText(DUMMY_IDENTIFIER)
                .withParent(psiElement(COLUMN_NAME)),
            FxSHybrisColumnCompletionProvider()
        )

        // <{} and <()> after FROM keyword
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(FROM)
                ),
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
            fxsBasePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(LPAREN)
                )
                .withParent(PlatformPatterns.not(psiElement(COLUMN_NAME))),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildYSubSelect())
                }
            }
        )

        // special case for root element -> `select`
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withParent(PsiErrorElement::class.java),
            FxSRootCompletionProvider()
        )

        // <AS> and <.. JOIN> after `Identifier` leaf in the `Defined table name`
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(IDENTIFIER)
                        .withParent(psiElement(DEFINED_TABLE_NAME))
                )
                .withParent(PlatformPatterns.not(psiElement(COLUMN_NAME))),
            FxSKeywordsCompletionProvider(setOf("AS") + KEYWORDS_JOINS)
        )

        // suggest different joins after `as` after `on`
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    PlatformPatterns.or(
                        psiElement()
                            .withSuperParent(3, psiElement(JOIN_CONSTRAINT)),
                        psiElement()
                            .withParent(psiElement(TABLE_ALIAS_NAME))
                    )
                ),
            FxSKeywordsCompletionProvider(setOf("ON") + KEYWORDS_JOINS)
        )
    }

    companion object {
        const val DUMMY_IDENTIFIER = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
        val KEYWORDS_JOINS = setOf("LEFT JOIN", "LEFT OUTER JOIN", "INNER JOIN", "RIGHT JOIN", "JOIN")
    }
}