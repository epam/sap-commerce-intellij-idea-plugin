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
package sap.commerce.toolset.spring.configurator

import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.spring.contexts.model.LocalXmlModel
import com.intellij.spring.facet.SpringFacet
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import java.io.File

class SpringFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Spring Facet"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        if (Plugin.SPRING.isDisabled()) return
        val modifiableFacetModel = modifiableModelsProvider.getModifiableFacetModel(module)

        when (moduleDescriptor) {
            is YBackofficeSubModuleDescriptor -> return
            is PlatformModuleDescriptor -> configure(module, moduleDescriptor, modifiableFacetModel, emptySet())
            is YModuleDescriptor -> configure(module, moduleDescriptor, modifiableFacetModel, moduleDescriptor.getSpringFiles())
        }
    }

    private suspend fun configure(
        javaModule: Module,
        moduleDescriptor: ModuleDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        additionalFileSet: Set<String>
    ) {
        backgroundWriteAction {
            val springFacet = SpringFacet.getInstance(javaModule)
                ?.also { it.removeFileSets() }
                ?: SpringFacet.getSpringFacetType()
                    .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
                    ?.let { it.createFacet(javaModule, it.defaultFacetName, it.createDefaultConfiguration(), null) }
                ?: return@backgroundWriteAction

            val facetName = moduleDescriptor.name + SpringFacet.FACET_TYPE_ID
            // dirty hack to trick IDEA autodetection of the web Spring context, see https://youtrack.jetbrains.com/issue/IDEA-257819
            // and WebXmlSpringWebModelContributor
            val facetId = if (moduleDescriptor is YWebSubModuleDescriptor) HybrisConstants.SPRING_WEB_FILE_SET_NAME
            else facetName

            val springFileSet = springFacet.addFileSet(facetId, facetName)
            springFileSet.isAutodetected = true

            additionalFileSet
                .mapNotNull { VfsUtil.findFileByIoFile(File(it), true) }
                .forEach { springFileSet.addFile(it) }
            additionalFileSet
                .filter { it.startsWith("jar://") }
                .forEach { springFileSet.addFile(it) }

            val setting = springFacet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED)
            if (setting != null) {
                setting.booleanValue = false
                setting.apply()
            }
            modifiableFacetModel.addFacet(springFacet)
        }
    }
}
