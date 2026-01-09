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
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.FacetEntityTypeId
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.util.descriptors.ConfigFileInfo
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_SOURCE_ROOT_ENTITY_TYPE_ID
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag
import sap.commerce.toolset.util.toSystemIndependentName

class WebFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Web Facets"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val javaModule = moduleEntity.findModule(workspaceModel.currentSnapshot) ?: return
        val webRoot = when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YAcceleratorAddonSubModuleDescriptor -> moduleDescriptor.webRoot
            else -> return
        }

        val facetType = FacetTypeRegistry.getInstance().findFacetType(WebFacet.ID)
            .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
            ?: return
        val facet = facetType.createFacet(
            javaModule,
            facetType.defaultFacetName,
            facetType.createDefaultConfiguration(), null
        )

        val sourceRoots = importContext.mutableStorage.contentRootEntities[moduleEntity]
            ?.flatMap { it.sourceRoots }
            ?.filter { it.rootTypeId == JAVA_SOURCE_ROOT_ENTITY_TYPE_ID }
            ?.map { it.url.url }
            ?: listOf()

        facet.addWebRootNoFire(VfsUtil.pathToUrl(webRoot.toSystemIndependentName), "/")
        facet.webConfiguration.sourceRoots = sourceRoots

        VfsUtil.findFile(moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_WEB_XML), true)
            ?.let { ConfigFileInfo(DeploymentDescriptorsConstants.WEB_XML_META_DATA, it.url) }
            ?.let {
                edtWriteAction {
                    facet.descriptorsContainer.configuration.addConfigFile(it)
                }
            }

        val configurationXmlTag = facet.configurationXmlTag ?: return
        val facetEntityTypeId = FacetEntityTypeId(WebFacetType.getInstance().stringId)
        val facetEntity = FacetEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = facetType.presentableName,
            typeId = facetEntityTypeId,
            entitySource = moduleEntity.entitySource
        ) {
            this.configurationXmlTag = configurationXmlTag
        }

        importContext.mutableStorage.add(moduleEntity, facetEntity)
    }

    /*
    Workspace Model Version - internal API :(
    val sourceRoots = importContext.mutableStorage.contentRootEntities[moduleEntity]
            ?.flatMap { it.sourceRoots }
            ?.filter { it.rootTypeId == JAVA_SOURCE_ROOT_ENTITY_TYPE_ID }
            ?.map { it.url.url }
            ?: emptyList()

        val webRoots = listOf(
            WebRootData(VfsUtil.pathToUrl(webRoot.toSystemIndependentName), "/")
        )

        val configFileItems = listOfNotNull(
            VfsUtil.findFile(moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_WEB_XML), true)
                ?.let { ConfigFileItem(DeploymentDescriptorsConstants.WEB_XML_META_DATA.id, it.url) }

        )

        val webSettingsEntity = WebSettingsEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = WebFacetType.getInstance().defaultFacetName,
            webRoots = webRoots,
            configFileItems = configFileItems,
            sourceRoots = sourceRoots,
            entitySource = moduleEntity.entitySource,
        )

        workspaceModel.update("Add web facet for ${moduleEntity.name}") { storage ->
            storage.modifyModuleEntity(moduleEntity) {
                this.webSettings += webSettingsEntity
            }
        }

     */
}
