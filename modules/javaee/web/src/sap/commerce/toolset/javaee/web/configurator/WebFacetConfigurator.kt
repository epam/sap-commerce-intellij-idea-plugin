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
import com.intellij.facet.ModifiableFacetModel
import com.intellij.javaee.DeploymentDescriptorsConstants
import com.intellij.javaee.web.facet.WebFacet
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.configurator.ModuleFacetConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import java.io.File

class WebFacetConfigurator : ModuleFacetConfigurator {

    override val name: String
        get() = "Web Facets"

    override fun configureModuleFacet(
        module: Module,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        modifiableRootModel: ModifiableRootModel
    ) {
        val webRoot = when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.webRoot.absolutePath
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.webRoot.absolutePath
            is YAcceleratorAddonSubModuleDescriptor -> moduleDescriptor.webRoot.absolutePath
            else -> return
        }

        WriteAction.runAndWait<RuntimeException> {
            val webFacet = modifiableFacetModel.getFacetByType(WebFacet.ID)
                ?.also {
                    it.removeAllWebRoots()
                    it.descriptorsContainer.configuration.removeConfigFiles(DeploymentDescriptorsConstants.WEB_XML_META_DATA)
                }
                ?: FacetTypeRegistry.getInstance().findFacetType(WebFacet.ID)
                    .takeIf { it.isSuitableModuleType(ModuleType.get(module)) }
                    ?.let { FacetManager.getInstance(module).createFacet(it, it.defaultFacetName, null) }
                    ?.also { modifiableFacetModel.addFacet(it) }
                ?: return@runAndWait

            webFacet.setWebSourceRoots(modifiableRootModel.getSourceRootUrls(false))
            webFacet.addWebRootNoFire(VfsUtil.pathToUrl(FileUtil.toSystemIndependentName(webRoot)), "/")

            VfsUtil.findFileByIoFile(File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_WEB_XML_PATH), true)
                ?.let { webFacet.descriptorsContainer.configuration.addConfigFile(DeploymentDescriptorsConstants.WEB_XML_META_DATA, it.url) }
        }
    }
}
