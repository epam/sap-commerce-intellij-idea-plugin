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

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.spring.contexts.model.LocalXmlModel
import com.intellij.spring.facet.SpringFacet
import com.intellij.spring.facet.SpringFacetType
import com.intellij.util.io.URLUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag
import java.io.File

class SpringFacetConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Spring Facets"

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        if (Plugin.SPRING.isDisabled()) return

        val moduleEntities = workspaceModel.currentSnapshot.entities(ModuleEntity::class.java)
            .associateBy { it.name }
        val facetEntities = importContext.chosenHybrisModuleDescriptors.mapNotNull { moduleDescriptor ->
            val ideaModuleName = moduleDescriptor.ideaModuleName()
            val javaModule = ModuleManager.getInstance(importContext.project).findModuleByName(ideaModuleName)
                ?: return@mapNotNull null
            val moduleEntity = moduleEntities[ideaModuleName]
                ?: return@mapNotNull null

            val facetEntity = when (moduleDescriptor) {
                is YBackofficeSubModuleDescriptor -> null
                is PlatformModuleDescriptor -> configure(moduleDescriptor, moduleEntity, javaModule, emptySet())
                is YModuleDescriptor -> configure(moduleDescriptor, moduleEntity, javaModule, moduleDescriptor.getSpringFiles())
                else -> null
            } ?: return@mapNotNull null

            moduleEntity to facetEntity
        }
            .associate { it.first to it.second }

        workspaceModel.update("Configure Spring Facets") { storage ->
            facetEntities.forEach { (moduleEntity, facetEntity) ->
                storage.modifyModuleEntity(moduleEntity) {
                    this.facets += facetEntity
                }
            }
        }
    }

    private fun configure(
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity,
        javaModule: Module,
        additionalFileSet: Set<String>
    ): FacetEntityBuilder? {
        val facetType = SpringFacet.getSpringFacetType()
            .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
            ?: return null
        val facet = facetType.createFacet(
            javaModule,
            facetType.defaultFacetName,
            facetType.createDefaultConfiguration(), null
        )

        val facetName = moduleDescriptor.name + " - " + SpringFacet.FACET_TYPE_ID
        // dirty hack to trick IDEA autodetection of the web Spring context, see https://youtrack.jetbrains.com/issue/IDEA-257819
        // and WebXmlSpringWebModelContributor
        val facetId = if (moduleDescriptor is YWebSubModuleDescriptor) HybrisConstants.SPRING_WEB_FILE_SET_NAME
        else facetName

        val springFileSet = facet.addFileSet(facetId, facetName)
        springFileSet.isAutodetected = true

        additionalFileSet
            .mapNotNull { VfsUtil.findFileByIoFile(File(it), true) }
            .forEach { springFileSet.addFile(it) }
        additionalFileSet
            // jar://
            .filter { it.startsWith("${URLUtil.JAR_PROTOCOL}${URLUtil.SCHEME_SEPARATOR}") }
            .forEach { springFileSet.addFile(it) }

        facet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED)
            ?.let {

                it.booleanValue = false
                it.apply()
            }

        val configurationXmlTag = facet.configurationXmlTag ?: return null
        val facetEntityTypeId = FacetEntityTypeId(SpringFacetType.SPRING_FACET_TYPE__STRING_ID)

        return FacetEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = facetType.presentableName,
            typeId = facetEntityTypeId,
            entitySource = moduleEntity.entitySource
        ) {
            this.configurationXmlTag = configurationXmlTag
        }
    }
}
