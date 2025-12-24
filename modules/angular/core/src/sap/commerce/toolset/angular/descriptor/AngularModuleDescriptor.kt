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

package sap.commerce.toolset.angular.descriptor

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.ModuleGroupingUtil
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus
import sap.commerce.toolset.project.descriptor.ModuleDescriptorProvider
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor
import java.io.File

class AngularModuleDescriptor(
    moduleRootDirectory: File,
    name: String = moduleRootDirectory.name,
    override val descriptorType: ModuleDescriptorType = ModuleDescriptorType.ANGULAR
) : ExternalModuleDescriptor(moduleRootDirectory, name) {

    init {
        importStatus = ModuleDescriptorImportStatus.MANDATORY
    }

    override fun isPreselected() = true
    override fun initDependencies(moduleDescriptors: Map<String, ModuleDescriptor>) = moduleDescriptors.values
        .filter { this.moduleRootDirectory.toString().startsWith(it.moduleRootDirectory.toString()) }
        .filter { this != it }
        .map { it.name }
        .take(1)
        .toSet()

    override fun groupName(importContext: ProjectImportContext): Array<String> {
        // assumption that there can be only 1 parent
        val parent = getDirectDependencies().firstOrNull()
            ?: return emptyArray()
        val parentPath = ModuleGroupingUtil.getGroupPath(importContext, parent, listOf())
        return parentPath + parent.name
    }

    class Provider : ModuleDescriptorProvider {
        override fun isApplicable(project: Project?, moduleRootDirectory: File) = Plugin.ANGULAR.isActive()
            && File(moduleRootDirectory, HybrisConstants.FILE_ANGULAR_JSON).isFile()

        override fun create(
            moduleRootDirectory: File
        ) = AngularModuleDescriptor(moduleRootDirectory)
    }
}
