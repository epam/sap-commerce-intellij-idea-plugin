/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.javaee.DeploymentDescriptorsConstants;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 8:33 PM 13 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class WebFacetConfigurator implements FacetConfigurator {


    @Override
    public void configure(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor, @NotNull final ModifiableFacetModel modifiableFacetModel,
        @NotNull final HybrisModuleDescriptor moduleDescriptor,
        @NotNull final Module javaModule,
        @NotNull final ModifiableRootModel modifiableRootModel
    ) {
        final File webRoot = moduleDescriptor.getWebRoot();
        if (null == webRoot) {
            return;
        }

        WebFacet webFacet = modifiableFacetModel.getFacetByType(WebFacet.ID);

        if (webFacet == null) {
            final FacetType<WebFacet, FacetConfiguration> webFacetType = FacetTypeRegistry.getInstance().findFacetType(
                WebFacet.ID
            );

            if (!webFacetType.isSuitableModuleType(ModuleType.get(javaModule))) {
                return;
            }

            webFacet = FacetManager.getInstance(javaModule).createFacet(
                webFacetType, webFacetType.getDefaultFacetName(), null
            );

            modifiableFacetModel.addFacet(webFacet);

        } else {
            webFacet.removeAllWebRoots();
            webFacet.getDescriptorsContainer().getConfiguration().removeConfigFiles(
                DeploymentDescriptorsConstants.WEB_XML_META_DATA
            );
        }
        writeFacetData(moduleDescriptor, modifiableRootModel, webRoot, webFacet);
    }

    private void writeFacetData(HybrisModuleDescriptor moduleDescriptor, ModifiableRootModel modifiableRootModel, File webRoot, WebFacet webFacet) {
        WriteAction.runAndWait(() -> {
            webFacet.setWebSourceRoots(modifiableRootModel.getSourceRootUrls(false));
            webFacet.addWebRootNoFire(VfsUtil.pathToUrl(FileUtil.toSystemIndependentName(webRoot.getAbsolutePath())), "/");
            setupFacetDeploymentDescriptor(webFacet, moduleDescriptor);
        });
    }

    private void setupFacetDeploymentDescriptor(
        @NotNull final WebFacet webFacet,
        @NotNull final HybrisModuleDescriptor moduleDescriptor
    ) {
        final VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(
            new File(moduleDescriptor.getRootDirectory(), HybrisConstants.WEB_XML_DIRECTORY_RELATIVE_PATH), true
        );

        if (null != fileByIoFile) {
            webFacet.getDescriptorsContainer().getConfiguration().addConfigFile(
                DeploymentDescriptorsConstants.WEB_XML_META_DATA, fileByIoFile.getUrl()
            );
        }
    }

}
