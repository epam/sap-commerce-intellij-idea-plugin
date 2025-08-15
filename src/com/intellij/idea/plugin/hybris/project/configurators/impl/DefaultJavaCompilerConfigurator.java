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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.HybrisConfiguratorCache;
import com.intellij.idea.plugin.hybris.project.configurators.JavaCompilerConfigurator;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.JavaCompilers;
import sap.commerce.toolset.project.descriptors.HybrisProjectDescriptor;
import sap.commerce.toolset.project.descriptors.impl.ConfigModuleDescriptor;
import sap.commerce.toolset.project.descriptors.impl.PlatformModuleDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultJavaCompilerConfigurator implements JavaCompilerConfigurator {

    @Override
    public void configure(
        @NotNull final HybrisProjectDescriptor descriptor,
        @NotNull final Project project,
        @NotNull final HybrisConfiguratorCache cache
    ) {
        final String buildCompilerPropValue = findBuildCompilerProperty(descriptor, cache);

        if (buildCompilerPropValue == null) {
            return;
        }
        final CompilerConfigurationImpl configuration =
            (CompilerConfigurationImpl) CompilerConfiguration.getInstance(project);

        if (buildCompilerPropValue.equals("org.eclipse.jdt.core.JDTCompilerAdapter")) {
            final Optional<BackendCompiler> eclipseCompiler = CollectionUtils.emptyIfNull(
                configuration.getRegisteredJavaCompilers()
            ).stream().filter(
                it -> JavaCompilers.ECLIPSE_ID.equals(it.getId())
            ).findAny();

            eclipseCompiler.ifPresent(configuration::setDefaultCompiler);

        } else if (buildCompilerPropValue.equals("modern")) {
            final Optional<BackendCompiler> javac = CollectionUtils.emptyIfNull(
                configuration.getRegisteredJavaCompilers()
            ).stream().filter(
                it -> JavaCompilers.JAVAC_ID.equals(it.getId())
            ).findAny();

            javac.ifPresent(configuration::setDefaultCompiler);
        }
    }

    private static String findBuildCompilerProperty(
        @NotNull final HybrisProjectDescriptor descriptor,
        @NotNull final HybrisConfiguratorCache cache
    ) {
        final List<File> propertyFiles = new ArrayList<>();
        final ConfigModuleDescriptor configDescriptor = descriptor.getConfigHybrisModuleDescriptor();

        if (configDescriptor != null) {
            propertyFiles.add(new File(configDescriptor.getModuleRootDirectory(), HybrisConstants.LOCAL_PROPERTIES_FILE));
        }
        final PlatformModuleDescriptor platformDescriptor = descriptor.getPlatformHybrisModuleDescriptor();
        propertyFiles.add(new File(platformDescriptor.getModuleRootDirectory(), HybrisConstants.ADVANCED_PROPERTIES));
        propertyFiles.add(new File(platformDescriptor.getModuleRootDirectory(), HybrisConstants.PROJECT_PROPERTIES_FILE));

        return cache.findPropertyInFiles(propertyFiles, HybrisConstants.PROPERTY_BUILD_COMPILER);
    }
}
