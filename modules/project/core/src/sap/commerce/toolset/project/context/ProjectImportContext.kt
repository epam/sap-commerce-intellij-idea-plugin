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

package sap.commerce.toolset.project.context

import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.io.File
import java.nio.file.Path

data class ProjectImportContext(
    var project: Project,
    val rootDirectory: File,
    val refresh: Boolean,
    val settings: ProjectImportSettings,

    var modulesFilesDirectory: File? = null,
    var ccv2Token: String? = null,
    var sourceCodeFile: File? = null,
    var projectIconFile: File? = null,

    var platformDirectory: File? = null,
    var externalExtensionsDirectory: File? = null,
    var externalConfigDirectory: File? = null,
    var externalDbDriversDirectory: File? = null,

    var javadocUrl: String? = null,

    var platformVersion: String? = null,

    val foundModules: Collection<ModuleDescriptor>,

    val detectedVcs: Collection<File>,
    val excludedFromScanning: Collection<String>,

    val configModuleDescriptor: ConfigModuleDescriptor,
    val platformModuleDescriptor: PlatformModuleDescriptor,

    val chosenHybrisModuleDescriptors: Collection<ModuleDescriptor>,
    val chosenOtherModuleDescriptors: Collection<ModuleDescriptor>,
) {
    val allChosenModuleDescriptors
        get() = chosenHybrisModuleDescriptors + chosenOtherModuleDescriptors

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null

    data class Mutable(
        val rootDirectory: File,
        val refresh: Boolean,
        val settings: ProjectImportSettings,

        var project: Project? = null,
        var modulesFilesDirectory: File? = null,
        var ccv2Token: String? = null,
        var sourceCodeFile: File? = null,
        var projectIconFile: File? = null,
        var platformDirectory: File? = null,
        var externalExtensionsDirectory: File? = null,
        var externalConfigDirectory: File? = null,
        var externalDbDriversDirectory: File? = null,
        var javadocUrl: String? = null,
        var platformVersion: String? = null,

//        private val _moduleRoots: MutableList<ModuleRoot> = mutableListOf(),
        private val _foundModules: MutableCollection<ModuleDescriptor> = mutableListOf(),
        private val _chosenModuleDescriptors: MutableMap<ModuleGroup, Collection<ModuleDescriptor>> = mutableMapOf(),
        private val _detectedVcs: MutableCollection<Path> = mutableSetOf(),
        private val _excludedFromScanning: MutableCollection<String> = mutableSetOf()
    ) {
//        val hybrisModuleRoots
//            get() = _moduleRoots.filter { it.moduleGroup == ModuleGroup.HYBRIS }
//        val otherModuleRoots
//            get() = _moduleRoots.filter { it.moduleGroup == ModuleGroup.OTHER }
        val foundModules: Collection<ModuleDescriptor>
            get() = _foundModules.toImmutableList()
        var excludedFromScanning: Collection<String>
            get() = _excludedFromScanning.toSet()
            set(value) {
                _excludedFromScanning.clear(); _excludedFromScanning.addAll(value)
            }

        fun chooseModuleDescriptors(moduleGroup: ModuleGroup, moduleDescriptors: Collection<ModuleDescriptor>) {
            _chosenModuleDescriptors[moduleGroup] = moduleDescriptors.toMutableList()
        }

//        fun addModuleRoot(root: ModuleRoot) = _moduleRoots.add(root)

        fun addModule(moduleDescriptor: ModuleDescriptor) = _foundModules.add(moduleDescriptor)
        fun addVcs(file: Path) = _detectedVcs.add(file)

        fun clear() {
//            _moduleRoots.clear()
            _foundModules.clear()
            _detectedVcs.clear()
            _chosenModuleDescriptors.clear()
        }

        fun immutable(project: Project) = ProjectImportContext(
            project = project,
            rootDirectory = rootDirectory,
            refresh = refresh,
            settings = settings,

            modulesFilesDirectory = modulesFilesDirectory,
            ccv2Token = ccv2Token,
            sourceCodeFile = sourceCodeFile,
            projectIconFile = projectIconFile,
            platformDirectory = platformDirectory,
            externalExtensionsDirectory = externalExtensionsDirectory,
            externalConfigDirectory = externalConfigDirectory,
            externalDbDriversDirectory = externalDbDriversDirectory,
            javadocUrl = javadocUrl,
            platformVersion = platformVersion,

            foundModules = _foundModules.toImmutableList(),
            chosenHybrisModuleDescriptors = _chosenModuleDescriptors[ModuleGroup.HYBRIS]
                ?: emptyList(),
            chosenOtherModuleDescriptors = _chosenModuleDescriptors[ModuleGroup.OTHER]
                ?: emptyList(),

            detectedVcs = _detectedVcs
                .map { it.normalize().toFile() }
                .toImmutableSet(),
            excludedFromScanning = _excludedFromScanning.toImmutableList(),

            configModuleDescriptor = _foundModules
                .filterIsInstance<ConfigModuleDescriptor>()
                .firstOrNull { it.isMainConfig }
                ?: throw HybrisConfigurationException("Unable to find main config module descriptor"),
            platformModuleDescriptor = _foundModules
                .filterIsInstance<PlatformModuleDescriptor>()
                .firstOrNull()
                ?: throw HybrisConfigurationException("Unable to find platform module descriptor"),
        )
    }
}