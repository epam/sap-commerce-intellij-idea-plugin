/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.ProcessingContext
import com.intellij.util.xml.DomManager

class RequiredExtensionsNameCompletionProvider: CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val file = parameters.originalFile
        if (file !is XmlFile) return

        val project = file.project
        val currentNames = DomManager.getDomManager(project)
            .getFileElement(file, ExtensionInfo::class.java)
            ?.rootElement
            ?.extension
            ?.requiresExtensions
            ?.mapNotNull { it.name.stringValue }
            ?.filter { it.isNotBlank() }
            ?.map { it.lowercase() } ?: emptyList()

        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project).state
        val extensions = hybrisProjectSettings.completeSetOfAvailableExtensionsInHybris
        extensions
            .filterNot { currentNames.contains(it.lowercase()) }
            .forEach { result.addElement(LookupElementBuilder.create(it)) }
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> = ApplicationManager.getApplication().getService(RequiredExtensionsNameCompletionProvider::class.java)
    }
}