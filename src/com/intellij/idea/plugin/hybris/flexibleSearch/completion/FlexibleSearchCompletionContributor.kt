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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiComment
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
                .withElementType(FlexibleSearchTypes.IDENTIFIER)
                .withText(DUMMY_IDENTIFIER)
                .withParent(psiElement(FlexibleSearchTypes.COLUMN_NAME)),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    result.addElement(FxSLookupElementFactory.buildYColumnReference())

                    PsiTreeUtil.getParentOfType(parameters.position, FlexibleSearchResultColumns::class.java)
                        ?.let {
                            result.addElement(FxSLookupElementFactory.buildYColumnAll())
                        }
                }
            }
        )

//        extend(
//            CompletionType.BASIC,
//            placePattern.withElementType(
//                PlatformPatterns.elementType().or(
//                    FlexibleSearchTypes.IDENTIFIER,
//                    FlexibleSearchTypes.BACKTICK_LITERAL
//                )
//            ),
//            object : CompletionProvider<CompletionParameters>() {
//                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
//                    parameters.originalPosition
//                        ?.let {
//                            val provider = when (it.parent.elementType) {
//                                FlexibleSearchTypes.SELECTED_TABLE_NAME -> FlexibleSearchTableAliasCompletionProvider.instance
//                                else -> null
//                            }
//                            provider
//                                ?.addCompletionVariants(parameters, context, result)
//                        }
//                }
//            }
//        )

        // keywords
//        extend(
//            CompletionType.BASIC,
//            psiElement(PsiElement.class)
//                .withLanguage(FlexibleSearchLanguage.getInstance())
//                            .andNot(psiElement().withParents(
//                                FlexibleSearchTableName.class,
//                                FlexibleSearchFromClause.class,
//                                FlexibleSearchWhereClause.class
//                            ))
//                            .andNot(psiElement().inside(psiElement(COLUMN_REFERENCE)))
//                            .andNot(psiElement().inside(psiElement(TABLE_NAME_IDENTIFIER)))
//            /*.andNot(psiElement().inside(psiElement(COLUMN_REFERENCE_IDENTIFIER)))*/,
//            FxsKeywordCompletionProvider.Companion.getInstance()
//        );


//        extend(
//            CompletionType.BASIC,
//            PlatformPatterns.psiElement(FlexibleSearchTypes.IDENTIFIER)
//                .inside(psiElement(TABLE_NAME))
//                .withLanguage(FlexibleSearchLanguage.IN),
//            ItemCodeCompletionProvider.Companion.getInstance()
//        );
//        extend(
//            CompletionType.BASIC,
//            psiElement(TABLE_NAME_IDENTIFIER)
//                .inside(psiElement(TABLE_NAME))
//                .withLanguage(FlexibleSearchLanguage.getInstance()),
//            ItemCodeCompletionProvider.Companion.getInstance()
//        );

//        extend(
//            CompletionType.BASIC,
//            psiElement(COLUMN_REFERENCE_IDENTIFIER)
//                .inside(psiElement(COLUMN_ALIAS_REFERENCE))
//                .withLanguage(FlexibleSearchLanguage.getInstance()),
//            FxsColumnReferenceCompletionProvider.Companion.getInstance()
//        );

//        extend(
//            CompletionType.BASIC,
//            psiElement(TABLE_NAME_IDENTIFIER)
//                .inside(psiElement(TABLE_ALIAS_REFERENCE))
//                .withLanguage(FlexibleSearchLanguage.getInstance()),
//            FxsTableAliasReferenceCompletionProvider.Companion.getInstance()
//        );

//        extend(
//            CompletionType.BASIC,
//            psiElement()
//                .afterLeaf(psiElement().withElementType(TokenSet.create(TABLE_NAME_IDENTIFIER)))
//                .withLanguage(FlexibleSearchLanguage.getInstance()),
//            new FSKeywordCompletionProvider(newHashSet("AS"), (keyword) ->
//                LookupElementBuilder.create(keyword)
//                                    .withCaseSensitivity(false)
//                                    .withIcon(AllIcons.Nodes.Function))
//        );


//        extend(
//            CompletionType.BASIC,
//            psiElement()
//                .inside(psiElement(SELECT_LIST))
//                .withLanguage(FlexibleSearchLanguage.getInstance())
//                .andNot(psiElement().inside(psiElement(COLUMN_REFERENCE))),
//            new FSKeywordCompletionProvider(newHashSet("*", "DISTINCT", "COUNT"), (keyword) ->
//                LookupElementBuilder.create(keyword)
//                                    .bold()
//                                    .withCaseSensitivity(false)
//                                    .withIcon(AllIcons.Nodes.Static))
//        );
    }

    companion object {
        const val DUMMY_IDENTIFIER = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    }
}