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

package sap.commerce.toolset.project

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import sap.commerce.toolset.project.context.ProjectImportState
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.pathString

private val projectImportState = Key<ProjectImportState>("sap.commerce.toolset.project.import.state")

var Project.importState: ProjectImportState
    get() = this.getUserData(projectImportState) ?: ProjectImportState.IMPORTED
    set(value) {
        this.putUserData(projectImportState, value)
    }

val Module.yExtensionName
    get() = yExtensionName(this.project.ySettings.module2extensionMapping)

fun Module.yExtensionName(moduleMapping: Map<String, String>) = moduleMapping[this.name]

val Module.yExtensionDescriptor
    get() = YFacet.get(this)
        ?.configuration
        ?.state

val Module.contentRoot
    get() = this
        .let { ModuleRootManager.getInstance(it).contentRoots }
        .firstOrNull()
        ?.toNioPathOrNull()

val PsiElement.isHybrisModule: Boolean
    get() {
        val module = ModuleUtilCore.findModuleForPsiElement(this) ?: return false
        val descriptorType = module.yExtensionDescriptor?.type
        return descriptorType == ModuleDescriptorType.PLATFORM
            || descriptorType == ModuleDescriptorType.EXT
    }

val PsiFile.module
    get() = this.virtualFile
        ?.let { ModuleUtilCore.findModuleForFile(it, this.project) }

fun VirtualFileUrlManager.fromPath(path: Path) = path
    .takeIf { it.directoryExists }
    ?.let { this.fromPath(path.normalize().pathString) }

fun VirtualFileUrlManager.fromJar(path: Path) = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
    ?.let { JarFileSystem.getInstance().getJarRootForLocalFile(it) }
    ?.let { this.getOrCreateFromUrl(it.url) }

fun Project.yModule(extensionName: String): Module? {
    val moduleMapping = this.ySettings.module2extensionMapping

    return ModuleManager.getInstance(this)
        .modules
        .firstOrNull { extensionName == it.yExtensionName(moduleMapping) }
}

fun Project.yModuleEntity(extensionName: String): ModuleEntity? {
    val moduleMapping = this.ySettings.module2extensionMapping

    return WorkspaceModel.getInstance(this)
        .currentSnapshot.entities<ModuleEntity>()
        .firstOrNull { extensionName == it.yExtensionName(moduleMapping) }
}

val ModuleEntity.contentRoot
    get() = this.contentRoots
        .firstOrNull()
        ?.url?.virtualFile

val ModuleEntity.contentRootPath
    get() = this.contentRoot
        ?.toNioPathOrNull()

fun ModuleEntity.yExtensionName(moduleMapping: Map<String, String>) = moduleMapping[this.name]

fun ModuleEntity.yExtensionDescriptor(snapshot: EntityStorage) = this.findModule(snapshot)
    ?.yExtensionDescriptor
