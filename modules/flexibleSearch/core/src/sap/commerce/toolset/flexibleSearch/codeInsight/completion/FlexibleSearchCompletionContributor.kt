/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.flexibleSearch.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.codeInsight.patterns.CaseInsensitiveContainsPatternCondition
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.codeInsight.completion.provider.FxSHybrisColumnCompletionProvider
import sap.commerce.toolset.flexibleSearch.codeInsight.completion.provider.FxSKeywordsCompletionProvider
import sap.commerce.toolset.flexibleSearch.codeInsight.completion.provider.FxSRootCompletionProvider
import sap.commerce.toolset.flexibleSearch.codeInsight.completion.provider.FxSTablesAliasCompletionProvider
import sap.commerce.toolset.flexibleSearch.codeInsight.lookup.FxSLookupElementFactory
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefYExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes.*

class FlexibleSearchCompletionContributor : CompletionContributor() {

    private val keywordJoins = setOf("LEFT JOIN", "LEFT OUTER JOIN", "INNER JOIN", "RIGHT JOIN", "JOIN")
    val expressionOperations = FxSKeywordsCompletionProvider(
        setOf(
            "IS NULL", "IS NOT NULL",
            "=", "==", ">", ">=", "<", "<=", "!=", "<>",
        )
    )

    override fun beforeCompletion(context: CompletionInitializationContext) {
        if (context.file !is FlexibleSearchPsiFile) return

        context.dummyIdentifier = FlexibleSearchConstants.DUMMY_IDENTIFIER
    }

//    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
//        val position = parameters.position
//
//        if (position.prevSibling?.text in listOf(":")) {
//            result.addElement(FxSLookupElementFactory.buildOuterJoin())
//            val autoPopupController = AutoPopupController.getInstance(position.project)

//            autoPopupController.autoPopupMemberLookup(parameters.editor, null)
//            autoPopupController.scheduleAutoPopup(parameters.editor, CompletionType.BASIC, null)
//        }
//    }

    init {
        val fxsBasePattern = psiElement()
            .andNot(psiElement().inside(PsiComment::class.java))
            .withLanguage(FlexibleSearchLanguage)

//        extend(
//            CompletionType.BASIC,
//            fxsBasePattern,
//            FxSKeywordsCompletionProvider(setOf("_test_"))
//        )

        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(StandardPatterns.string().startsWith(FlexibleSearchConstants.DUMMY_IDENTIFIER))
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(RBRACKET)
                        .withParent(FlexibleSearchColumnRefYExpression::class.java),
                ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildOuterJoin())
                }
            }
        )

        // suggest table alias after `as`
        // TODO: if there are multiple joins on a same table - add # postfix to the name
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .withParent(
                    psiElement(TABLE_ALIAS_NAME)
                        .withParent(
                            psiElement(FROM_TABLE)
                                .withText(
                                    StandardPatterns.string()
                                        .with(CaseInsensitiveContainsPatternCondition(" AS "))
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
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .withParent(psiElement(COLUMN_NAME)),
            FxSHybrisColumnCompletionProvider()
        )

        // <{} and <()> after FROM keyword
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
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
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
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

        // <AS>, <ON>, <.. JOIN> after `Identifier` leaf in the `Defined table name`
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),
                    psiElement(IDENTIFIER)
                        .withParent(psiElement(DEFINED_TABLE_NAME))
                )
                .withParent(PlatformPatterns.not(psiElement(COLUMN_NAME))),
            FxSKeywordsCompletionProvider(setOf("AS", "ON") + keywordJoins)
        )

        // <ON>, <.. JOIN> after `AS`
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        psiElement(TokenType.WHITE_SPACE),
                        psiElement(TokenType.ERROR_ELEMENT),
                    ),
                    psiElement(IDENTIFIER)
                        .withParent(psiElement(TABLE_ALIAS_NAME))
                ),
            FxSKeywordsCompletionProvider(setOf("ON") + keywordJoins)
        )

        // <AND>, <OR> with WHERE_CLAUSE parent after any EXPRESSION
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .withSuperParent(2, psiElement(WHERE_CLAUSE))
                .withParent(
                    psiElement(TokenType.ERROR_ELEMENT)
                        .afterLeafSkipping(
                            psiElement(TokenType.WHITE_SPACE),
                            psiElement()
                                .withParent(
                                    psiElement(FlexibleSearchExpression::class.java)
                                        .andNot(
                                            PlatformPatterns.or(
                                                psiElement(FlexibleSearchColumnRefYExpression::class.java),
                                                psiElement(FlexibleSearchColumnRefExpression::class.java)
                                            )
                                        )
                                ),
                        )
                ),
            FxSKeywordsCompletionProvider(setOf("AND", "OR"))
        )

        // <operations> with WHERE_CLAUSE parent after any EXPRESSION
        extend(
            CompletionType.BASIC,
            fxsBasePattern
                .withText(FlexibleSearchConstants.DUMMY_IDENTIFIER)
                .withSuperParent(2, psiElement(WHERE_CLAUSE))
                .afterLeafSkipping(
                    psiElement(TokenType.WHITE_SPACE),

                    PlatformPatterns.or(
                        psiElement(RBRACE)
                            .withParent(psiElement(COLUMN_REF_Y_EXPRESSION)),
                        psiElement(IDENTIFIER)
                            .withParent(psiElement(COLUMN_NAME)),
                    )
                ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    expressionOperations.addCompletionVariants(parameters, context, result)

                    result.addElement(FxSLookupElementFactory.buildIn())
                    result.addElement(FxSLookupElementFactory.buildNotIn())
                }
            }
        )
    }

}