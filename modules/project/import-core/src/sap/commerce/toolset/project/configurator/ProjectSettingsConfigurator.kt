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

import sap.commerce.toolset.Plugin
import sap.commerce.toolset.directory
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.settings.WorkspaceSettings
import sap.commerce.toolset.util.toSystemIndependentName
import kotlin.io.path.Path

class ProjectSettingsConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Project Settings"

    override suspend fun configure(context: ProjectImportContext) {
        val project = context.project
        val workspaceSettings = WorkspaceSettings.getInstance(project)
        val projectSettings = ProjectSettings.getInstance(project)
        workspaceSettings.hybrisProject = true

        Plugin.HYBRIS.pluginDescriptor
            ?.let { workspaceSettings.importedByVersion = it.version }

        applySettings(context)

        context.ifImport {
            saveCustomDirectoryLocation(context)
            projectSettings.excludedFromScanning = context.excludedFromScanning.toSet()
        }
    }

    private fun applySettings(context: ProjectImportContext) {
        val projectSettings = context.project.ySettings
        val importSettings = context.settings

        projectSettings.importOotbModulesInReadOnlyMode = importSettings.importOOTBModulesInReadOnlyMode
        projectSettings.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
        projectSettings.useFakeOutputPathForCustomExtensions = importSettings.useFakeOutputPathForCustomExtensions
        projectSettings.withDecompiledOotbSources = importSettings.withDecompiledOotbSources

        projectSettings.externalExtensionsDirectory = context.externalExtensionsDirectory?.toSystemIndependentName
        projectSettings.externalConfigDirectory = context.externalConfigDirectory?.toSystemIndependentName
        projectSettings.ideModulesFilesDirectory = context.modulesFilesDirectory?.toSystemIndependentName
        projectSettings.externalDbDriversDirectory = context.externalDbDriversDirectory?.toSystemIndependentName
        projectSettings.configDirectory = context.configModuleDescriptor.moduleRootPath.toSystemIndependentName

        projectSettings.modulesOnBlackList = createModulesOnBlackList(context)
        projectSettings.hybrisVersion = context.platformVersion
        projectSettings.javadocUrl = context.javadocUrl

        projectSettings.sourceCodePath = context.sourceCodePath?.toSystemIndependentName

        projectSettings.removeExternalModulesOnRefresh = context.removeExternalModules

        projectSettings.unusedExtensions = context.unusedExtensions.toSet()
    }

    private fun saveCustomDirectoryLocation(context: ProjectImportContext) {
        val project = context.project
        val projectDir = project.directory?.let { Path(it) } ?: return
        val projectSettings = project.ySettings
        val platformPath = context.platformDirectory

        projectSettings.platformRelativePath = projectDir
            .relativize(platformPath)
            .toString()

        context.externalExtensionsDirectory
            ?.let {
                val relativeCustomPath = platformPath.relativize(it)
                projectSettings.customDirectory = relativeCustomPath.toString()
            }
    }

    private fun createModulesOnBlackList(context: ProjectImportContext): Set<String> {
        val chosenHybrisModuleDescriptors = context.chosenHybrisModuleDescriptors
        val toBeImportedNames = chosenHybrisModuleDescriptors
            .map { it.name }
            .toSet()

        return context.foundModules
            .filterNot { chosenHybrisModuleDescriptors.contains(it) }
            .filter { toBeImportedNames.contains(it.name) }
            .map { it.getRelativePath(context.rootDirectory) }
            .toSet()
    }
}
