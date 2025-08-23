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
package sap.commerce.toolset.project.configurator

import com.intellij.openapi.components.service
import com.intellij.spellchecker.dictionary.UserDictionary
import com.intellij.spellchecker.state.ProjectDictionaryState
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

class ProjectDictionaryConfigurator : ProjectPreImportConfigurator {

    private val dictionaryName = "sap.commerce.toolset"

    override val name: String
        get() = "Project Dictionaries"

    override fun preConfigure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val moduleNames = hybrisProjectDescriptor.foundModules
            .map { it.name.lowercase() }
            .toSet()
        val projectDictionary = project.service<ProjectDictionaryState>()
            .getProjectDictionary()

        projectDictionary.editableWords //ensure dictionaries exist

        val hybrisDictionary = projectDictionary.dictionaries
            .firstOrNull { it.name == dictionaryName }
            ?: UserDictionary(dictionaryName).apply {
                projectDictionary.dictionaries.add(this)
            }
        hybrisDictionary.addToDictionary(dictionaryName)
        hybrisDictionary.addToDictionary(project.name.lowercase())
        hybrisDictionary.addToDictionary(moduleNames)
    }
}
