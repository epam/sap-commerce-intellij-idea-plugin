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
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.facet.YFacetConstants
import java.io.File
import java.nio.file.Path

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
    .firstOrNull { YFacetConstants.getModuleSettings(it).type == ModuleDescriptorType.PLATFORM }
    ?.let { ModuleRootManager.getInstance(it) }
    ?.contentRoots
    ?.firstOrNull { it.findChild(HybrisConstants.EXTENSIONS_XML) != null }

fun isHybrisModule(psi: PsiElement): Boolean {
    val module = ModuleUtilCore.findModuleForPsiElement(psi) ?: return false
    val descriptorType = YFacetConstants.getModuleSettings(module).type
    return descriptorType == ModuleDescriptorType.PLATFORM
        || descriptorType == ModuleDescriptorType.EXT
}

val PsiFile.module
    get() = this.virtualFile
        ?.let { ModuleUtilCore.findModuleForFile(it, this.project) }

fun VirtualFile.isCustomExtensionFile(project: Project): Boolean {
    val descriptorType = ModuleUtilCore.findModuleForFile(this, project)
        ?.let { YFacetConstants.getModuleSettings(it).type }
        ?: return false

    return when (descriptorType) {
        ModuleDescriptorType.NONE -> if (project.isHybrisProject) estimateIsCustomExtension(this) == ModuleDescriptorType.CUSTOM
        else false

        else -> descriptorType == ModuleDescriptorType.CUSTOM
    }
}

private fun estimateIsCustomExtension(file: VirtualFile): ModuleDescriptorType {
    val itemsFile = VfsUtilCore.virtualToIoFile(file)
    val filePath = normalize(itemsFile.absolutePath)

    return when {
        filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX)) -> ModuleDescriptorType.OOTB
        filePath.contains(normalize(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019)) -> ModuleDescriptorType.OOTB
        filePath.contains(normalize(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX)) -> ModuleDescriptorType.EXT
        else -> ModuleDescriptorType.CUSTOM
    }
}

private fun normalize(path: String): String =
    path.replace(File.separatorChar, '/')