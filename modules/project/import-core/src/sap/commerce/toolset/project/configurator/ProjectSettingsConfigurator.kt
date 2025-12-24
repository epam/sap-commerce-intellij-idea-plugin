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
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.settings.WorkspaceSettings
import java.io.File
import kotlin.io.path.Path

class ProjectSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Project Settings"

    override fun preConfigure(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val workspaceSettings = WorkspaceSettings.getInstance(project)
        val projectSettings = ProjectSettings.getInstance(project)
        workspaceSettings.hybrisProject = true

        Plugin.HYBRIS.pluginDescriptor
            ?.let { workspaceSettings.importedByVersion = it.version }

        applySettings(hybrisProjectDescriptor)

        hybrisProjectDescriptor.ifImport {
            saveCustomDirectoryLocation(hybrisProjectDescriptor)
            projectSettings.excludedFromScanning = hybrisProjectDescriptor.excludedFromScanning
        }
    }

    private fun applySettings(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val projectSettings = hybrisProjectDescriptor.project?.ySettings ?: return
        val importSettings = hybrisProjectDescriptor.importContext

        with(projectSettings) {
            this.importOotbModulesInReadOnlyMode = importSettings.importOOTBModulesInReadOnlyMode
            this.followSymlink = importSettings.followSymlink
            this.scanThroughExternalModule = importSettings.scanThroughExternalModule
            this.excludeTestSources = importSettings.excludeTestSources
            this.importCustomAntBuildFiles = importSettings.importCustomAntBuildFiles
            this.useFakeOutputPathForCustomExtensions = importSettings.useFakeOutputPathForCustomExtensions
        }

        projectSettings.externalExtensionsDirectory = hybrisProjectDescriptor.externalExtensionsDirectory?.directorySystemIndependentName
        projectSettings.externalConfigDirectory = hybrisProjectDescriptor.externalConfigDirectory?.directorySystemIndependentName
        projectSettings.ideModulesFilesDirectory = hybrisProjectDescriptor.modulesFilesDirectory?.directorySystemIndependentName
        projectSettings.externalDbDriversDirectory = hybrisProjectDescriptor.externalDbDriversDirectory?.directorySystemIndependentName
        projectSettings.configDirectory = hybrisProjectDescriptor.configHybrisModuleDescriptor?.moduleRootDirectory?.directorySystemIndependentName

        projectSettings.modulesOnBlackList = createModulesOnBlackList(hybrisProjectDescriptor)
        projectSettings.hybrisVersion = hybrisProjectDescriptor.hybrisVersion
        projectSettings.javadocUrl = hybrisProjectDescriptor.javadocUrl

        projectSettings.sourceCodeFile = hybrisProjectDescriptor.sourceCodeFile
            ?.takeIf { it.exists() }
            ?.fileSystemIndependentName
        projectSettings.availableExtensions = hybrisProjectDescriptor.foundModules
            .asSequence()
            .filterNot { it is YSubModuleDescriptor }
            .filterIsInstance<YModuleDescriptor>()
            .toSet()
            .map { it.extensionDescriptor() }
            .associateBy { it.name }
    }

    private fun saveCustomDirectoryLocation(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val projectDir = project.directory?.let { Path(it) } ?: return
        val projectSettings = project.ySettings
        val hybrisPath = hybrisProjectDescriptor.hybrisDistributionDirectory
            ?.toPath() ?: return

        projectSettings.hybrisDirectory = projectDir
            .relativize(hybrisPath)
            .toString()

        hybrisProjectDescriptor.externalExtensionsDirectory
            ?.toPath()
            ?.let {
                val relativeCustomPath = hybrisPath.relativize(it)
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

    private fun createModulesOnBlackList(hybrisProjectDescriptor: HybrisProjectDescriptor): Set<String> {
        val chosenModuleDescriptors = hybrisProjectDescriptor.chosenModuleDescriptors
        val toBeImportedNames = chosenModuleDescriptors
            .map { it.name }
            .toSet()

        return hybrisProjectDescriptor.foundModules
            .filterNot { chosenModuleDescriptors.contains(it) }
            .filter { toBeImportedNames.contains(it.name) }
            .map { it.getRelativePath() }
            .toSet()
    }
}
