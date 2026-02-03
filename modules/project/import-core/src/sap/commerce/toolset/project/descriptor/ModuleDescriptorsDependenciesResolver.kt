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
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.descriptor.impl.*


// TODO: improve dependencies resolution for conflicting extensions, load only first one, group by name and pick the first one, for submodules always get parent
@Service
class ModuleDescriptorsDependenciesResolver {

    private val logger = thisLogger()

    fun resolve(foundModuleDescriptors: Collection<ModuleDescriptor>): Collection<ModuleDescriptor> {
        val moduleDescriptors = foundModuleDescriptors.toMutableList()

        buildDependencies(moduleDescriptors)
        processWebSubModules(moduleDescriptors)
        val addons = processAddons(moduleDescriptors)
        removeNotInstalledAddons(moduleDescriptors, addons)
        removeHmcSubModules(moduleDescriptors)

        return moduleDescriptors
    }

    private fun buildDependencies(moduleDescriptors: MutableCollection<ModuleDescriptor>) {
        val moduleDescriptorsMap = moduleDescriptors
            .groupBy { it.name }
        for (moduleDescriptor in moduleDescriptors) {
            val dependencies = buildDependencies(moduleDescriptor, moduleDescriptorsMap)
            moduleDescriptor.addDirectDependencies(dependencies)
        }
    }

    private fun buildDependencies(
        moduleDescriptor: ModuleDescriptor,
        moduleDescriptors: Map<String, Collection<ModuleDescriptor>>
    ) = moduleDescriptor
        .apply { computeRequiredExtensionNames(moduleDescriptors) }
        .getRequiredExtensionNames()
        .sorted()
        .toSet()
        .takeIf { it.isNotEmpty() }
        ?.mapNotNull { requiresExtensionName ->
            moduleDescriptors[requiresExtensionName]
                ?: null.also {
                    // TODO: possible case due optional sub-modules, xxx.web | xxx.backoffice | etc.
                    logger.trace("Module '${moduleDescriptor.name}' contains unsatisfied dependency '$requiresExtensionName'.")
                }
        }
        ?.mapNotNull { it.firstOrNull() }
        ?: emptyList()

    private fun processWebSubModules(moduleDescriptors: Collection<ModuleDescriptor>) {
        moduleDescriptors
            .filterIsInstance<YWebSubModuleDescriptor>()
            .forEach { webSubModuleDescriptor ->
                webSubModuleDescriptor.getDirectDependencies()
                    .asSequence()
                    .filterIsInstance<YModuleDescriptor>()
                    .flatMap { it.getAllDependencies() }
                    .filterIsInstance<YCustomRegularModuleDescriptor>()
                    .flatMap { it.getSubModules() }
                    .filterIsInstance<YCommonWebSubModuleDescriptor>()
                    .toList()
                    .forEach { it.addDependantWebExtension(webSubModuleDescriptor) }
            }
    }

    private fun processAddons(moduleDescriptors: MutableList<ModuleDescriptor>): Collection<YAcceleratorAddonSubModuleDescriptor> {
        val addons = moduleDescriptors
            .filterIsInstance<YAcceleratorAddonSubModuleDescriptor>()
            .takeIf { it.isNotEmpty() }
            ?: return emptyList()

        moduleDescriptors
            .filterIsInstance<YModuleDescriptor>()
            .forEach { module ->
                addons
                    .filter { module != it && module.getDirectDependencies().contains(it.owner) }
                    .forEach { addon -> addon.addTargetModule(module) }
            }

        // update direct dependencies for addons
        addons
            .filter { addon -> addon.getTargetModules().isNotEmpty() }
            .forEach { addon ->
                val targetModules = addon.getTargetModules()
                    .flatMap { targetModule -> targetModule.getSubModules() }
                    .filterIsInstance<YWebSubModuleDescriptor>()
                    .toSet()

                addon.addRequiredExtensionNames(targetModules)
                addon.addDirectDependencies(targetModules)
            }

        return addons
    }

    private fun removeNotInstalledAddons(
        moduleDescriptors: MutableList<ModuleDescriptor>,
        addons: Collection<YAcceleratorAddonSubModuleDescriptor>
    ) {
        val notInstalledAddons = addons
            .filter { it.getTargetModules().isEmpty() }

        notInstalledAddons.forEach({ it.owner.removeSubModule(it) })
        moduleDescriptors.removeAll(notInstalledAddons)
    }

    private fun removeHmcSubModules(moduleDescriptors: MutableList<ModuleDescriptor>) {
        val hmcModulePresent = moduleDescriptors
            .any { it.name == EiConstants.Extension.HMC }
        if (hmcModulePresent) return

        val hmcSubModuleDescriptors = moduleDescriptors
            .filterIsInstance<YModuleDescriptor>()
            .flatMap { moduleDescriptor ->
                moduleDescriptor.getSubModules()
                    .filterIsInstance<YHmcSubModuleDescriptor>()
                    .onEach { moduleDescriptor.removeSubModule(it) }
            }

        moduleDescriptors.removeAll(hmcSubModuleDescriptors)
    }

    companion object {
        fun getInstance(): ModuleDescriptorsDependenciesResolver = application.service()
    }
}