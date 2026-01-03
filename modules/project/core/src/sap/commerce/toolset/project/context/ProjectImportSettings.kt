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

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.settings.ApplicationSettings

data class ProjectImportSettings(
    val importOOTBModulesInReadOnlyMode: Boolean,
    val followSymlink: Boolean,
    val excludeTestSources: Boolean,
    val importCustomAntBuildFiles: Boolean,
    val ignoreNonExistingSourceDirectories: Boolean,
    val hideEmptyMiddleFolders: Boolean,
    val useFakeOutputPathForCustomExtensions: Boolean,
    val withStandardProvidedSources: Boolean,
    val withExternalLibrarySources: Boolean,
    val withExternalLibraryJavadocs: Boolean,
    val groupModules: Boolean,
    val groupExternalModules: Boolean,
    val groupHybris: String,
    val groupOtherHybris: String,
    val groupCustom: String,
    val groupNonHybris: String,
    val groupOtherCustom: String,
    val groupPlatform: String,
    val groupCCv2: String,
    val groupNameExternalModules: String,
) {

    fun mutable() = Mutable(
        importOOTBModulesInReadOnlyMode = AtomicBooleanProperty(importOOTBModulesInReadOnlyMode),
        followSymlink = AtomicBooleanProperty(followSymlink),
        excludeTestSources = AtomicBooleanProperty(excludeTestSources),
        importCustomAntBuildFiles = AtomicBooleanProperty(importCustomAntBuildFiles),
        ignoreNonExistingSourceDirectories = AtomicBooleanProperty(ignoreNonExistingSourceDirectories),
        hideEmptyMiddleFolders = AtomicBooleanProperty(hideEmptyMiddleFolders),
        useFakeOutputPathForCustomExtensions = AtomicBooleanProperty(useFakeOutputPathForCustomExtensions),
        withStandardProvidedSources = AtomicBooleanProperty(withStandardProvidedSources),
        withExternalLibrarySources = AtomicBooleanProperty(withExternalLibrarySources),
        withExternalLibraryJavadocs = AtomicBooleanProperty(withExternalLibraryJavadocs),
        groupModules = AtomicBooleanProperty(groupModules),
        groupExternalModules = AtomicBooleanProperty(groupExternalModules),
        groupHybris = AtomicProperty(groupHybris),
        groupOtherHybris = AtomicProperty(groupOtherHybris),
        groupCustom = AtomicProperty(groupCustom),
        groupNonHybris = AtomicProperty(groupNonHybris),
        groupOtherCustom = AtomicProperty(groupOtherCustom),
        groupPlatform = AtomicProperty(groupPlatform),
        groupCCv2 = AtomicProperty(groupCCv2),
        groupNameExternalModules = AtomicProperty(groupNameExternalModules),
    )

    data class Mutable(
        val importOOTBModulesInReadOnlyMode: AtomicBooleanProperty,
        val followSymlink: AtomicBooleanProperty,
        val excludeTestSources: AtomicBooleanProperty,
        val importCustomAntBuildFiles: AtomicBooleanProperty,
        val ignoreNonExistingSourceDirectories: AtomicBooleanProperty,
        val hideEmptyMiddleFolders: AtomicBooleanProperty,
        val useFakeOutputPathForCustomExtensions: AtomicBooleanProperty,
        val withStandardProvidedSources: AtomicBooleanProperty,
        val withExternalLibrarySources: AtomicBooleanProperty,
        val withExternalLibraryJavadocs: AtomicBooleanProperty,
        val groupModules: AtomicBooleanProperty,
        val groupExternalModules: AtomicBooleanProperty,
        val groupHybris: AtomicProperty<String>,
        val groupOtherHybris: AtomicProperty<String>,
        val groupCustom: AtomicProperty<String>,
        val groupNonHybris: AtomicProperty<String>,
        val groupOtherCustom: AtomicProperty<String>,
        val groupPlatform: AtomicProperty<String>,
        val groupCCv2: AtomicProperty<String>,
        val groupNameExternalModules: AtomicProperty<String>,
    ) {
        fun immutable() = ProjectImportSettings(
            importOOTBModulesInReadOnlyMode = importOOTBModulesInReadOnlyMode.get(),
            followSymlink = followSymlink.get(),
            excludeTestSources = excludeTestSources.get(),
            importCustomAntBuildFiles = importCustomAntBuildFiles.get(),
            ignoreNonExistingSourceDirectories = ignoreNonExistingSourceDirectories.get(),
            hideEmptyMiddleFolders = hideEmptyMiddleFolders.get(),
            useFakeOutputPathForCustomExtensions = useFakeOutputPathForCustomExtensions.get(),
            withStandardProvidedSources = withStandardProvidedSources.get(),
            withExternalLibrarySources = withExternalLibrarySources.get(),
            withExternalLibraryJavadocs = withExternalLibraryJavadocs.get(),
            groupModules = groupModules.get(),
            groupExternalModules = groupExternalModules.get(),
            groupHybris = groupHybris.get(),
            groupOtherHybris = groupOtherHybris.get(),
            groupCustom = groupCustom.get(),
            groupNonHybris = groupNonHybris.get(),
            groupOtherCustom = groupOtherCustom.get(),
            groupPlatform = groupPlatform.get(),
            groupCCv2 = groupCCv2.get(),
            groupNameExternalModules = groupNameExternalModules.get(),
        )
    }

    companion object {
        fun of(applicationSettings: ApplicationSettings) = ProjectImportSettings(
            importOOTBModulesInReadOnlyMode = applicationSettings.defaultPlatformInReadOnly,
            followSymlink = applicationSettings.followSymlink,
            excludeTestSources = applicationSettings.excludeTestSources,
            importCustomAntBuildFiles = applicationSettings.importCustomAntBuildFiles,
            ignoreNonExistingSourceDirectories = applicationSettings.ignoreNonExistingSourceDirectories,
            hideEmptyMiddleFolders = applicationSettings.hideEmptyMiddleFolders,
            useFakeOutputPathForCustomExtensions = applicationSettings.useFakeOutputPathForCustomExtensions,
            withStandardProvidedSources = applicationSettings.withStandardProvidedSources,
            withExternalLibrarySources = applicationSettings.withExternalLibrarySources,
            withExternalLibraryJavadocs = applicationSettings.withExternalLibraryJavadocs,
            groupModules = applicationSettings.groupModules,
            groupExternalModules = applicationSettings.groupExternalModules,
            groupHybris = applicationSettings.groupHybris,
            groupOtherHybris = applicationSettings.groupOtherHybris,
            groupCustom = applicationSettings.groupCustom,
            groupNonHybris = applicationSettings.groupNonHybris,
            groupOtherCustom = applicationSettings.groupOtherCustom,
            groupPlatform = applicationSettings.groupPlatform,
            groupCCv2 = applicationSettings.groupCCv2,
            groupNameExternalModules = applicationSettings.groupNameExternalModules,
        )

        fun of(applicationSettings: ApplicationSettings, projectSettings: ProjectSettings) = ProjectImportSettings(
            importOOTBModulesInReadOnlyMode = projectSettings.importOotbModulesInReadOnlyMode,
            followSymlink = projectSettings.followSymlink,
            excludeTestSources = projectSettings.excludeTestSources,
            importCustomAntBuildFiles = projectSettings.importCustomAntBuildFiles,

            ignoreNonExistingSourceDirectories = applicationSettings.ignoreNonExistingSourceDirectories,
            hideEmptyMiddleFolders = applicationSettings.hideEmptyMiddleFolders,
            useFakeOutputPathForCustomExtensions = projectSettings.useFakeOutputPathForCustomExtensions,
            withStandardProvidedSources = applicationSettings.withStandardProvidedSources,
            withExternalLibrarySources = applicationSettings.withExternalLibrarySources,
            withExternalLibraryJavadocs = applicationSettings.withExternalLibraryJavadocs,
            groupModules = applicationSettings.groupModules,
            groupExternalModules = applicationSettings.groupExternalModules,
            groupHybris = applicationSettings.groupHybris,
            groupOtherHybris = applicationSettings.groupOtherHybris,
            groupCustom = applicationSettings.groupCustom,
            groupNonHybris = applicationSettings.groupNonHybris,
            groupOtherCustom = applicationSettings.groupOtherCustom,
            groupPlatform = applicationSettings.groupPlatform,
            groupCCv2 = applicationSettings.groupCCv2,
            groupNameExternalModules = applicationSettings.groupNameExternalModules,
        )
    }
}