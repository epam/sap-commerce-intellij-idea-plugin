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
package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext

@Service
class CngContextParentNonItemTypeCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val project = parameters.editor.project ?: return
        val resultCaseInsensitive = result.caseInsensitive()

        val mergeBy = parameters.position.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "context" }
            ?.getAttributeValue(Context.MERGE_BY)
            ?: return
        CngMetaModelAccess.getInstance(project).getMetaModel().contextAttributes[mergeBy]
            ?.map { LookupElementBuilder.create(it) }
            ?.forEach { resultCaseInsensitive.addElement(it) }
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> =
            ApplicationManager.getApplication().getService(CngContextParentNonItemTypeCompletionProvider::class.java)
    }
}