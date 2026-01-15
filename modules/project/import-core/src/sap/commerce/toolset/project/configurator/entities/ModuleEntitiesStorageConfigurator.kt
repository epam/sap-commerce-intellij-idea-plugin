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

import com.intellij.java.workspace.entities.javaSettings
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.modifyModuleEntity
import com.intellij.platform.workspace.jps.entities.sourceRoots
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.entities
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectStorageConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext

class ModuleEntitiesStorageConfigurator : ProjectStorageConfigurator {

    override val name: String
        get() = "Modules"

    override fun configure(context: ProjectImportContext, storage: MutableEntityStorage) {
        cleanupCurrentStorage(context, storage)

        val currentEntities = storage.entities<ModuleEntity>()
            // remove all facets, they will be re-created with legacy API in scope of the post-import configurator
            .onEach { currentEntity -> currentEntity.facets.forEach { storage.removeEntity(it) } }
            .mapNotNull { currentEntity ->
                val extensionName = currentEntity.javaSettings
                    ?.manifestAttributes[ProjectConstants.ModuleManifestAttribute.EXTENSION_NAME]
                    ?: return@mapNotNull null
                extensionName to currentEntity
            }
            .associate { it.first to it.second }

        context.mutableStorage.modules.forEach { newEntity ->
            val extensionName = newEntity.javaSettings
                ?.manifestAttributes[ProjectConstants.ModuleManifestAttribute.EXTENSION_NAME]
                ?: return@forEach
            val currentEntity = currentEntities[extensionName]

            if (currentEntity != null) {
                storage.modifyModuleEntity(currentEntity) {
                    this.entitySource = newEntity.entitySource
                    this.name = newEntity.name
                    this.type = newEntity.type
                    this.dependencies = newEntity.dependencies
                    this.facets = newEntity.facets
                    this.contentRoots = newEntity.contentRoots
                    this.sourceRoots = newEntity.sourceRoots
                    this.javaSettings = newEntity.javaSettings
                }
            } else {
                storage.addEntity(newEntity)
            }
        }
    }

    private fun cleanupCurrentStorage(context: ProjectImportContext, storage: MutableEntityStorage) {
        val yExtensionNames = context.chosenHybrisModuleDescriptors
            .map { it.name }

        storage.entities<ModuleEntity>()
            .forEach { moduleEntity ->
                val extensionName = moduleEntity.javaSettings
                    ?.manifestAttributes[ProjectConstants.ModuleManifestAttribute.EXTENSION_NAME]

                when {
                    // remove external module when requested
                    extensionName == null && context.removeExternalModules -> storage.removeEntity(moduleEntity)
                    // remove NOT selected for import (localextensions.xml and dependencies)
                    extensionName != null && !yExtensionNames.contains(extensionName) -> storage.removeEntity(moduleEntity)
                }
            }
    }
}
