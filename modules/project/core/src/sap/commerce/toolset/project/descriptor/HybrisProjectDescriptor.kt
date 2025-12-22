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

package sap.commerce.toolset.project.descriptor

import com.intellij.openapi.project.Project
import sap.commerce.toolset.project.tasks.TaskProgressProcessor
import java.io.File

interface HybrisProjectDescriptor {

    fun setHybrisProject(project: Project?)

    // TODO: review this method
    fun clear()

    fun setRootDirectoryAndScanForModules(
        progressListenerProcessor: TaskProgressProcessor<File>?,
        errorsProcessor: TaskProgressProcessor<MutableList<File>>?
    )

    val importContext: ProjectImportSettings
    val project: Project?
    val refresh: Boolean
    val foundModules: MutableList<ModuleDescriptor>
    val chosenModuleDescriptors: MutableList<ModuleDescriptor>

    var configHybrisModuleDescriptor: ConfigModuleDescriptor?
    var platformHybrisModuleDescriptor: PlatformModuleDescriptor
    var kotlinNatureModuleDescriptor: ModuleDescriptor?

    val rootDirectory: File?
    var modulesFilesDirectory: File?
    var ccv2Token: String?
    var sourceCodeFile: File?
    var projectIconFile: File?
    var openProjectSettingsAfterImport: Boolean

    var hybrisDistributionDirectory: File?
    var externalExtensionsDirectory: File?
    var externalConfigDirectory: File?
    var externalDbDriversDirectory: File?

    var javadocUrl: String?

    var hybrisVersion: String?
    val detectedVcs: MutableSet<File>

    var excludedFromScanning: Set<String>

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null
}