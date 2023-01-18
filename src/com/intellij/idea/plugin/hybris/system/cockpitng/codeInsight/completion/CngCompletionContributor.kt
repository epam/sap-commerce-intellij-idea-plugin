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
package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.ItemTypeCodeCompletionProvider
import com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.provider.CngItemAttributeCodeCompletionProvider
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPatterns
import com.intellij.patterns.PlatformPatterns

class CngCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.CONTEXT_TYPE),
            ItemTypeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.FLOW_STEP_CONTENT_PROPERTY_TYPE),
            ItemTypeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.TREE_NODE_TYPE_CODE),
            ItemTypeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.CONTEXT_PARENT),
            ItemTypeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.LIST_VIEW_COLUMN_QUALIFIER),
            CngItemAttributeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.EDITOR_ATTRIBUTE),
            CngItemAttributeCodeCompletionProvider.instance
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.ADVANCED_SEARCH_FIELD_NAME),
            CngItemAttributeCodeCompletionProvider.instance
        )
    }
}