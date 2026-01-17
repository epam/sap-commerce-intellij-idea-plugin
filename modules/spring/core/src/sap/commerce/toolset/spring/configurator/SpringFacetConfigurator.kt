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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.spring.contexts.model.LocalXmlModel
import com.intellij.spring.facet.SpringFacet
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import java.io.File

class SpringFacetConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Spring Facets"

    override fun configure(
        context: ProjectPostImportContext,
        legacyWorkspace: IdeModifiableModelsProvider,
        edtActions: MutableList<() -> Unit>
    ) {
        if (Plugin.SPRING.isDisabled()) return

        val moduleFacets = context.chosenHybrisModuleDescriptors.mapNotNull { moduleDescriptor ->
            val module = context.modules[moduleDescriptor.name] ?: run {
                thisLogger().warn("Could not find module for ${moduleDescriptor.name}")
                return@mapNotNull null
            }

            val facet = when (moduleDescriptor) {
                is YBackofficeSubModuleDescriptor -> null
                is PlatformModuleDescriptor -> configure(moduleDescriptor, module, emptySet())
                is YModuleDescriptor -> configure(moduleDescriptor, module, moduleDescriptor.getSpringFiles())
                else -> null
            } ?: return@mapNotNull null

            legacyWorkspace.getModifiableFacetModel(module).addFacet(facet)

            moduleDescriptor to facet
        }
            .associate { it.first to it.second }

        context.chosenHybrisModuleDescriptors.forEach { moduleDescriptor ->
            configureDependencies(moduleDescriptor, moduleFacets)
        }
    }

    private fun configure(
        moduleDescriptor: ModuleDescriptor,
        module: Module,
        additionalFileSet: Set<String>
    ): SpringFacet? {
        val springFacet = SpringFacet.getSpringFacetType()
            .takeIf { it.isSuitableModuleType(ModuleType.get(module)) }
            ?.let { it.createFacet(module, it.defaultFacetName, it.createDefaultConfiguration(), null) }
            ?: return null

        val facetName = moduleDescriptor.name + " - " + SpringFacet.FACET_TYPE_ID
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

        springFacet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED)?.let {
            it.booleanValue = false
            it.apply()
        }

        return springFacet
    }

    private fun configureDependencies(
        moduleDescriptor: ModuleDescriptor,
        moduleFacets: Map<ModuleDescriptor, SpringFacet>,
    ) {
        val springFacet = moduleFacets[moduleDescriptor] ?: return
        val springFileSet = springFacet.fileSets.firstOrNull() ?: return

        moduleDescriptor.getDirectDependencies()
            .mapNotNull { moduleFacets[it] }
            .mapNotNull { it.fileSets.firstOrNull() }
            .forEach { springFileSet.addDependency(it) }
    }
}
