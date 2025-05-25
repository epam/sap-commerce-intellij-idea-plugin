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
package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.lookup.CngLookupElementFactory
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelStateService
import com.intellij.openapi.components.service
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext

class CngWidgetSettingCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val project = parameters.editor.project ?: return

        val widgetDefinitionId = parameters.position.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "widget" }
            ?.getAttributeValue("widgetDefinitionId")
            ?: return

        val resultCaseInsensitive = result.caseInsensitive()

        commonSettings
            .map {
                LookupElementBuilder.create(it)
                    .withTypeText("java.lang.String", true)
            }
            .forEach { resultCaseInsensitive.addElement(it) }

        project.service<CngMetaModelStateService>().get()
            .widgetDefinitions[widgetDefinitionId]
            ?.settings
            ?.values
            ?.map { CngLookupElementFactory.build(it) }
            ?.forEach { resultCaseInsensitive.addElement(it) }
    }

    companion object {
        val commonSettings = setOf(
            "widgetStyleClass".lowercase(),
            "widgetStyleAttribute".lowercase(),
            "_width",
            "_height"
        )
    }

}