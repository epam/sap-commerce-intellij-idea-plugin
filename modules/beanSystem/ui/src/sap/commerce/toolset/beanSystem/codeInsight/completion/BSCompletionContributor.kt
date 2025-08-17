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

package sap.commerce.toolset.beanSystem.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import sap.commerce.toolset.beanSystem.codeInsight.completion.provider.BSBeanClassCompletionProvider
import sap.commerce.toolset.beanSystem.codeInsight.completion.provider.BSBeanPropertyTypeCompletionProvider
import sap.commerce.toolset.beanSystem.codeInsight.completion.provider.BSEnumClassCompletionProvider
import sap.commerce.toolset.beanSystem.codeInsight.completion.provider.BSHintNameCompletionProvider
import sap.commerce.toolset.beanSystem.psi.BSPatterns

class BSCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().inside(BSPatterns.BEAN_CLASS),
                PlatformPatterns.psiElement().inside(BSPatterns.BEAN_EXTENDS)
            ),
            BSBeanClassCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.ENUM_CLASS),
            BSEnumClassCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.BEAN_PROPERTY_TYPE),
            BSBeanPropertyTypeCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(BSPatterns.HINT_NAME),
            BSHintNameCompletionProvider()
        )
    }
}