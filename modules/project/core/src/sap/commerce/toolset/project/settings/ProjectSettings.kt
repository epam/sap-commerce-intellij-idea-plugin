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

package sap.commerce.toolset.project.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.text.VersionComparatorUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.settings.state.ProjectSettingsState

@State(
    name = "HybrisProjectSettings",
    storages = [
        Storage(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED),
    ]
)
@Service(Service.Level.PROJECT)
class ProjectSettings : SerializablePersistentStateComponent<ProjectSettingsState>(ProjectSettingsState()), ModificationTracker {

    var customDirectory
        get() = state.customDirectory
        set(value) {
            updateState { it.copy(customDirectory = value) }
        }
    var hybrisDirectory
        get() = state.hybrisDirectory
        set(value) {
            updateState { it.copy(hybrisDirectory = value) }
        }
    var configDirectory
        get() = state.configDirectory
        set(value) {
            updateState { it.copy(configDirectory = value) }
        }
    var importedByVersion
        get() = state.importedByVersion
        set(value) {
            updateState { it.copy(importedByVersion = value) }
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
    var sourceCodeFile
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
    var followSymlink
        get() = state.followSymlink
        set(value) {
            updateState { it.copy(followSymlink = value) }
        }
    var scanThroughExternalModule
        get() = state.scanThroughExternalModule
        set(value) {
            updateState { it.copy(scanThroughExternalModule = value) }
        }
    var excludeTestSources
        get() = state.excludeTestSources
        set(value) {
            updateState { it.copy(excludeTestSources = value) }
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

    var availableExtensions: Map<String, ExtensionDescriptor>
        get() = state.availableExtensions
        set(value) {
            updateState { it.copy(availableExtensions = value) }
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

    fun isOutdatedHybrisProject(): Boolean {
        val lastImportVersion = importedByVersion ?: return true
        val currentVersion = Plugin.HYBRIS.pluginDescriptor
            ?.version
            ?: return true

        return VersionComparatorUtil.compare(currentVersion, lastImportVersion) > 0
    }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ProjectSettings = project.service()
    }
}

val Project.ySettings
    get() = ProjectSettings.getInstance(this)
