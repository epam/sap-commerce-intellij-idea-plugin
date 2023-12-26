/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider.BSBeanClassCompletionProvider
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider.BSBeanPropertyTypeCompletionProvider
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider.BSEnumClassCompletionProvider
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider.BSHintNameCompletionProvider
import com.intellij.idea.plugin.hybris.system.bean.psi.BSPatterns
import com.intellij.patterns.PlatformPatterns

class BSCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.BEAN_CLASS),
            BSBeanClassCompletionProvider.getInstance()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.ENUM_CLASS),
            BSEnumClassCompletionProvider.getInstance()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.BEAN_EXTENDS),
            BSBeanClassCompletionProvider.getInstance()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.BEAN_PROPERTY_TYPE),
            BSBeanPropertyTypeCompletionProvider.getInstance()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.HINT_NAME),
            BSHintNameCompletionProvider.getInstance()
        )
    }
}