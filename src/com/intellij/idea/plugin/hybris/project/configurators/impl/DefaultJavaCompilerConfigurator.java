package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.HybrisConfiguratorCache;
import com.intellij.idea.plugin.hybris.project.configurators.JavaCompilerConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.ConfigHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.JavaCompilers;

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
        final ConfigHybrisModuleDescriptor configDescriptor = descriptor.getConfigHybrisModuleDescriptor();

        if (configDescriptor != null) {
            propertyFiles.add(new File(configDescriptor.getRootDirectory(), HybrisConstants.LOCAL_PROPERTIES));
        }
        final PlatformHybrisModuleDescriptor platformDescriptor = descriptor.getPlatformHybrisModuleDescriptor();
        propertyFiles.add(new File(platformDescriptor.getRootDirectory(), HybrisConstants.ADVANCED_PROPERTIES));
        propertyFiles.add(new File(platformDescriptor.getRootDirectory(), HybrisConstants.PROJECT_PROPERTIES));

        return cache.findPropertyInFiles(propertyFiles, HybrisConstants.PROPERTY_BUILD_COMPILER);
    }
}
