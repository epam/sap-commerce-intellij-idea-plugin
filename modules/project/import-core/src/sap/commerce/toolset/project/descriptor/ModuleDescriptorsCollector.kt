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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Messages
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisI18nBundle
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YHmcSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.provider.ModuleDescriptorFactory
import sap.commerce.toolset.project.tasks.TaskProgressProcessor
import sap.commerce.toolset.project.utils.FileUtils
import sap.commerce.toolset.settings.ApplicationSettings
import java.io.File
import java.io.IOException

@Service
class ModuleDescriptorsCollector {

    @Throws(InterruptedException::class, IOException::class)
    fun collect(
        importContext: ProjectImportContext.Mutable,
        progressListenerProcessor: TaskProgressProcessor<File>,
        errorsProcessor: TaskProgressProcessor<MutableList<File>>
    ): Collection<ModuleDescriptor> {
        val externalExtensionsDirectory = importContext.externalExtensionsDirectory
        val hybrisDistributionDirectory = importContext.platformDirectory
        val rootDirectory = importContext.rootDirectory
        val excludedFromScanning = getExcludedFromScanningDirectories(importContext)
        val modulesScanner = ModuleDescriptorsScanner.getInstance()
        val moduleRootsContext = ModuleRootsContext()

        thisLogger().info("Scanning for modules")
        modulesScanner.findModuleRoots(importContext, moduleRootsContext, excludedFromScanning, rootDirectory, progressListenerProcessor)

        if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(externalExtensionsDirectory, rootDirectory)) {
            thisLogger().info("Scanning for external modules")
            modulesScanner.findModuleRoots(importContext, moduleRootsContext, excludedFromScanning, externalExtensionsDirectory, progressListenerProcessor)
        }

        if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(hybrisDistributionDirectory, rootDirectory)) {
            thisLogger().info("Scanning for hybris modules out of the project")
            modulesScanner.findModuleRoots(importContext, moduleRootsContext, excludedFromScanning, hybrisDistributionDirectory, progressListenerProcessor)
        }

        val moduleRootDirectories = modulesScanner.processDirectoriesByTypePriority(
            importContext,
            rootDirectory,
            moduleRootsContext
        )

        val moduleDescriptors = mutableListOf<ModuleDescriptor>()
        val pathsFailedToImport = mutableListOf<File>()

        if (!ApplicationSettings.Companion.getInstance().groupModules) {
            val rootModule = addRootModule(rootDirectory)
            if (rootModule != null) moduleDescriptors.add(rootModule)
            else pathsFailedToImport.add(rootDirectory)
        }

        moduleRootDirectories.forEach { moduleRootDirectory ->
            try {
                val moduleDescriptor = ModuleDescriptorFactory.Companion.getInstance().createDescriptor(moduleRootDirectory, importContext)
                moduleDescriptors.add(moduleDescriptor)

                moduleDescriptor.asSafely<YModuleDescriptor>()
                    ?.let { moduleDescriptors.addAll(it.getSubModules()) }
            } catch (e: HybrisConfigurationException) {
                thisLogger().error("Can not import a module using path: $moduleRootDirectory", e)
                pathsFailedToImport.add(rootDirectory)
            }
        }

        if (moduleDescriptors.none { it is PlatformModuleDescriptor }) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    HybrisI18nBundle.message("hybris.project.import.scan.platform.not.found"),
                    HybrisI18nBundle.message("hybris.project.error")
                )
            }

            throw InterruptedException("Unable to find Platform module.")
        }

        if (errorsProcessor.shouldContinue(pathsFailedToImport)) {
            throw InterruptedException("Modules scanning has been interrupted.")
        }

        moduleDescriptors.sort()

        buildDependencies(moduleDescriptors)
        val addons = processAddons(moduleDescriptors)
        removeNotInstalledAddons(moduleDescriptors, addons)
        removeHmcSubModules(moduleDescriptors)

        return moduleDescriptors
    }

    private fun addRootModule(moduleRootDirectory: File): ExternalModuleDescriptor? = try {
        ModuleDescriptorFactory.Companion.getInstance().createRootDescriptor(moduleRootDirectory, moduleRootDirectory.getName())
    } catch (e: HybrisConfigurationException) {
        thisLogger().error("Can not import a module using path: $moduleRootDirectory", e)
        null
    }

    private fun buildDependencies(moduleDescriptors: MutableCollection<ModuleDescriptor>) {
        val moduleDescriptorsMap = moduleDescriptors
            .distinctBy { it.name }
            .associateBy { it.name }
        for (moduleDescriptor in moduleDescriptors) {
            val dependencies = buildDependencies(moduleDescriptor, moduleDescriptorsMap)
            moduleDescriptor.addDirectDependencies(dependencies)
        }
    }

    private fun buildDependencies(
        moduleDescriptor: ModuleDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>
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
                    thisLogger().trace("Module '${moduleDescriptor.name}' contains unsatisfied dependency '$requiresExtensionName'.")
                }
        }
        ?: emptyList()

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

    private fun getExcludedFromScanningDirectories(importContext: ProjectImportContext.Mutable) = importContext.excludedFromScanning
        .map { File(importContext.rootDirectory, it) }
        .filter { it.exists() }
        .filter { it.isDirectory() }
        .toSet()

    companion object {
        fun getInstance(): ModuleDescriptorsCollector = application.service()
    }
}