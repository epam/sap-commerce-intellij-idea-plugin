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

package sap.commerce.toolset.project.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.extensioninfo.model.ExtensionInfo
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings

class RequiredExtensionsNameCompletionProvider : ExtensionNameCompletionProvider() {

    override fun getExtensionDescriptors(parameters: CompletionParameters, project: Project): Collection<ExtensionDescriptor> {
        val file = parameters.originalFile
        if (file !is XmlFile) return emptyList()

        val currentNames = DomManager.getDomManager(project)
                .getFileElement(file, ExtensionInfo::class.java)
                ?.rootElement
                ?.extension
                ?.requiresExtensions
                ?.mapNotNull { it.name.stringValue }
                ?.filter { it.isNotBlank() }
                ?.map { it.lowercase() } ?: emptyList()

        return ProjectSettings.getInstance(project)
                .availableExtensions
                .entries
                .filterNot { currentNames.contains(it.key) }
                .map { it.value }
    }
}