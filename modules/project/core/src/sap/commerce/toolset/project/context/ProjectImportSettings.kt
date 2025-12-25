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
        )
    }
}