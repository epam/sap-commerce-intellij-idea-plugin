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

package sap.commerce.toolset.impex.codeInsight.lookup

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.codeInsight.completion.AutoPopupInsertHandler
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.constants.modifier.ImpExModifier
import sap.commerce.toolset.impex.constants.modifier.InterceptorProvider
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAttribute
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.settings.state.ImpExCompletionSettingsState

object ImpExLookupElementFactory {

    private const val PRIORITY_2_0 = 2.0
    private const val PRIORITY_1_0 = 1.0
    private const val PRIORITY_0_2 = 0.2
    private const val PRIORITY_0_1 = 0.1
    private const val MACRO_USAGE_GROUP = 1
    private const val HEADER_MODE_GROUP = 2

    fun build(
        element: PsiElement,
        modifier: TypeModifier,
        completionSettings: ImpExCompletionSettingsState
    ) = modifier.build(element, completionSettings)

    fun build(
        element: PsiElement,
        modifier: AttributeModifier,
        completionSettings: ImpExCompletionSettingsState
    ) = modifier.build(element, completionSettings)

    fun buildModifierValue(lookupElement: String) = LookupElementBuilder.create(lookupElement)

    fun buildModifierValue(lookupElement: String, typeText: String, presentableText: String = lookupElement) = LookupElementBuilder.create(lookupElement)
        .withPresentableText(presentableText)
        .withTypeText(typeText, true)

    fun buildInterceptor(bean: InterceptorProvider.Bean) = LookupElementBuilder.create(bean.name)
        .withIcon(HybrisIcons.TypeSystem.INTERCEPTOR)
        .withTypeIconRightAligned(true)
        .withTypeText(bean.className, HybrisIcons.Spring.BEAN, true)

    fun buildUserRights() = LookupElementBuilder.create(
        """
            ${'$'}START_USERRIGHTS
            Type;UID;MemberOfGroups;Password;Target;read;change;create;remove;change_perm
                ;   ;              ;        ;      ;    ;      ;      ;      ;
            ${'$'}END_USERRIGHTS
        """.trimIndent()
    )
        .withPresentableText("\$START_USERRIGHTS")
        .withIcon(HybrisIcons.ImpEx.USER_RIGHTS)
        .let { PrioritizedLookupElement.withPriority(it, PRIORITY_0_1) }
        .let { PrioritizedLookupElement.withGrouping(it, HEADER_MODE_GROUP) }

    fun buildMacro(lookupElement: String) = LookupElementBuilder.create(lookupElement)
        .withIcon(HybrisIcons.ImpEx.MACRO_USAGE)
        .withTypeText("macro", true)
        .let { PrioritizedLookupElement.withPriority(it, PRIORITY_1_0) }
        .let { PrioritizedLookupElement.withGrouping(it, MACRO_USAGE_GROUP) }

    fun buildMacroConfig() = LookupElementBuilder.create($$"$config-")
        .withIcon(HybrisIcons.ImpEx.MACRO_CONFIG)
        .withTypeText("config property access", true)
        .withInsertHandler(AutoPopupInsertHandler.INSTANCE)
        .let { PrioritizedLookupElement.withPriority(it, PRIORITY_2_0) }
        .let { PrioritizedLookupElement.withGrouping(it, MACRO_USAGE_GROUP) }

    fun buildMode(mode: String) = LookupElementBuilder.create("$mode ")
        .withPresentableText(mode)
        .withIcon(HybrisIcons.ImpEx.MODE)
        .withInsertHandler(AutoPopupInsertHandler.INSTANCE)
        .let { PrioritizedLookupElement.withPriority(it, PRIORITY_0_2) }
        .let { PrioritizedLookupElement.withGrouping(it, HEADER_MODE_GROUP) }

    private fun ImpExModifier.build(element: PsiElement, completionSettings: ImpExCompletionSettingsState) =
        if (completionSettings.addEqualsAfterModifier && !hasAssignValueLeaf(element))
            LookupElementBuilder.create("$modifierName=")
                .withIcon(HybrisIcons.ImpEx.MODIFIER)
                .withPresentableText(modifierName)
                .withTypeText(modifierMode.presentationText, modifierMode.icon, true)
                .withTypeIconRightAligned(true)
                .withInsertHandler(AutoPopupInsertHandler.INSTANCE)
        else LookupElementBuilder.create(modifierName)
            .withIcon(HybrisIcons.ImpEx.MODIFIER)
            .withPresentableText(modifierName)
            .withTypeText(modifierMode.presentationText, modifierMode.icon, true)
            .withTypeIconRightAligned(true)

    private fun hasAssignValueLeaf(element: PsiElement) = element.parentOfType<ImpExAttribute>()
        ?.childrenOfType<LeafPsiElement>()
        ?.any { it.elementType == ImpExTypes.ASSIGN_VALUE }
        ?: false

}