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

import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.contexts.model.LocalXmlModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.facet.beans.CustomSetting;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SpringFacetConfigurator implements FacetConfigurator {

    @Override
    public void configure(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor, @NotNull final ModifiableFacetModel modifiableFacetModel,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final Module javaModule,
        @NotNull final ModifiableRootModel modifiableRootModel
    ) {
        if (moduleDescriptor instanceof final YModuleDescriptor yModuleDescriptor) {
            configure(modifiableFacetModel, javaModule, yModuleDescriptor);
        }
    }

    private void configure(final @NotNull ModifiableFacetModel modifiableFacetModel, final @NotNull Module javaModule, final YModuleDescriptor yModuleDescriptor) {
        SpringFacet springFacet = SpringFacet.getInstance(javaModule);

        if (springFacet == null) {
            final FacetType<SpringFacet, SpringFacetConfiguration> springFacetType = SpringFacet.getSpringFacetType();

            if (!springFacetType.isSuitableModuleType(ModuleType.get(javaModule))) return;

            springFacet = springFacetType.createFacet(
                javaModule, springFacetType.getDefaultFacetName(), springFacetType.createDefaultConfiguration(), null
            );
        } else {
            springFacet.removeFileSets();
        }

        final String facetId = yModuleDescriptor.getName() + SpringFacet.FACET_TYPE_ID;
        final SpringFileSet springFileSet = springFacet.addFileSet(facetId, facetId);

        for (String springFile : yModuleDescriptor.getSpringFileSet()) {
            final VirtualFile vf = VfsUtil.findFileByIoFile(new File(springFile), true);

            if (null != vf) {
                springFileSet.addFile(vf);
            }
        }
        final CustomSetting.BOOLEAN setting = springFacet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED);

        if (setting != null) {
            setting.setBooleanValue(false);
            setting.apply();
        }

        modifiableFacetModel.addFacet(springFacet);
    }
}
