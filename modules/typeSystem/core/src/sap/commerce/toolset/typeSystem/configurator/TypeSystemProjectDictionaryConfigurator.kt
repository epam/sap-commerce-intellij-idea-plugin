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
package sap.commerce.toolset.typeSystem.configurator

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.service
import com.intellij.spellchecker.state.ProjectDictionaryState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class TypeSystemProjectDictionaryConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Type System Dictionary"

    // TODO: will not work, we have to replace all words, because developer may remove a type
    override fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return

        CoroutineScope(Dispatchers.Default).launch {
            smartReadAction(project) {
                val projectDictionary = project.service<ProjectDictionaryState>()
                    .projectDictionary

                val tsNames = TSMetaModelAccess.getInstance(project).getAll()
                    .mapNotNull { it.name }
                    .toSet()
                    .onEach { tsName ->
                        projectDictionary.words
                            .firstOrNull { it.equals(tsName, true) }
                            ?.let { projectDictionary.removeFromDictionary(it) }
                    }

                projectDictionary.addToDictionary(tsNames)
            }
        }
    }
}
