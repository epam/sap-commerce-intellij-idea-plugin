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
package sap.commerce.toolset.angular.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.workspace.jps.entities.ModuleTypeId
import sap.commerce.toolset.angular.descriptor.AngularModuleDescriptor
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor

class AngularModuleImportConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Angular Modules"

    override fun isApplicable(moduleDescriptor: ModuleDescriptor) = moduleDescriptor is AngularModuleDescriptor

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptor: ModuleDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        rootProjectModifiableModel: ModifiableModuleModel
    ): Module {
        return rootProjectModifiableModel.newModule(
            moduleDescriptor.ideaModuleFile().absolutePath,
            ModuleTypeId("Angular").name
        )
            .apply {
                modifiableModelsProvider.getModifiableRootModel(this)
                    .addContentEntry(VfsUtil.pathToUrl(moduleDescriptor.moduleRootDirectory.absolutePath));
            }
    }
}
