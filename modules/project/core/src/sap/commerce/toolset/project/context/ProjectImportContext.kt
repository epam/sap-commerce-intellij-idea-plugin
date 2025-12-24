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
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.io.File

data class ProjectImportContext(
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

    private val _foundModules: MutableCollection<ModuleDescriptor> = mutableListOf(),
    private val _chosenModuleDescriptors: MutableMap<ModuleGroup, Collection<ModuleDescriptor>> = mutableMapOf(),
    private val _detectedVcs: MutableCollection<File> = mutableSetOf(),
    private val _excludedFromScanning: MutableCollection<String> = mutableSetOf()
) {
    val detectedVcs: Collection<File> get() = _detectedVcs.toSet()
    val foundModules: Collection<ModuleDescriptor> get() = _foundModules.toList()

    var excludedFromScanning: Collection<String>
        get() = _excludedFromScanning.toSet()
        set(value) {
            _excludedFromScanning.clear(); _excludedFromScanning.addAll(value)
        }

    val chosenModuleDescriptors: Collection<ModuleDescriptor>
        get() = _chosenModuleDescriptors.values.flatten()

    val configModuleDescriptor: ConfigModuleDescriptor
        get() = chosenModuleDescriptors(ModuleGroup.HYBRIS)
            .filterIsInstance<ConfigModuleDescriptor>()
            .firstOrNull { it.isMainConfig }
            ?: throw HybrisConfigurationException("Unable to find main config module descriptor")

    val platformModuleDescriptor: PlatformModuleDescriptor
        get() = chosenModuleDescriptors(ModuleGroup.HYBRIS)
            .filterIsInstance<PlatformModuleDescriptor>()
            .firstOrNull()
            ?: throw HybrisConfigurationException("Unable to find platform module descriptor")

    fun chosenModuleDescriptors(moduleGroup: ModuleGroup) = _chosenModuleDescriptors[moduleGroup]
        ?: emptyList()

    fun chooseModuleDescriptors(moduleGroup: ModuleGroup, moduleDescriptors: Collection<ModuleDescriptor>) {
        _chosenModuleDescriptors[moduleGroup] = moduleDescriptors.toMutableList()
    }

    fun clear() {
        _foundModules.clear()
        _chosenModuleDescriptors.clear()
    }

    fun addFoundModule(moduleDescriptor: ModuleDescriptor) = _foundModules.add(moduleDescriptor)
    fun addVcs(file: File) = _detectedVcs.add(file)

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null

}