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
package com.intellij.idea.plugin.hybris.impex.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.idea.plugin.hybris.impex.codeInsight.lookup.ImpExLookupElementFactory
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.util.ProcessingContext

@Service
class ImpexHeaderAttributeModifierNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val element = parameters.position
        val completionSettings = DeveloperSettingsComponent.getInstance(element.project).state
            .impexSettings
            .completion
        AttributeModifier.entries
            .map { ImpExLookupElementFactory.build(element, it, completionSettings) }
            .let { result.addAllElements(it) }
    }

    companion object {
        @JvmStatic
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(ImpexHeaderAttributeModifierNameCompletionProvider::class.java)
    }
}