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
import com.intellij.platform.workspace.jps.entities.ContentRootEntityBuilder
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.FacetEntityTypeId
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.util.descriptors.ConfigFileInfo
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import com.intellij.workspaceModel.ide.legacyBridge.impl.java.JAVA_SOURCE_ROOT_ENTITY_TYPE_ID
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag
import sap.commerce.toolset.util.toSystemIndependentName

/**
 * No need to migrate to the new Workspace API or use the {@code webSettings} storage builder at the moment.
 * See <a href="https://platform.jetbrains.com/t/how-to-properly-manage-javasettings-and-websettings-in-workspace-api/3471">How to properly manage javaSettings and webSettings in Workspace API</a>.
 */
class WebFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Web Facets"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(context: ProjectModuleConfigurationContext, ) {
        val importContext = context.importContext
        val moduleDescriptor = context.moduleDescriptor
        val moduleEntity = context.moduleEntity
        val javaModule = moduleEntity.findModule(context.workspaceModel.currentSnapshot) ?: return
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

        val sourceRoots = importContext.mutableStorage.entities(ContentRootEntityBuilder::class)[moduleEntity]
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
}
