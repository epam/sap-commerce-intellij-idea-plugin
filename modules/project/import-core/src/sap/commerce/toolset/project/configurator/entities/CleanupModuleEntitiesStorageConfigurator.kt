/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.project.configurator.entities

import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.entities
import sap.commerce.toolset.project.configurator.ProjectStorageCleanupConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.settings.ySettings

class CleanupModuleEntitiesStorageConfigurator : ProjectStorageCleanupConfigurator {

    override val name: String
        get() = "Cleanup Modules"

    override fun configure(context: ProjectImportContext, storage: MutableEntityStorage) {
        // idea module name <-> extension name
        val previouslyLoadedExtensions = context.project.ySettings.module2extensionMapping

        val yExtensionNames = context.chosenHybrisModuleDescriptors
            .map { it.name }

        storage.entities<ModuleEntity>()
            .forEach { moduleEntity ->
                val extensionName = previouslyLoadedExtensions[moduleEntity.name]

                when {
                    // remove external module when requested
                    extensionName == null && context.removeExternalModules -> storage.removeEntity(moduleEntity)
                    // remove NOT selected for import (localextensions.xml and dependencies)
                    extensionName != null && !yExtensionNames.contains(extensionName) -> storage.removeEntity(moduleEntity)
                }
            }
    }
}
