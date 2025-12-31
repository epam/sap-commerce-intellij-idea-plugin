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

import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.context.ProjectImportContext
import java.nio.file.Path

interface ModuleDescriptor : Comparable<ModuleDescriptor> {
    val name: String
    var groupNames: Array<String>
    val moduleRootPath: Path
    var importStatus: ModuleDescriptorImportStatus
    val type: ModuleDescriptorType
    var readonly: Boolean

    val extensionDescriptor: ExtensionDescriptor
    fun ideaModuleName(): String = (if (groupNames.isEmpty()) "" else groupNames.joinToString(separator = ".", postfix = ".")) + name
    fun groupName(importContext: ProjectImportContext): Array<String>? = null
    fun isPreselected(): Boolean
    fun ideaModuleFile(importContext: ProjectImportContext): Path
    fun getRelativePath(rootDirectory: Path): String

    // TODO: all below methods are YModuleDescriptor specific
    fun getRequiredExtensionNames(): Set<String>
    fun addRequiredExtensionNames(extensions: Collection<YModuleDescriptor>): Boolean
    fun computeRequiredExtensionNames(moduleDescriptors: Map<String, ModuleDescriptor>)
    fun getAllDependencies(): Set<ModuleDescriptor>
    fun getDirectDependencies(): Set<ModuleDescriptor>
    fun addDirectDependencies(dependencies: Collection<ModuleDescriptor>): Boolean
}