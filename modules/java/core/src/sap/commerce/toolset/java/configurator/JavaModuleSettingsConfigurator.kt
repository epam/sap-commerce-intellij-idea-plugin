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
package sap.commerce.toolset.java.configurator

import com.intellij.java.workspace.entities.JavaModuleSettingsEntity
import com.intellij.java.workspace.entities.javaSettings
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import kotlin.io.path.pathString

class JavaModuleSettingsConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Java Settings"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(context: ProjectModuleConfigurationContext<ModuleDescriptor>) {
        val importContext = context.importContext
        val moduleDescriptor = context.moduleDescriptor
        val moduleEntity = context.moduleEntity
        val fakeOutputPath = importContext.settings.useFakeOutputPathForCustomExtensions
        val ootbReadonlyMode = importContext.settings.importOOTBModulesInReadOnlyMode

        val output = if (moduleDescriptor.type == ModuleDescriptorType.CUSTOM) {
            if (fakeOutputPath) ProjectConstants.Directory.ECLIPSE_BIN
            else ProjectConstants.Directory.CLASSES
        } else {
            if (ootbReadonlyMode || fakeOutputPath) ProjectConstants.Directory.ECLIPSE_BIN
            else ProjectConstants.Directory.CLASSES
        }

        val virtualFileUrlManager = context.importContext.workspace.getVirtualFileUrlManager()
        val outputDirectory = moduleDescriptor.moduleRootPath.resolve(output)
        val javaSettingsEntity = JavaModuleSettingsEntity(
            inheritedCompilerOutput = false,
            excludeOutput = true,
            entitySource = moduleEntity.entitySource
        ) {
            this.excludeOutput = true
            this.compilerOutput = virtualFileUrlManager.fromPath(outputDirectory.pathString)
            this.compilerOutputForTests = virtualFileUrlManager.fromPath(outputDirectory.pathString)
        }

        moduleEntity.javaSettings = javaSettingsEntity
    }
}
