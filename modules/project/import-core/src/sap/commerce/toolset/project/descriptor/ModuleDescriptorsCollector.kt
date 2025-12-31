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
import com.intellij.platform.util.progress.withProgressText
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ModuleRoot
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YHmcSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.provider.ModuleDescriptorFactory
import sap.commerce.toolset.project.module.ModuleRootsScanner
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.util.isDescendantOf
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

@Service
class ModuleDescriptorsCollector {

    @Throws(HybrisConfigurationException::class)
    suspend fun collect(importContext: ProjectImportContext.Mutable): Collection<ModuleDescriptor> {
        val rootDirectory = importContext.rootDirectory.toPath()
        val excludedFromScanning = getExcludedFromScanningDirectories(importContext)
        val moduleRootsScanner = ModuleRootsScanner.getInstance()
        val skipDirectories = excludedFromScanning.map { it.toPath() }
        val foundModuleRoots = mutableListOf<ModuleRoot>()

        withProgressText("Scanning for modules & vcs...") {
            thisLogger().info("Scanning for modules & vcs: $rootDirectory")
            moduleRootsScanner.execute(importContext, rootDirectory, skipDirectories)
                .apply { foundModuleRoots.addAll(this) }
        }

        importContext.externalExtensionsDirectory?.toPath()
            ?.takeUnless { it.isDescendantOf(rootDirectory) }
            ?.let {
                withProgressText("Scanning for modules & vcs in: ${it.name}") {
                    thisLogger().info("Scanning external extensions directory: $it")
                    moduleRootsScanner.execute(importContext, it, skipDirectories)
                }
            }
            ?.apply { foundModuleRoots.addAll(this) }

        importContext.platformDirectory?.toPath()
            ?.takeUnless { it.isDescendantOf(rootDirectory) }
            ?.let {
                withProgressText("Scanning for modules & vcs in: ${it.name}") {
                    thisLogger().info("Scanning for hybris modules out of the project: $it")
                    moduleRootsScanner.execute(importContext, it, skipDirectories)
                }
            }
            ?.apply { foundModuleRoots.addAll(this) }

        val moduleRoots = moduleRootsScanner.processModuleRootsByTypePriority(
            importContext, rootDirectory, foundModuleRoots
        )
        val moduleDescriptors = mutableListOf<ModuleDescriptor>()
        val moduleRootsFailedToImport = mutableListOf<ModuleRoot>()

        addRootModuleDescriptor(importContext, rootDirectory, moduleDescriptors, moduleRootsFailedToImport)

        moduleRoots.forEach { moduleRoot ->
            try {
                val moduleDescriptor = ModuleDescriptorFactory.getInstance().createDescriptor(importContext, moduleRoot)
                moduleDescriptors.add(moduleDescriptor)

                moduleDescriptor.asSafely<YModuleDescriptor>()
                    ?.let { moduleDescriptors.addAll(it.getSubModules()) }
            } catch (e: HybrisConfigurationException) {
                thisLogger().error("Can not import a module using path: $moduleRoot", e)
                moduleRootsFailedToImport.add(moduleRoot)
            }
        }

        if (moduleDescriptors.none { it is PlatformModuleDescriptor }) {
            throw HybrisConfigurationException(i18n("hybris.project.import.scan.platform.not.found"))
        }

        if (moduleRootsFailedToImport.isNotEmpty()) {
            val paths = moduleRootsFailedToImport
                .map { it.path }
            throw HybrisConfigurationException(i18n("hybris.project.import.scan.failed", paths))
        }

        buildDependencies(moduleDescriptors)
        val addons = processAddons(moduleDescriptors)
        removeNotInstalledAddons(moduleDescriptors, addons)
        removeHmcSubModules(moduleDescriptors)

        return moduleDescriptors
    }

    private fun addRootModuleDescriptor(
        importContext: ProjectImportContext.Mutable,
        rootDirectory: Path,
        moduleDescriptors: MutableList<ModuleDescriptor>,
        moduleRootsFailedToImport: MutableList<ModuleRoot>
    ) {
        if (ApplicationSettings.getInstance().groupModules) return

        val moduleRoot = ModuleRoot(
            moduleGroup = ModuleGroup.HYBRIS,
            type = ModuleDescriptorType.ROOT,
            path = rootDirectory,
        )

        try {
            ModuleDescriptorFactory.getInstance().createRootDescriptor(moduleRoot)
                .let { moduleDescriptors.add(it) }
        } catch (e: HybrisConfigurationException) {
            thisLogger().error("Can not import a module using path: $moduleRoot", e)
            moduleRootsFailedToImport.add(moduleRoot)
        }
    }

    private fun getExcludedFromScanningDirectories(importContext: ProjectImportContext.Mutable) = importContext.excludedFromScanning
        .map { File(importContext.rootDirectory, it) }
        .filter { it.exists() }
        .filter { it.isDirectory() }
        .toSet()

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

    companion object {
        fun getInstance(): ModuleDescriptorsCollector = application.service()
    }
}