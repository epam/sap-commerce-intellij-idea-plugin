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
import sap.commerce.toolset.project.descriptor.impl.*
import sap.commerce.toolset.project.descriptor.provider.ModuleDescriptorFactory
import sap.commerce.toolset.project.exceptions.PlatformModuleNotFoundException
import sap.commerce.toolset.project.module.ModuleRootsScanner
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.isDescendantOf
import java.nio.file.Path
import kotlin.io.path.name

@Service
class ModuleDescriptorsCollector {

    private val logger = thisLogger()

    @Throws(HybrisConfigurationException::class)
    suspend fun collect(context: ProjectImportContext.Mutable): Collection<ModuleDescriptor> {
        val rootDirectory = context.rootDirectory
        val skipDirectories = getExcludedFromScanningDirectories(context)
        val moduleRootsScanner = ModuleRootsScanner.getInstance()
        val foundModuleRoots = mutableListOf<ModuleRoot>()

        withProgressText("Scanning for modules & vcs...") {
            logger.debug("Scanning for modules & vcs: $rootDirectory")
            moduleRootsScanner.execute(context, rootDirectory, skipDirectories)
                .apply { foundModuleRoots.addAll(this) }
        }

        context.externalExtensionsDirectory
            ?.takeUnless { it.isDescendantOf(rootDirectory) }
            ?.let {
                withProgressText("Scanning for modules & vcs in: ${it.name}") {
                    logger.debug("Scanning external extensions directory: $it")
                    moduleRootsScanner.execute(context, it, skipDirectories)
                }
            }
            ?.apply { foundModuleRoots.addAll(this) }

        context.platformDistributionPath
            ?.takeUnless { it.isDescendantOf(rootDirectory) }
            ?.let {
                withProgressText("Scanning for modules & vcs in: ${it.name}") {
                    logger.debug("Scanning for hybris modules out of the project: $it")
                    moduleRootsScanner.execute(context, it, skipDirectories)
                }
            }
            ?.apply { foundModuleRoots.addAll(this) }

        val moduleRoots = moduleRootsScanner.processModuleRootsByTypePriority(
            context, rootDirectory, foundModuleRoots
        )
        val moduleDescriptors = mutableListOf<ModuleDescriptor>()
        val moduleRootsFailedToImport = mutableListOf<ModuleRoot>()

        addRootModuleDescriptor(context, rootDirectory, moduleDescriptors, moduleRootsFailedToImport)

        moduleRoots.forEach { moduleRoot ->
            try {
                val moduleDescriptor = ModuleDescriptorFactory.getInstance().createDescriptor(context, moduleRoot)
                moduleDescriptors.add(moduleDescriptor)

                moduleDescriptor.asSafely<YModuleDescriptor>()
                    ?.let { moduleDescriptors.addAll(it.getSubModules()) }
            } catch (e: HybrisConfigurationException) {
                logger.error("Can not import a module using path: $moduleRoot", e)
                moduleRootsFailedToImport.add(moduleRoot)
            }
        }

        if (moduleDescriptors.none { it is PlatformModuleDescriptor }) {
            throw PlatformModuleNotFoundException(moduleDescriptors)
        }

        if (moduleRootsFailedToImport.isNotEmpty()) {
            val paths = moduleRootsFailedToImport
                .map { it.path }
            throw HybrisConfigurationException(i18n("hybris.project.import.scan.failed", paths))
        }

        buildDependencies(moduleDescriptors)
        processWebSubModules(moduleDescriptors)
        val addons = processAddons(moduleDescriptors)
        removeNotInstalledAddons(moduleDescriptors, addons)
        removeHmcSubModules(moduleDescriptors)

        return moduleDescriptors
    }

    private fun addRootModuleDescriptor(
        context: ProjectImportContext.Mutable,
        rootDirectory: Path,
        moduleDescriptors: MutableList<ModuleDescriptor>,
        moduleRootsFailedToImport: MutableList<ModuleRoot>
    ) {
        if (context.settings.groupModules) return

        val moduleRoot = ModuleRoot(
            moduleGroup = ModuleGroup.HYBRIS,
            type = ModuleDescriptorType.ROOT,
            path = rootDirectory,
        )

        try {
            ModuleDescriptorFactory.getInstance().createRootDescriptor(moduleRoot)
                .let { moduleDescriptors.add(it) }
        } catch (e: HybrisConfigurationException) {
            logger.error("Can not import a module using path: $moduleRoot", e)
            moduleRootsFailedToImport.add(moduleRoot)
        }
    }

    private fun getExcludedFromScanningDirectories(context: ProjectImportContext.Mutable) = context.excludedFromScanning
        .map { context.rootDirectory.resolve(it) }
        .filter { it.directoryExists }
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
                    logger.trace("Module '${moduleDescriptor.name}' contains unsatisfied dependency '$requiresExtensionName'.")
                }
        }
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
        fun getInstance(): ModuleDescriptorsCollector = application.service()
    }
}