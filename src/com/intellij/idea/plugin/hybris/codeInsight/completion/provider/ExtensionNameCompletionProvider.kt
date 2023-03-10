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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.ProcessingContext

open class ExtensionNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.originalFile.project
        getExtensionDescriptors(parameters, project)
                .map {
                    LookupElementBuilder.create(it.name)
                            .withTailText(tail(it), true)
                            .withTypeText(it.type.name, true)
                            .withIcon(
                                    when (it.type) {
                                        HybrisModuleDescriptorType.CCV2 -> HybrisIcons.EXTENSION_CLOUD
                                        HybrisModuleDescriptorType.CUSTOM -> HybrisIcons.EXTENSION_CUSTOM
                                        HybrisModuleDescriptorType.EXT -> HybrisIcons.EXTENSION_EXT
                                        HybrisModuleDescriptorType.OOTB -> HybrisIcons.EXTENSION_OOTB
                                        HybrisModuleDescriptorType.PLATFORM -> HybrisIcons.EXTENSION_PLATFORM
                                        else -> null
                                    }
                            )
                }
                .forEach { result.addElement(it) }
    }

    open fun getExtensionDescriptors(parameters: CompletionParameters, project: Project): Collection<ExtensionDescriptor> = HybrisProjectSettingsComponent.getInstance(project)
            .state
            .availableExtensions.values

    private fun tail(extensionDescriptor: ExtensionDescriptor): String? {
        val tail = listOfNotNull(
                if (extensionDescriptor.deprecated) "deprecated" else null,
                if (extensionDescriptor.extGenTemplateExtension) "template" else null,
                if (extensionDescriptor.addon) "addon" else null,
                if (extensionDescriptor.hacModule) "hac" else null,
                if (extensionDescriptor.backofficeModule) "backoffice" else null,
        ).joinToString(", ")
        return if (tail.isBlank()) null
        else " ($tail)"
    }

    companion object {
        val instance: CompletionProvider<CompletionParameters> =
                ApplicationManager.getApplication().getService(ExtensionNameCompletionProvider::class.java)
    }
}