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

package sap.commerce.toolset.settings

import com.intellij.openapi.application.Application
import com.intellij.openapi.components.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import org.apache.commons.lang3.StringUtils
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.settings.state.ApplicationSettingsState

@State(
    name = "[y] Global Settings",
    category = SettingsCategory.PLUGINS,
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS, roamingType = RoamingType.LOCAL)]
)
@Service
class ApplicationSettings : SerializablePersistentStateComponent<ApplicationSettingsState>(ApplicationSettingsState()), ModificationTracker {

    var groupModules: Boolean
        get() = state.groupModules
        set(value) {
            updateState { it.copy(groupModules = value) }
        }
    var groupExternalModules: Boolean
        get() = state.groupExternalModules
        set(value) {
            updateState { it.copy(groupExternalModules = value) }
        }
    var hideEmptyMiddleFolders: Boolean
        get() = state.hideEmptyMiddleFolders
        set(value) {
            updateState { it.copy(hideEmptyMiddleFolders = value) }
        }
    var importOOTBModulesInReadOnlyMode: Boolean
        get() = state.defaultPlatformInReadOnly
        set(value) {
            updateState { it.copy(defaultPlatformInReadOnly = value) }
        }
    var useRealPathForModuleRoot: Boolean
        get() = state.useRealPathForModuleRoot
        set(value) {
            updateState { it.copy(useRealPathForModuleRoot = value) }
        }
    var ignoreNonExistingSourceDirectories: Boolean
        get() = state.ignoreNonExistingSourceDirectories
        set(value) {
            updateState { it.copy(ignoreNonExistingSourceDirectories = value) }
        }
    var librarySourcesFetchMode: LibrarySourcesFetchMode
        get() = state.librarySourcesFetchMode
        set(value) {
            updateState { it.copy(librarySourcesFetchMode = value) }
        }
    var withExternalLibrarySources: Boolean
        get() = state.withExternalLibrarySources
        set(value) {
            updateState { it.copy(withExternalLibrarySources = value) }
        }
    var withExternalLibraryJavadocs: Boolean
        get() = state.withExternalLibraryJavadocs
        set(value) {
            updateState { it.copy(withExternalLibraryJavadocs = value) }
        }
    var useFakeOutputPathForCustomExtensions: Boolean
        get() = state.useFakeOutputPathForCustomExtensions
        set(value) {
            updateState { it.copy(useFakeOutputPathForCustomExtensions = value) }
        }
    var importCustomAntBuildFiles: Boolean
        get() = state.importCustomAntBuildFiles
        set(value) {
            updateState { it.copy(importCustomAntBuildFiles = value) }
        }
    var groupHybris: String
        get() = state.groupHybris
        set(value) {
            updateState { it.copy(groupHybris = value) }
        }
    var groupOtherHybris: String
        get() = state.groupOtherHybris
        set(value) {
            updateState { it.copy(groupOtherHybris = value) }
        }
    var groupCustom: String
        get() = state.groupCustom
        set(value) {
            updateState { it.copy(groupCustom = value) }
        }
    var groupNonHybris: String
        get() = state.groupNonHybris
        set(value) {
            updateState { it.copy(groupNonHybris = value) }
        }
    var groupOtherCustom: String
        get() = state.groupOtherCustom
        set(value) {
            updateState { it.copy(groupOtherCustom = value) }
        }
    var groupPlatform: String
        get() = state.groupPlatform
        set(value) {
            updateState { it.copy(groupPlatform = value) }
        }
    var groupCCv2: String
        get() = state.groupCCv2
        set(value) {
            updateState { it.copy(groupCCv2 = value) }
        }
    var groupNameExternalModules: String
        get() = state.groupNameExternalModules
        set(value) {
            updateState { it.copy(groupNameExternalModules = value) }
        }
    var externalDbDriversDirectory: String?
        get() = state.externalDbDriversDirectory
        set(value) {
            updateState { it.copy(externalDbDriversDirectory = value) }
        }
    var junkDirectoryList: List<String>
        get() = state.junkDirectoryList
        set(value) {
            updateState { it.copy(junkDirectoryList = value) }
        }
    var extensionsResourcesToExclude: List<String>
        get() = state.extensionsResourcesToExclude
        set(value) {
            updateState { it.copy(extensionsResourcesToExclude = value) }
        }
    var excludedFromIndexList: List<String>
        get() = state.excludedFromIndexList
        set(value) {
            updateState { it.copy(excludedFromIndexList = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(): ApplicationSettings = application.service()

        @JvmStatic
        fun toIdeaGroup(group: String?): Array<String>? {
            if (group == null || group.trim { it <= ' ' }.isEmpty()) {
                return null
            }
            return StringUtils.split(group, " ,.;>/\\")
        }
    }
}

val Application.ySettings
    get() = ApplicationSettings.getInstance()


fun String.toIdeaGroup(): Array<String>? {
    if (this.trim { it <= ' ' }.isEmpty()) {
        return null
    }
    return StringUtils.split(this, " ,.;>/\\")
}