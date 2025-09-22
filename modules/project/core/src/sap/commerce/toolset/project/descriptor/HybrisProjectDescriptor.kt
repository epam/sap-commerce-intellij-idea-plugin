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
    fun clear()
    fun setRootDirectoryAndScanForModules(
        rootDirectory: File,
        progressListenerProcessor: TaskProgressProcessor<File>?,
        errorsProcessor: TaskProgressProcessor<MutableList<File>>?
    )

    var project: Project?
    var refresh: Boolean
    val foundModules: MutableList<ModuleDescriptor>
    var chosenModuleDescriptors: MutableList<ModuleDescriptor>
    val configHybrisModuleDescriptor: ConfigModuleDescriptor?
    val platformHybrisModuleDescriptor: PlatformModuleDescriptor
    val kotlinNatureModuleDescriptor: ModuleDescriptor?

    val alreadyOpenedModules: MutableSet<ModuleDescriptor?>
    val rootDirectory: File?
    var modulesFilesDirectory: File?
    var ccv2Token: String?
    var sourceCodeFile: File?
    var projectIconFile: File?
    var isOpenProjectSettingsAfterImport: Boolean
    var isImportOotbModulesInReadOnlyMode: Boolean
    var hybrisDistributionDirectory: File?
    var externalExtensionsDirectory: File?
    var externalConfigDirectory: File?
    var externalDbDriversDirectory: File?
    var isIgnoreNonExistingSourceDirectories: Boolean
    var isUseFakeOutputPathForCustomExtensions: Boolean
    var javadocUrl: String?
    var isFollowSymlink: Boolean
    var isExcludeTestSources: Boolean
    var isImportCustomAntBuildFiles: Boolean
    var isScanThroughExternalModule: Boolean
    var hybrisVersion: String?
    val detectedVcs: MutableSet<File>
    var isWithStandardProvidedSources: Boolean
    var excludedFromScanning: MutableSet<String>
    val excludedFromScanningDirectories: MutableSet<File>?

    val yModuleDescriptorsToImport: Map<String, YModuleDescriptor>
    val moduleDescriptorsToImport: Map<String, ModuleDescriptor>

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null
}