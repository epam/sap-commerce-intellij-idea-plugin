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

import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.util.toSystemIndependentName

class ApplicationSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Application Settings"

    override fun preConfigure(importContext: ProjectImportContext) {
        if (importContext.refresh) return

        val importSettings = importContext.settings

        with(ApplicationSettings.getInstance()) {
            this.externalDbDriversDirectory = importContext.externalDbDriversDirectory?.toSystemIndependentName

            this.defaultPlatformInReadOnly = importSettings.importOOTBModulesInReadOnlyMode
            this.followSymlink = importSettings.followSymlink
            this.excludeTestSources = importSettings.excludeTestSources
            this.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
            this.ignoreNonExistingSourceDirectories = importSettings.ignoreNonExistingSourceDirectories
            this.hideEmptyMiddleFolders = importSettings.hideEmptyMiddleFolders
            this.withStandardProvidedSources = importSettings.withStandardProvidedSources
            this.withExternalLibrarySources = importSettings.withExternalLibrarySources
            this.withExternalLibraryJavadocs = importSettings.withExternalLibraryJavadocs
        }
    }
}
