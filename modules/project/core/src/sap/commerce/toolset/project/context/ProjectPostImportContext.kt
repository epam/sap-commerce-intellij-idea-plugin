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

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.storage.ImmutableEntityStorage
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.nio.file.Path

data class ProjectPostImportContext(
    val project: Project,
    val rootDirectory: Path,
    val platformDirectory: Path,
    val refresh: Boolean,
    val settings: ProjectImportSettings,
    val storage: ImmutableEntityStorage,

    val modulesFilesDirectory: Path? = null,
    val ccv2Token: String? = null,
    val sourceCodePath: Path? = null,
    val sourceCodeFile: Path? = null,
    val projectIconFile: Path? = null,

    val externalExtensionsDirectory: Path? = null,
    val externalConfigDirectory: Path? = null,
    val externalDbDriversDirectory: Path? = null,

    val javadocUrl: String? = null,

    val platformVersion: String? = null,

    val foundModules: Collection<ModuleDescriptor>,

    val detectedVcs: Collection<Path>,
    val excludedFromScanning: Collection<String>,

    val configModuleDescriptor: ConfigModuleDescriptor,
    val platformModuleDescriptor: PlatformModuleDescriptor,

    val chosenHybrisModuleDescriptors: Collection<ModuleDescriptor>,
    val chosenOtherModuleDescriptors: Collection<ModuleDescriptor>,
) {
    val workspace = WorkspaceModel.getInstance(project)
    val modules = ModuleManager.getInstance(project).modules
        .associateBy { it.name }

    val allChosenModuleDescriptors
        get() = chosenHybrisModuleDescriptors + chosenOtherModuleDescriptors

    fun <T> ifRefresh(operation: () -> T): T? = if (refresh) operation() else null
    fun <T> ifImport(operation: () -> T): T? = if (!refresh) operation() else null

    companion object {
        fun from(context: ProjectImportContext, storage: ImmutableEntityStorage) = ProjectPostImportContext(
            storage = storage,
            project = context.project,
            rootDirectory = context.rootDirectory,
            platformDirectory = context.platformDirectory,
            refresh = context.refresh,
            settings = context.settings,
            modulesFilesDirectory = context.modulesFilesDirectory,
            ccv2Token = context.ccv2Token,
            sourceCodePath = context.sourceCodePath,
            sourceCodeFile = context.sourceCodeFile,
            projectIconFile = context.projectIconFile,
            externalExtensionsDirectory = context.externalExtensionsDirectory,
            externalConfigDirectory = context.externalConfigDirectory,
            externalDbDriversDirectory = context.externalDbDriversDirectory,
            javadocUrl = context.javadocUrl,
            platformVersion = context.platformVersion,
            foundModules = context.foundModules,
            detectedVcs = context.detectedVcs,
            excludedFromScanning = context.excludedFromScanning,
            configModuleDescriptor = context.configModuleDescriptor,
            platformModuleDescriptor = context.platformModuleDescriptor,
            chosenHybrisModuleDescriptors = context.chosenHybrisModuleDescriptors,
            chosenOtherModuleDescriptors = context.chosenOtherModuleDescriptors,
        )
    }
}