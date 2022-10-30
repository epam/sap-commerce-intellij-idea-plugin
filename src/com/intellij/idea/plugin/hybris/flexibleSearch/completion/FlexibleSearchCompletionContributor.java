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

package com.intellij.idea.plugin.hybris.flexibleSearch.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.idea.plugin.hybris.completion.provider.EnumTypeCodeCompletionProvider;
import com.intellij.idea.plugin.hybris.completion.provider.ItemTypeCodeCompletionProvider;
import com.intellij.idea.plugin.hybris.completion.provider.RelationTypeCodeCompletionProvider;
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage;
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FSFieldsCompletionProvider;
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FSKeywordCompletionProvider;
import com.intellij.idea.plugin.hybris.flexibleSearch.completion.provider.FSKeywords;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.COLUMN_REFERENCE_IDENTIFIER;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.TABLE_NAME_IDENTIFIER;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class FlexibleSearchCompletionContributor extends CompletionContributor {

    private static final Logger LOG = Logger.getInstance(FlexibleSearchCompletionContributor.class);

    public FlexibleSearchCompletionContributor() {
        // keywords
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiElement.class)
                            .withLanguage(FlexibleSearchLanguage.getInstance())
//                            .andNot(psiElement().withParents(
//                                FlexibleSearchTableName.class,
//                                FlexibleSearchFromClause.class,
//                                FlexibleSearchWhereClause.class
//                            ))
//                            .andNot(psiElement().inside(psiElement(COLUMN_REFERENCE)))
//                            .andNot(psiElement().inside(psiElement(TABLE_NAME_IDENTIFIER)))
                /*.andNot(psiElement().inside(psiElement(COLUMN_REFERENCE_IDENTIFIER)))*/,
            new FSKeywordCompletionProvider(FSKeywords.topLevelKeywords(), (keyword) ->
                LookupElementBuilder.create(keyword)
                                    .withCaseSensitivity(false)
                                    .withIcon(AllIcons.Nodes.Function))
        );

        extend(
            CompletionType.BASIC,
            psiElement()
                .withElementType(TokenSet.create(TABLE_NAME_IDENTIFIER))
                .withLanguage(FlexibleSearchLanguage.getInstance()),
            ItemTypeCodeCompletionProvider.getInstance()
        );

        extend(
            CompletionType.BASIC,
            psiElement()
                .withElementType(TokenSet.create(TABLE_NAME_IDENTIFIER))
                .withLanguage(FlexibleSearchLanguage.getInstance()),
            EnumTypeCodeCompletionProvider.getInstance()
        );

        extend(
            CompletionType.BASIC,
            psiElement()
                .withElementType(TokenSet.create(TABLE_NAME_IDENTIFIER))
                .withLanguage(FlexibleSearchLanguage.getInstance()),
            RelationTypeCodeCompletionProvider.getInstance()
        );

        extend(
            CompletionType.BASIC,
            psiElement().inside(psiElement(COLUMN_REFERENCE_IDENTIFIER))
                .withLanguage(FlexibleSearchLanguage.getInstance()),
            FSFieldsCompletionProvider.Companion.getInstance()
        );

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