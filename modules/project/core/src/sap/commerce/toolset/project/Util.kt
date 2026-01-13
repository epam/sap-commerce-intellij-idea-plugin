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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import sap.commerce.toolset.project.context.ProjectImportState
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path
import kotlin.io.path.pathString

private val importInProgress = Key<ProjectImportState>("sap.commerce.toolset.project.import.state")

var Project.importState: ProjectImportState
    get() = this.getUserData(importInProgress) ?: ProjectImportState.IMPORTED
    set(value) {
        this.putUserData(importInProgress, value)
    }

fun Module.yExtensionName(): String = YFacet.get(this)
    ?.configuration
    ?.state
    ?.name
    ?: this.name.substringAfterLast(".")

fun Module.root(): Path? = this
    .let { ModuleRootManager.getInstance(it).contentRoots }
    .firstOrNull()
    ?.toNioPathOrNull()

fun findPlatformRootDirectory(project: Project): VirtualFile? = ModuleManager.getInstance(project)
    .modules
    .firstOrNull { YFacet.getState(it)?.type == ModuleDescriptorType.PLATFORM }
    ?.let { ModuleRootManager.getInstance(it) }
    ?.contentRoots
    ?.firstOrNull { it.findChild(ProjectConstants.File.EXTENSIONS_XML) != null }

val PsiElement.isHybrisModule: Boolean
    get() {
        val module = ModuleUtilCore.findModuleForPsiElement(this) ?: return false
        val descriptorType = YFacet.getState(module)?.type
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
