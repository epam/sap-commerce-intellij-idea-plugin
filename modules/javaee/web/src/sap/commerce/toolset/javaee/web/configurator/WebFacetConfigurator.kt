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
package sap.commerce.toolset.javaee.web.configurator

import com.intellij.facet.FacetTypeRegistry
import com.intellij.javaee.DeploymentDescriptorsConstants
import com.intellij.javaee.web.facet.WebFacet
import com.intellij.javaee.web.facet.WebFacetType
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.util.descriptors.ConfigFileInfo
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag
import sap.commerce.toolset.util.toSystemIndependentName

/**
 * No need to migrate to the new Workspace API or use the {@code webSettings} storage builder at the moment.
 * See <a href="https://platform.jetbrains.com/t/how-to-properly-manage-javasettings-and-websettings-in-workspace-api/3471">How to properly manage javaSettings and webSettings in Workspace API</a>.
 */
class WebFacetConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Web Facets"

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        val moduleEntities = workspaceModel.currentSnapshot.entities(ModuleEntity::class.java)
            .associateBy { it.name }

        val facetEntities = importContext.chosenHybrisModuleDescriptors.mapNotNull { moduleDescriptor ->
            val ideaModuleName = moduleDescriptor.ideaModuleName()
            val javaModule = ModuleManager.getInstance(importContext.project).findModuleByName(ideaModuleName)
                ?: return@mapNotNull null
            val moduleEntity = moduleEntities[ideaModuleName]
                ?: return@mapNotNull null
            val facetEntity = configure(moduleDescriptor, moduleEntity, javaModule)
                ?: return@mapNotNull null

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

    private suspend fun configure(
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity,
        javaModule: Module,
    ): FacetEntityBuilder? {
        val webRoot = when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YAcceleratorAddonSubModuleDescriptor -> moduleDescriptor.webRoot
            else -> return null
        }

        val facetType = FacetTypeRegistry.getInstance().findFacetType(WebFacet.ID)
            .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
            ?: return null
        val facet = facetType.createFacet(
            javaModule,
            facetType.defaultFacetName,
            facetType.createDefaultConfiguration(), null
        )

        val sourceRoots = moduleEntity.sourceRoots.map { it.url.url }

        facet.addWebRootNoFire(VfsUtil.pathToUrl(webRoot.toSystemIndependentName), "/")
        facet.webConfiguration.sourceRoots = sourceRoots

        VfsUtil.findFile(moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_WEB_XML), true)
            ?.let { ConfigFileInfo(DeploymentDescriptorsConstants.WEB_XML_META_DATA, it.url) }
            ?.let {
                edtWriteAction {
                    facet.descriptorsContainer.configuration.addConfigFile(it)
                }
            }

        val configurationXmlTag = facet.configurationXmlTag ?: return null
        val facetEntityTypeId = FacetEntityTypeId(WebFacetType.getInstance().stringId)
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
