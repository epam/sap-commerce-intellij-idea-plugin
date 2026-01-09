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

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.modifyFacetEntity
import com.intellij.spring.facet.SpringFacet
import com.intellij.spring.facet.SpringFacetType
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag

class SpringDependenciesConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Spring Dependencies"

    private data class ModuleFacetContext(
        val moduleEntity: ModuleEntity,
        val facetEntity: FacetEntity,
        val facet: SpringFacet,
        var newConfigurationXmlTag: String? = null
    )

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        if (Plugin.SPRING.isDisabled()) return

        val moduleEntities = workspaceModel.currentSnapshot.entities(ModuleEntity::class.java)
            .mapNotNull { moduleEntity ->
                val javaModule = moduleEntity.findModule(workspaceModel.currentSnapshot)
                    ?: return@mapNotNull null
                val springFacet = SpringFacet.getInstance(javaModule)
                    ?: return@mapNotNull null
                val facetEntity = moduleEntity.facets
                    .find { it.name == SpringFacetType.SPRING_FACET_TYPE__STRING_ID }
                    ?: return@mapNotNull null
                ModuleFacetContext(moduleEntity, facetEntity, springFacet)
            }
            .associateBy { it.moduleEntity.name }

        val updateEntities = importContext.chosenHybrisModuleDescriptors
            .mapNotNull { configureFacetDependencies(it, moduleEntities) }

        workspaceModel.update("Updating spring facet dependencies") { storage ->
            updateEntities.forEach { context ->
                val configurationXmlTag = context.newConfigurationXmlTag ?: return@forEach
                val facetEntity = context.facetEntity

                storage.modifyFacetEntity(facetEntity) {
                    this.configurationXmlTag = configurationXmlTag
                }
            }
        }
    }

    private fun configureFacetDependencies(
        moduleDescriptor: ModuleDescriptor,
        moduleEntities: Map<String, ModuleFacetContext>,
    ): ModuleFacetContext? {
        val context = moduleEntities[moduleDescriptor.ideaModuleName()] ?: return null
        val springFacet = context.facet
        val springFileSet = springFacet.fileSets.firstOrNull() ?: return null

        moduleDescriptor.getDirectDependencies()
            .mapNotNull { moduleEntities[it.ideaModuleName()] }
            .mapNotNull { it.facet.fileSets.firstOrNull() }
            .forEach { springFileSet.addDependency(it) }

        val configurationXmlTag = springFacet.configurationXmlTag ?: return null

        return context.also { it.newConfigurationXmlTag = configurationXmlTag }
    }
}
