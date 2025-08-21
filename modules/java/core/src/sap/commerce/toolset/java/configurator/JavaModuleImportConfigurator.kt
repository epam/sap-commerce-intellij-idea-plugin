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
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.impl.storage.ClassPathStorageUtil
import com.intellij.openapi.roots.impl.storage.ClasspathStorage
import sap.commerce.toolset.i18n
import sap.commerce.toolset.java.configurator.ex.CompilerOutputPathsConfigurator
import sap.commerce.toolset.java.configurator.ex.JavadocSettingsConfigurator
import sap.commerce.toolset.java.configurator.ex.LibRootsConfigurator
import sap.commerce.toolset.java.configurator.ex.ReadonlyConfigurator
import sap.commerce.toolset.project.configurator.ContentRootConfigurator
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor

class JavaModuleImportConfigurator : ModuleImportConfigurator {

    // TODO: handle CCv2 modules separately!, change this to !is ExternalModuleDescriptor
    override fun isApplicable(moduleDescriptor: ModuleDescriptor): Boolean = true

    override fun configure(
        indicator: ProgressIndicator,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        allYModules: Map<String, YModuleDescriptor>,
        rootProjectModifiableModel: ModifiableModuleModel,
        moduleDescriptor: ModuleDescriptor
    ): Module {
        indicator.text = i18n("hybris.project.import.module.import", moduleDescriptor.name)
        indicator.text2 = i18n("hybris.project.import.module.settings")

        val javaModule = rootProjectModifiableModel.newModule(
            moduleDescriptor.ideaModuleFile().absolutePath,
            StdModuleTypes.JAVA.id
        )

        ReadonlyConfigurator.configure(moduleDescriptor)

        val modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(javaModule);

        indicator.text2 = i18n("hybris.project.import.module.sdk");
        ClasspathStorage.setStorageType(modifiableRootModel, ClassPathStorageUtil.DEFAULT_STORAGE);

        modifiableRootModel.inheritSdk();

        JavadocSettingsConfigurator.configure(modifiableRootModel, moduleDescriptor)
        LibRootsConfigurator.configure(allYModules, modifiableRootModel, moduleDescriptor, modifiableModelsProvider, indicator);
        ContentRootConfigurator.configure(indicator, modifiableRootModel, moduleDescriptor);
        CompilerOutputPathsConfigurator.configure(indicator, modifiableRootModel, moduleDescriptor);

        indicator.text2 = i18n("hybris.project.import.module.facet");

        return javaModule
    }
}