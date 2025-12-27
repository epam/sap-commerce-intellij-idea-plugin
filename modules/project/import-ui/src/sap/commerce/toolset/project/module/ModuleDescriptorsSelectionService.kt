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

package sap.commerce.toolset.project.module

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.project.collector.ExplicitRequiredExtensionsCollector
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.*
import sap.commerce.toolset.project.settings.ProjectSettings

@Service
class ModuleDescriptorsSelectionService {

    fun preselect(importContext: ProjectImportContext.Mutable, configModuleDescriptor: ConfigModuleDescriptor) {
        val extensionsInLocalExtensions = ExplicitRequiredExtensionsCollector.getInstance().collect(importContext, configModuleDescriptor.moduleRootDirectory)

        importContext.foundModules
            .filter { extensionsInLocalExtensions.contains(it.name) }
            .filterIsInstance<YRegularModuleDescriptor>()
            .forEach { moduleDescriptor ->
                moduleDescriptor.isInLocalExtensions = true
                moduleDescriptor.importStatus = ModuleDescriptorImportStatus.MANDATORY
                moduleDescriptor.getSubModules()
                    .forEach { subModule -> subModule.importStatus = ModuleDescriptorImportStatus.MANDATORY }
                moduleDescriptor.getAllDependencies()
                    .filterIsInstance<YRegularModuleDescriptor>()
                    .forEach {
                        it.isNeededDependency = true
                        it.importStatus = ModuleDescriptorImportStatus.MANDATORY
                        it.getSubModules().forEach { subModule -> subModule.importStatus = ModuleDescriptorImportStatus.MANDATORY }
                    }
            }

        importContext.foundModules
            .filterIsInstance<ConfigModuleDescriptor>()
            .forEach { it.setPreselected(true) }
    }

    /*

    public List<ModuleDescriptor> getBestMatchingExtensionsToImport(final @Nullable ProjectSettings settings) {
        final List<ModuleDescriptor> allModules = this.getHybrisProjectDescriptor().getFoundModules();
        final List<ModuleDescriptor> moduleToImport = new ArrayList<>();
        final Set<ModuleDescriptor> moduleToCheck = new HashSet<>();
        for (final var moduleDescriptor : allModules) {
            if (moduleDescriptor.isPreselected()) {
                moduleToImport.add(moduleDescriptor);
                moduleDescriptor.setImportStatus(MANDATORY);
                moduleToCheck.add(moduleDescriptor);
            }
        }
        resolveDependency(moduleToImport, moduleToCheck, MANDATORY);

        final Set<String> unusedExtensionNameSet = settings != null
            ? settings.getUnusedExtensions()
            : Collections.emptySet();

        allModules.stream()
            .filter(e -> unusedExtensionNameSet.contains(e.getName()))
            .forEach(e -> {
                moduleToImport.add(e);
                e.setImportStatus(UNUSED);
                moduleToCheck.add(e);
            });
        resolveDependency(moduleToImport, moduleToCheck, UNUSED);

        final Set<String> modulesOnBlackList = settings != null
            ? settings.getModulesOnBlackList()
            : Collections.emptySet();

        return moduleToImport.stream()
            .filter(e -> !modulesOnBlackList.contains(e.getRelativePath()))
            .sorted(Comparator.nullsLast(Comparator.comparing(ModuleDescriptor::getName)))
            .collect(Collectors.toList());
    }

     */

    fun getSelectableHybrisModules(importContext: ProjectImportContext.Mutable, settings: ProjectSettings): List<ModuleDescriptor> {
        val moduleToImport = mutableListOf<ModuleDescriptor>()
        val moduleToCheck = mutableSetOf<ModuleDescriptor>()

        importContext.foundModules
            .filter { it.isPreselected() }
            .forEach {
                moduleToImport.add(it)
                moduleToCheck.add(it)
                it.importStatus = ModuleDescriptorImportStatus.MANDATORY
            }
        resolveDependencies(moduleToImport, moduleToCheck, ModuleDescriptorImportStatus.MANDATORY)

        importContext.foundModules
            .filter { settings.unusedExtensions.contains(it.name) }
            .forEach {
                moduleToImport.add(it)
                moduleToCheck.add(it)
                it.importStatus = ModuleDescriptorImportStatus.UNUSED
            }

        resolveDependencies(moduleToImport, moduleToCheck, ModuleDescriptorImportStatus.UNUSED)

        return moduleToImport
            .filterNot { settings.modulesOnBlackList.contains(it.getRelativePath(importContext.rootDirectory)) }
    }

    /*

    private void resolveDependency(
        final List<ModuleDescriptor> moduleToImport,
        final Set<ModuleDescriptor> moduleToCheck,
        final ModuleDescriptorImportStatus selectionMode
    ) {
        while (!moduleToCheck.isEmpty()) {
            final ModuleDescriptor currentModule = moduleToCheck.iterator().next();
            if (currentModule instanceof final YModuleDescriptor yModuleDescriptor) {
                for (final ModuleDescriptor moduleDescriptor : yModuleDescriptor.getAllDependencies()) {
                    if (!moduleToImport.contains(moduleDescriptor)) {
                        moduleToImport.add(moduleDescriptor);
                        moduleDescriptor.setImportStatus(selectionMode);
                        moduleToCheck.add(moduleDescriptor);
                    }
                }
            }
            moduleToCheck.remove(currentModule);
        }
    }

     */

    private fun resolveDependencies(
        moduleToImport: MutableList<ModuleDescriptor>,
        moduleToCheck: MutableSet<ModuleDescriptor>,
        selectionMode: ModuleDescriptorImportStatus
    ) {
        while (!moduleToCheck.isEmpty()) {
            val currentModule = moduleToCheck.iterator().next()
            if (currentModule is YModuleDescriptor) {
                for (moduleDescriptor in currentModule.getAllDependencies()) {
                    if (!moduleToImport.contains(moduleDescriptor)) {
                        moduleToImport.add(moduleDescriptor)
                        moduleDescriptor.importStatus = selectionMode
                        moduleToCheck.add(moduleDescriptor)
                    }
                }
            }
            moduleToCheck.remove(currentModule)
        }
    }

    companion object {
        fun getInstance(): ModuleDescriptorsSelectionService = application.service()
    }
}