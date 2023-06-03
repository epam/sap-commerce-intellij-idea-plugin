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
import com.intellij.idea.plugin.hybris.project.descriptors.*;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultModulesDependenciesConfigurator implements ModulesDependenciesConfigurator {

    @Override
    public void configure(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final var modules = Arrays.asList(modifiableModelsProvider.getModules());
        final var modulesChosenForImport = hybrisProjectDescriptor.getModulesChosenForImport();
        final var extModules = modulesChosenForImport.stream()
            .filter(YPlatformExtModuleDescriptor.class::isInstance)
            .collect(Collectors.toSet());

        modulesChosenForImport.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .forEach(it -> {
                findModuleByNameIgnoreCase(modules, it.getName())
                    .ifPresent(nextModule -> configureModuleDependencies(
                        it,
                        nextModule,
                        modules,
                        extModules,
                        modifiableModelsProvider
                    ));
            });
    }

    private void configureModuleDependencies(
        @NotNull final YModuleDescriptor moduleDescriptor,
        @NotNull final Module module,
        @NotNull final Collection<Module> allModules,
        @NotNull final Set<ModuleDescriptor> extModules,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final ModifiableRootModel rootModel = modifiableModelsProvider.getModifiableRootModel(module);

        for (YModuleDescriptor dependency : moduleDescriptor.getDependenciesTree()) {
            if (moduleDescriptor instanceof YOotbRegularModuleDescriptor && extModules.contains(dependency)) {
                continue;
            }

            Optional<Module> targetDependencyModule = findModuleByNameIgnoreCase(allModules, dependency.getName());
            final ModuleOrderEntry moduleOrderEntry = targetDependencyModule.isPresent()
                ? rootModel.addModuleOrderEntry(targetDependencyModule.get())
                : rootModel.addInvalidModuleEntry(dependency.getName());

            moduleOrderEntry.setExported(true);
            moduleOrderEntry.setScope(DependencyScope.COMPILE);
        }
    }

    private static Optional<Module> findModuleByNameIgnoreCase(
        final @NotNull Collection<Module> all,
        final @NotNull String name
    ) {
        return all.stream()
                  .filter(module -> name.equalsIgnoreCase(module.getName()))
                  .findAny();
    }

}
