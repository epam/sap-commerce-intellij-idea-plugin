/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.provider.*
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPatterns
import com.intellij.patterns.PlatformPatterns

class CngCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.ITEM_TYPE),
            CngItemTypeCodeCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.CONTEXT_PARENT_NON_ITEM_TYPE),
            CngContextParentNonItemTypeCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.FLOW_STEP_CONTENT_PROPERTY_LIST_PROPERTY_QUALIFIER),
            CngFlowPropertyListPropertyQualifierCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.FLOW_INITIALIZE_TYPE),
            CngFlowTypeCodeCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.FLOW_PROPERTY_LIST_ROOT),
            CngFlowInitializePropertyCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.EDITOR_DEFINITION),
            CngEditorDefinitionCodeCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.ACTION_DEFINITION),
            CngActionDefinitionCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.WIDGET_DEFINITION),
            CngWidgetDefinitionCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.WIDGET_CONNECTION_WIDGET_ID),
            CngWidgetConnectionWidgetIdCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.WIDGET_ID),
            CngWidgetIdCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.WIDGET_SETTING),
            CngWidgetSettingCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(CngPatterns.WIDGET_COMPONENT_RENDERER_CLASS),
            CngWidgetComponentRendererClassCompletionProvider()
        )
    }
}