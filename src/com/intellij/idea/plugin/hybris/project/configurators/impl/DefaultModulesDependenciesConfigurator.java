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

import com.intellij.idea.plugin.hybris.project.configurators.ModulesDependenciesConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultModulesDependenciesConfigurator implements ModulesDependenciesConfigurator {

    @Override
    public void configure(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final var allModules = Arrays.stream(modifiableModelsProvider.getModules())
            .collect(Collectors.toMap(Module::getName, Function.identity()));
        final var modulesChosenForImport = hybrisProjectDescriptor.getModulesChosenForImport();
        final var extModules = modulesChosenForImport.stream()
            .filter(YPlatformExtModuleDescriptor.class::isInstance)
            .collect(Collectors.toSet());

        modulesChosenForImport.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .forEach(it -> {
                final var nextModule = allModules.get(it.ideaModuleName());

                if (nextModule != null) {
                    configureModuleDependencies(
                        it,
                        nextModule,
                        allModules,
                        extModules,
                        modifiableModelsProvider
                    );
                }
            });

        final var platformDescriptor = hybrisProjectDescriptor.getPlatformHybrisModuleDescriptor();
        final var module = allModules.get(platformDescriptor.ideaModuleName());
        if (module != null) {
            final var rootModel = modifiableModelsProvider.getModifiableRootModel(module);

            modulesChosenForImport.stream()
                .filter(YPlatformExtModuleDescriptor.class::isInstance)
                .map(YPlatformExtModuleDescriptor.class::cast)
                .forEach(dependency -> processModuleDependency(allModules, dependency.ideaModuleName(), rootModel));
        }

        final var configDescriptor = hybrisProjectDescriptor.getConfigHybrisModuleDescriptor();
        if (configDescriptor != null) {
            final var rootModel = modifiableModelsProvider.getModifiableRootModel(module);
            processModuleDependency(allModules, configDescriptor.ideaModuleName(), rootModel);
        }
    }

    private void configureModuleDependencies(
        @NotNull final YModuleDescriptor moduleDescriptor,
        @NotNull final Module module,
        @NotNull final Map<String, Module> allModules,
        @NotNull final Set<ModuleDescriptor> extModules,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final var rootModel = modifiableModelsProvider.getModifiableRootModel(module);

        for (YModuleDescriptor dependency : moduleDescriptor.getDependenciesTree()) {
            if (moduleDescriptor instanceof YOotbRegularModuleDescriptor && extModules.contains(dependency)) {
                continue;
            }

            processModuleDependency(allModules, dependency.ideaModuleName(), rootModel);
        }
    }

    private void processModuleDependency(
        final @NotNull Map<String, Module> allModules,
        final String dependencyName,
        final ModifiableRootModel rootModel) {
        final Module targetDependencyModule = allModules.get(dependencyName);
        final ModuleOrderEntry moduleOrderEntry = targetDependencyModule != null
            ? rootModel.addModuleOrderEntry(targetDependencyModule)
            : rootModel.addInvalidModuleEntry(dependencyName);

        moduleOrderEntry.setExported(true);
        moduleOrderEntry.setScope(DependencyScope.COMPILE);
    }

}
