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

class ApplicationSettingsConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Application Settings"

    override suspend fun configure(context: ProjectImportContext) {
        val importSettings = context.settings

        with(ApplicationSettings.getInstance()) {
            this.externalDbDriversDirectory = context.externalDbDriversDirectory?.toSystemIndependentName

            this.importOOTBModulesInReadOnlyMode = importSettings.importOOTBModulesInReadOnlyMode
            this.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
            this.ignoreNonExistingSourceDirectories = importSettings.ignoreNonExistingSourceDirectories
            this.hideEmptyMiddleFolders = importSettings.hideEmptyMiddleFolders
            this.withExternalLibrarySources = importSettings.withExternalLibrarySources
            this.withExternalLibraryJavadocs = importSettings.withExternalLibraryJavadocs
            this.groupModules = importSettings.groupModules
            this.groupExternalModules = importSettings.groupExternalModules
            this.groupHybris = importSettings.groupHybris
            this.groupOtherHybris = importSettings.groupOtherHybris
            this.groupCustom = importSettings.groupCustom
            this.groupNonHybris = importSettings.groupNonHybris
            this.groupOtherCustom = importSettings.groupOtherCustom
            this.groupPlatform = importSettings.groupPlatform
            this.groupCCv2 = importSettings.groupCCv2
            this.groupNameExternalModules = importSettings.groupNameExternalModules
            this.extensionsResourcesToExclude = importSettings.extensionsResourcesToExclude.toList()
        }
    }
}
