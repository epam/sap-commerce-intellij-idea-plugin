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
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.descriptors.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.facet.YFacetConstants
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
