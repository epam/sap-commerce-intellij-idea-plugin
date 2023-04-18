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

package com.intellij.idea.plugin.hybris.flexibleSearch.completion;

import com.intellij.codeInsight.completion.CompletionContributor;

public class FlexibleSearchCompletionContributor extends CompletionContributor {

    public FlexibleSearchCompletionContributor() {
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
}