/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.impex.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.notification.NotificationType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAttribute

class ImpExHeaderTypeModifierValueCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val project = parameters.position.project
        val psiElementUnderCaret = parameters.position
        val impexAttribute = PsiTreeUtil.getParentOfType(psiElementUnderCaret, ImpExAttribute::class.java) ?: return
        val modifierName = impexAttribute.anyAttributeName.text
        val impexModifier = TypeModifier.getModifier(modifierName)

        if (impexModifier != null) {
            impexModifier.getLookupElements(project)
                .forEach { result.addElement(it) }
        } else {
            // show an error message when not defined within hybris API
            Notifications.create(
                NotificationType.WARNING,
                i18n("hybris.completion.error.impex.title"),
                i18n("hybris.completion.error.impex.unknownTypeModifier.content", modifierName)
            )
                .notify(parameters.position.project)
        }
    }
}