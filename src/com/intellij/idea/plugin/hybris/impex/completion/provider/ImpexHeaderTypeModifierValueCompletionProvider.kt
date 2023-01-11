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
package com.intellij.idea.plugin.hybris.impex.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexProcessorModifier.ImpexProcessorModifierValue
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class ImpexHeaderTypeModifierValueCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val psiElementUnderCaret = parameters.position
        val impexAttribute = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpexAttribute::class.java) ?: return
        val modifierName = impexAttribute.anyAttributeName.text
        val impexModifier = TypeModifier.getByModifierName(modifierName)

        if (impexModifier != null) {
            addCompletions(impexModifier, result)
        } else {
            // show error message when not defined within hybris API
            Notifications.create(
                NotificationType.WARNING,
                HybrisI18NBundleUtils.message("hybris.completion.error.impex.title"),
                HybrisI18NBundleUtils.message("hybris.completion.error.impex.unknownTypeModifier.content", modifierName)
            )
                .notify(parameters.position.project)
        }
    }

    private fun addCompletions(
        impexModifier: ImpexModifier,
        result: CompletionResultSet
    ) {
        if (TypeModifier.PROCESSOR == impexModifier) {
            val modifierValues = (impexModifier as TypeModifier).rawModifierValues
            modifierValues
                .filterIsInstance<ImpexProcessorModifierValue>()
                .map { it.psiClass }
                .filter { it.qualifiedName != null && it.name != null }
                .map {
                    LookupElementBuilder.create(it.qualifiedName!!)
                        .withPresentableText(it.name!!)
                        .withIcon(HybrisIcons.ITEM)
                }
                .forEach { result.addElement(it) }
        } else {
            impexModifier.modifierValues
                .map { LookupElementBuilder.create(it) }
                .forEach { result.addElement(it) }
        }
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(ImpexHeaderTypeModifierValueCompletionProvider::class.java)
    }
}