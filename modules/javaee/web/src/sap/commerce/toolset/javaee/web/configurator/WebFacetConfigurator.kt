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

import com.intellij.facet.FacetManager
import com.intellij.facet.FacetTypeRegistry
import com.intellij.javaee.DeploymentDescriptorsConstants
import com.intellij.javaee.web.facet.WebFacet
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.toSystemIndependentName

/**
 * No need to migrate to the new Workspace API or use the {@code webSettings} storage builder at the moment.
 * See <a href="https://platform.jetbrains.com/t/how-to-properly-manage-javasettings-and-websettings-in-workspace-api/3471">How to properly manage javaSettings and webSettings in Workspace API</a>.
 */
class WebFacetConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Web Facets"

    override fun configure(
        context: ProjectPostImportContext,
        legacyWorkspace: IdeModifiableModelsProvider,
        edtActions: MutableList<() -> Unit>
    ) {
        context.chosenHybrisModuleDescriptors.forEach { moduleDescriptor ->
            val module = context.modules[moduleDescriptor.name] ?: run {
                thisLogger().warn("Could not find module for ${moduleDescriptor.name}")
                return@forEach
            }

            configure(legacyWorkspace, moduleDescriptor, module)
                ?.let(edtActions::add)
        }
    }

    private fun configure(
        legacyWorkspace: IdeModifiableModelsProvider,
        moduleDescriptor: ModuleDescriptor,
        module: Module,
    ): (() -> Unit)? {
        val modifiableFacetModel = legacyWorkspace.getModifiableFacetModel(module)
        val modifiableRootModel = legacyWorkspace.getModifiableRootModel(module)

        val webRoot = when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.webRoot
            is YAcceleratorAddonSubModuleDescriptor -> moduleDescriptor.webRoot
            else -> return null
        }

        val webFacet = FacetTypeRegistry.getInstance().findFacetType(WebFacet.ID)
            .takeIf { it.isSuitableModuleType(ModuleType.get(module)) }
            ?.let { FacetManager.getInstance(module).createFacet(it, it.defaultFacetName, null) }
            ?.also { modifiableFacetModel.addFacet(it) }
            ?: return null

        return {
            webFacet.removeAllWebRoots()
            webFacet.descriptorsContainer.configuration.removeConfigFiles(DeploymentDescriptorsConstants.WEB_XML_META_DATA)

            webFacet.setWebSourceRoots(modifiableRootModel.getSourceRootUrls(false))
            webFacet.addWebRootNoFire(VfsUtil.pathToUrl(webRoot.toSystemIndependentName), "/")

            VfsUtil.findFile(moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_WEB_XML), true)
                ?.let { webFacet.descriptorsContainer.configuration.addConfigFile(DeploymentDescriptorsConstants.WEB_XML_META_DATA, it.url) }
        }
    }
}
