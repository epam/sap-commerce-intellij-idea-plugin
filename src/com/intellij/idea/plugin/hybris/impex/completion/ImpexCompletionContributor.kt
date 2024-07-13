/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.ItemCodeCompletionProvider
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.completion.provider.*
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderType
import com.intellij.idea.plugin.hybris.impex.psi.ImpexModifiers
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.tree.TokenSet

class ImpexCompletionContributor : CompletionContributor() {
    init {
        // case: header type modifier -> attribute_name
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.ATTRIBUTE_NAME)
                .inside(ImpexFullHeaderType::class.java)
                .inside(ImpexModifiers::class.java),
            ImpexHeaderTypeModifierNameCompletionProvider()
        )

        // case: header attribute's modifier name -> attribute_name
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.ATTRIBUTE_NAME)
                .inside(ImpexFullHeaderParameter::class.java)
                .inside(ImpexModifiers::class.java),
            ImpexHeaderAttributeModifierNameCompletionProvider()
        )

        // case: header type value -> attribute_value
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.ATTRIBUTE_VALUE)
                .inside(ImpexFullHeaderType::class.java)
                .inside(ImpexModifiers::class.java),
            ImpexHeaderTypeModifierValueCompletionProvider()
        )

        // case: header attribute's modifier value -> attribute_value
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.ATTRIBUTE_VALUE)
                .inside(ImpexFullHeaderParameter::class.java)
                .inside(ImpexModifiers::class.java),
            ImpexHeaderAttributeModifierValueCompletionProvider()
        )

        // case: itemtype-code
        // case: enumtype-code
        // case: relationtype-code
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.HEADER_TYPE),
            ItemCodeCompletionProvider()
        )

        // case: item's attribute
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.HEADER_PARAMETER_NAME)
                .andNot(PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement().withElementType(ImpexTypes.PARAMETER))),
            ImpexHeaderItemTypeAttributeNameCompletionProvider()
        )
        // case: item's attribute
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withParent(PlatformPatterns.psiElement().withElementType(ImpexTypes.PARAMETER))
                .and(PlatformPatterns.psiElement().withElementType(ImpexTypes.HEADER_PARAMETER_NAME)),
            ImpexHeaderItemTypeParameterNameCompletionProvider()
        )
        // case: impex keywords
        extend(
            CompletionType.BASIC,
            topLevel(),
            ImpexKeywordModeCompletionProvider()
        )

        // case: macros keywords
        extend(
            CompletionType.BASIC,
            topLevel(),
            ImpexKeywordMacroCompletionProvider()
        )

        // case: impex macros
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .withElementType(ImpexTypes.MACRO_USAGE),
            ImpexMacrosCompletionProvider()
        )

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpexLanguage)
                .inside(PlatformPatterns.psiElement().withElementType(TokenSet.create(ImpexTypes.MACRO_USAGE, ImpexTypes.MACRO_DECLARATION))),
            ImpexMacrosConfigCompletionProvider()
        )
    }

    private fun topLevel() = PlatformPatterns.psiElement()
        .withLanguage(ImpexLanguage)
        .andNot(
            PlatformPatterns.psiElement() // FIXME bad code, but working
                .andOr(
                    PlatformPatterns.psiElement(ImpexTypes.HEADER_TYPE),
                    PlatformPatterns.psiElement(ImpexTypes.MACRO_NAME_DECLARATION),
                    PlatformPatterns.psiElement(ImpexTypes.ROOT_MACRO_USAGE),
                    PlatformPatterns.psiElement(ImpexTypes.MACRO_DECLARATION),
                    PlatformPatterns.psiElement(ImpexTypes.ASSIGN_VALUE),
                    PlatformPatterns.psiElement(ImpexTypes.MACRO_VALUE),
                    PlatformPatterns.psiElement(ImpexTypes.ATTRIBUTE),
                    PlatformPatterns.psiElement(ImpexTypes.HEADER_TYPE_NAME),
                    PlatformPatterns.psiElement(ImpexTypes.HEADER_PARAMETER_NAME),
                    PlatformPatterns.psiElement(ImpexTypes.ATTRIBUTE_NAME),
                    PlatformPatterns.psiElement(ImpexTypes.FIELD_VALUE),
                    PlatformPatterns.psiElement(ImpexTypes.ATTRIBUTE_VALUE)
                )
        )
}
