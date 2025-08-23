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

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.settings.WorkspaceSettings

class ProjectSettingsConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = "Project Settings"

    override fun preConfigure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>
    ) {
        val project = hybrisProjectDescriptor.project ?: return
        val projectSettings = ProjectSettings.getInstance(project)
        WorkspaceSettings.getInstance(project).hybrisProject = true

        Plugin.HYBRIS.pluginDescriptor
            ?.let { projectSettings.importedByVersion = it.version }

        hybrisProjectDescriptor.ifImport {
            saveCustomDirectoryLocation(project, hybrisProjectDescriptor, projectSettings)
            projectSettings.excludedFromScanning = hybrisProjectDescriptor.excludedFromScanning
        }
    }

    private fun saveCustomDirectoryLocation(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        projectSettings: ProjectSettings
    ) {
        val projectDir = project.guessProjectDir() ?: return
        val hybrisPath = hybrisProjectDescriptor.hybrisDistributionDirectory
            ?.toPath() ?: return

        projectSettings.hybrisDirectory = VfsUtilCore.virtualToIoFile(projectDir)
            .toPath()
            .relativize(hybrisPath)
            .toString()

        hybrisProjectDescriptor.externalExtensionsDirectory
            ?.toPath()
            ?.let {
                val relativeCustomPath = hybrisPath.relativize(it)
                projectSettings.customDirectory = relativeCustomPath.toString()
            }
    }
}
