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

package sap.commerce.toolset.meta.util

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacetConstants
import java.io.File
import java.io.IOException

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
    val filePath = try {
        itemsFile.canonicalPath.normalize()
    } catch (e: IOException) {
        itemsFile.absolutePath.normalize()
    }

    return when {
        filePath.contains(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX) -> ModuleDescriptorType.OOTB
        filePath.contains(HybrisConstants.HYBRIS_OOTB_MODULE_PREFIX_2019) -> ModuleDescriptorType.OOTB
        filePath.contains(HybrisConstants.PLATFORM_EXT_MODULE_PREFIX) -> ModuleDescriptorType.EXT
        else -> ModuleDescriptorType.CUSTOM
    }
}

private fun String.normalize() = replace(File.separatorChar, '/')
