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
package sap.commerce.toolset.project.configurator

import com.intellij.openapi.util.io.FileUtil
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.settings.ApplicationSettings
import java.io.File

class ApplicationSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Application Settings"

    override fun preConfigure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        if (hybrisProjectDescriptor.refresh) return

        val importSettings = hybrisProjectDescriptor.importContext

        with(ApplicationSettings.getInstance()) {
            this.externalDbDriversDirectory = hybrisProjectDescriptor.externalDbDriversDirectory?.directorySystemIndependentName
            this.defaultPlatformInReadOnly = importSettings.importOOTBModulesInReadOnlyMode
            this.followSymlink = importSettings.followSymlink
            this.scanThroughExternalModule = importSettings.scanThroughExternalModule
            this.excludeTestSources = importSettings.excludeTestSources
            this.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
            this.ignoreNonExistingSourceDirectories = importSettings.ignoreNonExistingSourceDirectories
            this.hideEmptyMiddleFolders = importSettings.hideEmptyMiddleFolders
            this.withStandardProvidedSources = importSettings.withStandardProvidedSources
            this.withExternalLibrarySources = importSettings.withExternalLibrarySources
            this.withExternalLibraryJavadocs = importSettings.withExternalLibraryJavadocs
        }
    }

    private val File.directorySystemIndependentName: String?
        get() = this
            .takeIf { it.exists() }
            ?.takeIf { it.isDirectory }
            ?.let { FileUtil.toSystemIndependentName(it.path) }
}
