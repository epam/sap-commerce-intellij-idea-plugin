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

package sap.commerce.toolset.impex.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.tree.TokenSet
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.codeInsight.completion.provider.*
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExFullHeaderType
import sap.commerce.toolset.impex.psi.ImpExModifiers
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.typeSystem.codeInsight.completion.provider.ItemCodeCompletionProvider

class ImpExCompletionContributor : CompletionContributor() {
    init {
        // case: header type modifier -> attribute_name
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.ATTRIBUTE_NAME)
                .inside(ImpExFullHeaderType::class.java)
                .inside(ImpExModifiers::class.java),
            ImpExHeaderTypeModifierNameCompletionProvider()
        )

        // case: header attribute's modifier name -> attribute_name
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.ATTRIBUTE_NAME)
                .inside(ImpExFullHeaderParameter::class.java)
                .inside(ImpExModifiers::class.java),
            ImpExHeaderAttributeModifierNameCompletionProvider()
        )

        // case: header type value -> attribute_value
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.ATTRIBUTE_VALUE)
                .inside(ImpExFullHeaderType::class.java)
                .inside(ImpExModifiers::class.java),
            ImpExHeaderTypeModifierValueCompletionProvider()
        )

        // case: header attribute's modifier value -> attribute_value
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.ATTRIBUTE_VALUE)
                .inside(ImpExFullHeaderParameter::class.java)
                .inside(ImpExModifiers::class.java),
            ImpExHeaderAttributeModifierValueCompletionProvider()
        )

        // case: itemtype-code
        // case: enumtype-code
        // case: relationtype-code
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.HEADER_TYPE),
            ItemCodeCompletionProvider()
        )

        // case: item's attribute
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.HEADER_PARAMETER_NAME)
                .andNot(PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement().withElementType(ImpExTypes.PARAMETER))),
            ImpExHeaderItemTypeAttributeNameCompletionProvider()
        )
        // case: item's attribute
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withParent(PlatformPatterns.psiElement().withElementType(ImpExTypes.PARAMETER))
                .and(PlatformPatterns.psiElement().withElementType(ImpExTypes.HEADER_PARAMETER_NAME)),
            ImpExHeaderItemTypeParameterNameCompletionProvider()
        )
        // case: impex keywords
        extend(
            CompletionType.BASIC,
            topLevel(),
            ImpExKeywordModeCompletionProvider()
        )

        // case: macros keywords
        extend(
            CompletionType.BASIC,
            topLevel(),
            ImpExKeywordMacroCompletionProvider()
        )

        // case: impex macros
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.MACRO_USAGE),
            ImpExMacrosCompletionProvider()
        )

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .inside(PlatformPatterns.psiElement().withElementType(TokenSet.create(ImpExTypes.MACRO_USAGE, ImpExTypes.MACRO_DECLARATION))),
            ImpExMacrosConfigCompletionProvider()
        )
    }

    private fun topLevel() = PlatformPatterns.psiElement()
        .withLanguage(ImpExLanguage)
        .andNot(
            PlatformPatterns.psiElement() // FIXME bad code, but working
                .andOr(
                    PlatformPatterns.psiElement(ImpExTypes.HEADER_TYPE),
                    PlatformPatterns.psiElement(ImpExTypes.MACRO_NAME_DECLARATION),
                    PlatformPatterns.psiElement(ImpExTypes.ROOT_MACRO_USAGE),
                    PlatformPatterns.psiElement(ImpExTypes.MACRO_DECLARATION),
                    PlatformPatterns.psiElement(ImpExTypes.ASSIGN_VALUE),
                    PlatformPatterns.psiElement(ImpExTypes.MACRO_VALUE),
                    PlatformPatterns.psiElement(ImpExTypes.ATTRIBUTE),
                    PlatformPatterns.psiElement(ImpExTypes.HEADER_TYPE_NAME),
                    PlatformPatterns.psiElement(ImpExTypes.HEADER_PARAMETER_NAME),
                    PlatformPatterns.psiElement(ImpExTypes.ATTRIBUTE_NAME),
                    PlatformPatterns.psiElement(ImpExTypes.FIELD_VALUE),
                    PlatformPatterns.psiElement(ImpExTypes.ATTRIBUTE_VALUE)
                )
        )
}