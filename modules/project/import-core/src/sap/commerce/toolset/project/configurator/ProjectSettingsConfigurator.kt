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
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.directory
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.settings.WorkspaceSettings
import sap.commerce.toolset.util.toSystemIndependentName
import java.io.File
import kotlin.io.path.Path

class ProjectSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Project Settings"

    override suspend fun preConfigure(importContext: ProjectImportContext) {
        val project = importContext.project
        val workspaceSettings = WorkspaceSettings.getInstance(project)
        val projectSettings = ProjectSettings.getInstance(project)
        workspaceSettings.hybrisProject = true

        Plugin.HYBRIS.pluginDescriptor
            ?.let { workspaceSettings.importedByVersion = it.version }

        applySettings(importContext)

        importContext.ifImport {
            saveCustomDirectoryLocation(importContext)
            projectSettings.excludedFromScanning = importContext.excludedFromScanning.toSet()
        }
    }

    private fun applySettings(importContext: ProjectImportContext) {
        val projectSettings = importContext.project.ySettings
        val importSettings = importContext.settings

        projectSettings.importOotbModulesInReadOnlyMode = importSettings.importOOTBModulesInReadOnlyMode
        projectSettings.followSymlink = importSettings.followSymlink
        projectSettings.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
        projectSettings.useFakeOutputPathForCustomExtensions = importSettings.useFakeOutputPathForCustomExtensions

        projectSettings.externalExtensionsDirectory = importContext.externalExtensionsDirectory?.toSystemIndependentName
        projectSettings.externalConfigDirectory = importContext.externalConfigDirectory?.toSystemIndependentName
        projectSettings.ideModulesFilesDirectory = importContext.modulesFilesDirectory?.toSystemIndependentName
        projectSettings.externalDbDriversDirectory = importContext.externalDbDriversDirectory?.toSystemIndependentName
        projectSettings.configDirectory = importContext.configModuleDescriptor.moduleRootPath.toSystemIndependentName

        projectSettings.modulesOnBlackList = createModulesOnBlackList(importContext)
        projectSettings.hybrisVersion = importContext.platformVersion
        projectSettings.javadocUrl = importContext.javadocUrl

        projectSettings.sourceCodePath = importContext.sourceCodePath?.toSystemIndependentName
    }

    private fun saveCustomDirectoryLocation(importContext: ProjectImportContext) {
        val project = importContext.project
        val projectDir = project.directory?.let { Path(it) } ?: return
        val projectSettings = project.ySettings
        val platformPath = importContext.platformDirectory ?: return

        projectSettings.hybrisDirectory = projectDir
            .relativize(platformPath)
            .toString()

        importContext.externalExtensionsDirectory
            ?.let {
                val relativeCustomPath = platformPath.relativize(it)
                projectSettings.customDirectory = relativeCustomPath.toString()
            }
    }

    private val File.directorySystemIndependentName: String?
        get() = this
            .takeIf { it.exists() }
            ?.takeIf { it.isDirectory }
            ?.let { FileUtil.toSystemIndependentName(it.path) }

    private val File.fileSystemIndependentName: String?
        get() = this
            .takeIf { it.exists() }
            ?.takeIf { it.isFile }
            ?.let { FileUtil.toSystemIndependentName(it.path) }

    private fun createModulesOnBlackList(importContext: ProjectImportContext): Set<String> {
        val chosenHybrisModuleDescriptors = importContext.chosenHybrisModuleDescriptors
        val toBeImportedNames = chosenHybrisModuleDescriptors
            .map { it.name }
            .toSet()

        return importContext.foundModules
            .filterNot { chosenHybrisModuleDescriptors.contains(it) }
            .filter { toBeImportedNames.contains(it.name) }
            .map { it.getRelativePath(importContext.rootDirectory) }
            .toSet()
    }
}
