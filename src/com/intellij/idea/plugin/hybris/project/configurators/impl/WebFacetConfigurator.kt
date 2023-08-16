/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.facet.FacetManager
import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ModifiableFacetModel
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCommonWebSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YWebSubModuleDescriptor
import com.intellij.javaee.DeploymentDescriptorsConstants
import com.intellij.javaee.web.facet.WebFacet
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import java.io.File

class WebFacetConfigurator : FacetConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        val webRoot = when (moduleDescriptor) {
            is YWebSubModuleDescriptor -> moduleDescriptor.webRoot.absolutePath
            is YCommonWebSubModuleDescriptor -> moduleDescriptor.webRoot.absolutePath
            else -> return
        }

        WriteAction.runAndWait<RuntimeException> {
            val webFacet = modifiableFacetModel.getFacetByType(WebFacet.ID)
                ?.also {
                    it.removeAllWebRoots()
                    it.descriptorsContainer.configuration.removeConfigFiles(DeploymentDescriptorsConstants.WEB_XML_META_DATA)
                }
                ?: FacetTypeRegistry.getInstance().findFacetType(WebFacet.ID)
                    .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
                    ?.let { FacetManager.getInstance(javaModule).createFacet(it, it.defaultFacetName, null) }
                    ?.also { modifiableFacetModel.addFacet(it) }
                ?: return@runAndWait

            webFacet.setWebSourceRoots(modifiableRootModel.getSourceRootUrls(false))
            webFacet.addWebRootNoFire(VfsUtil.pathToUrl(FileUtil.toSystemIndependentName(webRoot)), "/")

            VfsUtil.findFileByIoFile(File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_WEB_XML_PATH), true)
                ?.let { webFacet.descriptorsContainer.configuration.addConfigFile(DeploymentDescriptorsConstants.WEB_XML_META_DATA, it.url) }
        }
    }

}
