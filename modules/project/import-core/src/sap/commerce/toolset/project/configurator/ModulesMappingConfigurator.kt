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
package sap.commerce.toolset.project.configurator

import com.intellij.platform.workspace.storage.MutableEntityStorage
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.settings.ySettings

/*
Modules mapping: [y] extension name <-> ModuleEntity.name.
Must be invoked as last step of the Workspace Storage configurators

Example:
    trainingcore - Custom.training.trainingcore
 */
class ModulesMappingConfigurator : ProjectStorageSaveConfigurator {

    override val name: String
        get() = "Modules mapping"

    override fun configure(context: ProjectImportContext, storage: MutableEntityStorage) {
        context.project.ySettings.module2extensionMapping = context.mutableStorage.modules.entries
            .associate { it.value.name to it.key.name }
    }
}
