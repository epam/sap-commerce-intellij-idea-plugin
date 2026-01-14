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

package sap.commerce.toolset.project.descriptor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.localextensions.LeExtension
import sap.commerce.toolset.localextensions.LeExtensionsCollector
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.settings.ProjectSettings

@Service
class ModuleDescriptorsSelector {

    fun preselect(context: ProjectImportContext.Mutable, configModuleDescriptor: ConfigModuleDescriptor) {
        val foundExtensions = context.foundModules
            .map { LeExtension(it.name, it.moduleRootPath) }
        val extensionsInLocalExtensions = LeExtensionsCollector.getInstance().collect(
            foundExtensions,
            configModuleDescriptor.moduleRootPath,
            context.platformDistributionPath
        )

        context.foundModules
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

        context.foundModules
            .filterIsInstance<ConfigModuleDescriptor>()
            .forEach { it.setPreselected(true) }
    }

    fun getSelectableHybrisModules(context: ProjectImportContext.Mutable, settings: ProjectSettings): List<ModuleDescriptor> {
        val moduleToImport = mutableSetOf<ModuleDescriptor>()
        val moduleToCheck = mutableSetOf<ModuleDescriptor>()

        context.foundModules
            .filter { it.isPreselected() }
            .forEach {
                moduleToImport.add(it)
                moduleToCheck.add(it)
                it.importStatus = ModuleDescriptorImportStatus.MANDATORY
            }
        resolveDependencies(moduleToImport, moduleToCheck, ModuleDescriptorImportStatus.MANDATORY)

        context.foundModules
            .filter { settings.unusedExtensions.contains(it.name) }
            .forEach {
                moduleToImport.add(it)
                moduleToCheck.add(it)
                it.importStatus = ModuleDescriptorImportStatus.UNUSED
            }

        resolveDependencies(moduleToImport, moduleToCheck, ModuleDescriptorImportStatus.UNUSED)

        return moduleToImport
            .filterNot { settings.modulesOnBlackList.contains(it.getRelativePath(context.rootDirectory)) }
    }

    private fun resolveDependencies(
        moduleToImport: MutableSet<ModuleDescriptor>,
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
        fun getInstance(): ModuleDescriptorsSelector = application.service()
    }
}