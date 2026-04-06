/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.tree.TokenSet
import sap.commerce.toolset.impex.ImpExConstants
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

        // case: impex header mode keywords
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.VALUE_SUBTYPE),
            ImpExKeywordModeCompletionProvider()
        )

        // case: user rights keywords
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(ImpExTypes.VALUE_SUBTYPE),
            ImpExUserRightsCompletionProvider()
        )

        // case: impex macros
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(ImpExLanguage)
                .withElementType(
                    TokenSet.create(
                        ImpExTypes.FIELD_VALUE,
                        ImpExTypes.MACRO_VALUE,
                        ImpExTypes.STRING_LITERAL,
                        ImpExTypes.SCRIPT_BODY_VALUE,
                        ImpExTypes.HEADER_TYPE,
                        ImpExTypes.HEADER_PARAMETER_NAME,
                        ImpExTypes.SPECIAL_PARAMETER_VALUE,
                        ImpExTypes.ATTRIBUTE_VALUE,
                    )
                )
                .withText(
                    StandardPatterns.string().startsWith(ImpExConstants.MACRO_MARKER)
                        .andNot(StandardPatterns.string().startsWith(ImpExConstants.MACRO_CONFIG_COMPLETE_MARKER))
                ),
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
}
