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

import com.intellij.java.workspace.entities.JavaModuleSettingsEntityBuilder
import com.intellij.openapi.project.Project
import com.intellij.platform.workspace.jps.entities.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.nio.file.Path

data class ProjectImportContext(
    val project: Project,
    val rootDirectory: Path,
    val platformDirectory: Path,
    val refresh: Boolean,
    val settings: ProjectImportSettings,

    val modulesFilesDirectory: Path? = null,
    val ccv2Token: String? = null,
    val sourceCodePath: Path? = null,
    val sourceCodeFile: Path? = null,
    val projectIconFile: Path? = null,

    val externalExtensionsDirectory: Path? = null,
    val externalConfigDirectory: Path? = null,
    val externalDbDriversDirectory: Path? = null,

    val javadocUrl: String? = null,

    val platformVersion: String? = null,

    val foundModules: Collection<ModuleDescriptor>,

    val detectedVcs: Collection<Path>,
    val excludedFromScanning: Collection<String>,

    val configModuleDescriptor: ConfigModuleDescriptor,
    val platformModuleDescriptor: PlatformModuleDescriptor,

    val chosenHybrisModuleDescriptors: Collection<ModuleDescriptor>,
    val chosenOtherModuleDescriptors: Collection<ModuleDescriptor>,

    // Must not be used in PostImportConfigurators as it will be cleared beforehand
    // represents intermediate state of the storage to be persisted by ProjectImportTask
    val mutableStorage: MutableWorkspace = MutableWorkspace(),
) {
    val allChosenModuleDescriptors
        get() = chosenHybrisModuleDescriptors + chosenOtherModuleDescriptors

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null

    data class MutableWorkspace(
        private val _contentRootEntities: MutableMap<ModuleEntity, MutableList<ContentRootEntityBuilder>> = mutableMapOf(),
        private val _libraryEntities: MutableMap<ModuleEntity, MutableList<LibraryEntityBuilder>> = mutableMapOf(),
        private val _facetEntities: MutableMap<ModuleEntity, MutableList<FacetEntityBuilder>> = mutableMapOf(),
        private val _dependencyItems: MutableMap<ModuleEntity, MutableList<ModuleDependencyItem>> = mutableMapOf(),
        private val _javaSettingsEntities: MutableMap<ModuleEntity, JavaModuleSettingsEntityBuilder> = mutableMapOf(),
    ) {
        private var accessAllowed = true

        private fun <T> access(exec: () -> T) = if (accessAllowed) exec()
        else throw IllegalStateException("Access is not allowed at this point.")

        val contentRootEntities
            get() = access { _contentRootEntities.mapValues { (_, list) -> list.toList() } }
        val libraryEntities
            get() = access { _libraryEntities.mapValues { (_, list) -> list.toList() } }
        val facetEntities
            get() = access { _facetEntities.mapValues { (_, list) -> list.toList() } }
        val dependencyItems
            get() = access { _dependencyItems.mapValues { (_, list) -> list.toList() } }
        val javaSettingsEntities
            get() = access { _javaSettingsEntities.toList() }

        fun add(moduleEntity: ModuleEntity, entity: ContentRootEntityBuilder) = access {
            _contentRootEntities.getOrPut(moduleEntity) { mutableListOf() }.add(entity)
        }

        fun add(moduleEntity: ModuleEntity, entity: LibraryEntityBuilder) = access {
            _libraryEntities.getOrPut(moduleEntity) { mutableListOf() }.add(entity)
        }

        fun add(moduleEntity: ModuleEntity, entity: ModuleDependencyItem) = access {
            _dependencyItems.getOrPut(moduleEntity) { mutableListOf() }.add(entity)
        }

        fun add(moduleEntity: ModuleEntity, entity: FacetEntityBuilder) = access {
            _facetEntities.getOrPut(moduleEntity) { mutableListOf() }.add(entity)
        }

        fun set(moduleEntity: ModuleEntity, entity: JavaModuleSettingsEntityBuilder) = access {
            _javaSettingsEntities[moduleEntity] = entity
        }

        fun clear() = access {
            // Any access to the class must not take place after cleanup
            accessAllowed = false

            _contentRootEntities.clear()
            _libraryEntities.clear()
            _facetEntities.clear()
            _javaSettingsEntities.clear()
            _dependencyItems.clear()
        }
    }

    data class Mutable(
        val rootDirectory: Path,
        val refresh: Boolean,
        val settings: ProjectImportSettings,

        var project: Project? = null,
        var modulesFilesDirectory: Path? = null,
        var ccv2Token: String? = null,
        var sourceCodePath: Path? = null,
        var sourceCodeFile: Path? = null,
        var projectIconFile: Path? = null,
        var platformDirectory: Path? = null,
        var externalExtensionsDirectory: Path? = null,
        var externalConfigDirectory: Path? = null,
        var externalDbDriversDirectory: Path? = null,
        var javadocUrl: String? = null,
        var platformVersion: String? = null,

        private val _foundModules: MutableCollection<ModuleDescriptor> = mutableListOf(),
        private val _chosenModuleDescriptors: MutableMap<ModuleGroup, Collection<ModuleDescriptor>> = mutableMapOf(),
        private val _detectedVcs: MutableCollection<Path> = mutableSetOf(),
        private val _excludedFromScanning: MutableCollection<String> = mutableSetOf()
    ) {
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

        fun addModule(moduleDescriptor: ModuleDescriptor) = _foundModules.add(moduleDescriptor)
        fun addVcs(file: Path) = _detectedVcs.add(file)

        fun clear() {
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
            sourceCodePath = sourceCodePath,
            sourceCodeFile = sourceCodeFile,
            projectIconFile = projectIconFile,
            platformDirectory = platformDirectory
                ?: throw HybrisConfigurationException("Unable to find platform directory"),
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

            detectedVcs = _detectedVcs.toImmutableSet(),
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