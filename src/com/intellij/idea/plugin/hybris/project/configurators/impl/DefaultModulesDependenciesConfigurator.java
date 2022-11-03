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
import com.intellij.idea.plugin.hybris.project.descriptors.ExtHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.OotbHybrisModuleDescriptor;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created 12:22 AM 25 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultModulesDependenciesConfigurator implements ModulesDependenciesConfigurator {

    @Override
    public void configure(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final List<Module> modules = Arrays.asList(modifiableModelsProvider.getModules());
        final Set<HybrisModuleDescriptor> extModules = hybrisProjectDescriptor
            .getModulesChosenForImport()
            .stream()
            .filter(moduleDescriptor -> moduleDescriptor instanceof ExtHybrisModuleDescriptor)
            .collect(Collectors.toSet());

        for (final HybrisModuleDescriptor nextDescriptor : hybrisProjectDescriptor.getModulesChosenForImport()) {
            findModuleByNameIgnoreCase(modules, nextDescriptor.getName())
                .ifPresent(nextModule -> configureModuleDependencies(
                    nextDescriptor,
                    nextModule,
                    modules,
                    extModules,
                    modifiableModelsProvider
                ));
        }
    }

    private void configureModuleDependencies(
        @NotNull final HybrisModuleDescriptor moduleDescriptor,
        @NotNull final Module module,
        @NotNull final Collection<Module> allModules,
        @NotNull final Set<HybrisModuleDescriptor> extModules,
        final @NotNull IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final ModifiableRootModel rootModel = modifiableModelsProvider.getModifiableRootModel(module);

        for (HybrisModuleDescriptor dependency : moduleDescriptor.getDependenciesTree()) {
            if (moduleDescriptor instanceof OotbHybrisModuleDescriptor) {
                if (extModules.contains(dependency)) {
                    continue;
                }
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
        return all.stream().filter(m -> name.equalsIgnoreCase(m.getName())).findAny();
    }

}
