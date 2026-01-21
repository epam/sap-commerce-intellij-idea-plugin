/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.settings.state.ProjectSettingsState

@State(
    name = "HybrisProjectSettings",
    storages = [
        Storage(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS, roamingType = RoamingType.LOCAL),
    ]
)
@Service(Service.Level.PROJECT)
class ProjectSettings : SerializablePersistentStateComponent<ProjectSettingsState>(ProjectSettingsState()), ModificationTracker {

    var customDirectory
        get() = state.customDirectory
        set(value) {
            updateState { it.copy(customDirectory = value) }
        }
    var platformRelativePath
        get() = state.hybrisDirectory
        set(value) {
            updateState { it.copy(hybrisDirectory = value) }
        }
    var configDirectory
        get() = state.configDirectory
        set(value) {
            updateState { it.copy(configDirectory = value) }
        }
    var hybrisVersion
        get() = state.hybrisVersion
        set(value) {
            updateState { it.copy(hybrisVersion = value) }
        }
    var javadocUrl
        get() = state.javadocUrl
        set(value) {
            updateState { it.copy(javadocUrl = value) }
        }
    var sourceCodePath
        get() = state.sourceCodeFile
        set(value) {
            updateState { it.copy(sourceCodeFile = value) }
        }
    var externalExtensionsDirectory
        get() = state.externalExtensionsDirectory
        set(value) {
            updateState { it.copy(externalExtensionsDirectory = value) }
        }
    var externalConfigDirectory
        get() = state.externalConfigDirectory
        set(value) {
            updateState { it.copy(externalConfigDirectory = value) }
        }
    var externalDbDriversDirectory
        get() = state.externalDbDriversDirectory
        set(value) {
            updateState { it.copy(externalDbDriversDirectory = value) }
        }
    var ideModulesFilesDirectory
        get() = state.ideModulesFilesDirectory
        set(value) {
            updateState { it.copy(ideModulesFilesDirectory = value) }
        }
    var importOotbModulesInReadOnlyMode
        get() = state.importOotbModulesInReadOnlyMode
        set(value) {
            updateState { it.copy(importOotbModulesInReadOnlyMode = value) }
        }
    var importCustomAntBuildFiles
        get() = state.importCustomAntBuildFiles
        set(value) {
            updateState { it.copy(importCustomAntBuildFiles = value) }
        }
    var removeExternalModulesOnRefresh
        get() = state.removeExternalModulesOnRefresh
        set(value) {
            updateState { it.copy(removeExternalModulesOnRefresh = value) }
        }
    var withDecompiledOotbSources
        get() = state.withDecompiledOotbSources
        set(value) {
            updateState { it.copy(withDecompiledOotbSources = value) }
        }
    var unusedExtensions
        get() = state.unusedExtensions
        set(value) {
            updateState { it.copy(unusedExtensions = value) }
        }
    var modulesOnBlackList
        get() = state.modulesOnBlackList
        set(value) {
            updateState { it.copy(modulesOnBlackList = value) }
        }
    var excludedFromScanning
        get() = state.excludedFromScanning
        set(value) {
            updateState { it.copy(excludedFromScanning = value) }
        }
    var useFakeOutputPathForCustomExtensions
        get() = state.useFakeOutputPathForCustomExtensions
        set(value) {
            updateState { it.copy(useFakeOutputPathForCustomExtensions = value) }
        }
    var extensionDescriptors: Collection<ExtensionDescriptor>
        get() = state.extensionDescriptors
        set(value) {
            updateState { it.copy(extensionDescriptors = value) }
        }
    var generateCodeOnRebuild
        get() = state.generateCodeOnRebuild
        set(value) {
            updateState { it.copy(generateCodeOnRebuild = value) }
        }
    var generateCodeOnJUnitRunConfiguration
        get() = state.generateCodeOnJUnitRunConfiguration
        set(value) {
            updateState { it.copy(generateCodeOnJUnitRunConfiguration = value) }
        }
    var generateCodeOnServerRunConfiguration
        get() = state.generateCodeOnServerRunConfiguration
        set(value) {
            updateState { it.copy(generateCodeOnServerRunConfiguration = value) }
        }
    var generateCodeTimeoutSeconds
        get() = state.generateCodeTimeoutSeconds
        set(value) {
            updateState { it.copy(generateCodeTimeoutSeconds = value) }
        }
    var showFullModuleName
        get() = state.showFullModuleName
        set(value) {
            updateState { it.copy(showFullModuleName = value) }
        }
    // IDEA module name <-> extension name
    var module2extensionMapping
        get() = state.modulesMapping
        set(value) {
            updateState { it.copy(modulesMapping = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ProjectSettings = project.service()
    }
}

val Project.ySettings
    get() = ProjectSettings.getInstance(this)
