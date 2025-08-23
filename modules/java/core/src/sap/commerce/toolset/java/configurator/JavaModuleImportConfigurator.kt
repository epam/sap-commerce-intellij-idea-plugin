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

package sap.commerce.toolset.java.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.roots.impl.storage.ClassPathStorageUtil
import com.intellij.openapi.roots.impl.storage.ClasspathStorage
import sap.commerce.toolset.java.configurator.ex.*
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor

class JavaModuleImportConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Java Modules"

    override fun isApplicable(moduleDescriptor: ModuleDescriptor) = moduleDescriptor !is ExternalModuleDescriptor

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptor: ModuleDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        rootProjectModifiableModel: ModifiableModuleModel
    ): Module {
        val javaModule = rootProjectModifiableModel.newModule(
            moduleDescriptor.ideaModuleFile().absolutePath,
            StdModuleTypes.JAVA.id
        )

        ReadonlyConfiguratorEx.configure(moduleDescriptor)

        val modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(javaModule);

        ClasspathStorage.setStorageType(modifiableRootModel, ClassPathStorageUtil.DEFAULT_STORAGE);

        modifiableRootModel.inheritSdk();

        val yModuleDescriptorsToImport = hybrisProjectDescriptor.yModuleDescriptorsToImport

        JavadocSettingsConfiguratorEx.configure(modifiableRootModel, moduleDescriptor)
        LibRootsConfiguratorEx.configure(yModuleDescriptorsToImport, modifiableRootModel, moduleDescriptor, modifiableModelsProvider);
        ContentRootConfiguratorEx.configure(modifiableRootModel, moduleDescriptor);
        CompilerOutputPathsConfiguratorEx.configure( modifiableRootModel, moduleDescriptor);

        return javaModule
    }
}