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
package sap.commerce.toolset.project.descriptor.provider

import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.vfs.VirtualFileSystemService
import java.io.File

class YOotbRegularModuleDescriptorProvider : YModuleDescriptorProvider() {

    override fun isApplicable(context: ModuleDescriptorProviderContext): Boolean {
        val moduleRootDirectory = context.moduleRootDirectory
        val externalExtensionsDirectory = context.externalExtensionsDirectory
        if (externalExtensionsDirectory != null) {
            if (VirtualFileSystemService.getInstance().fileContainsAnother(externalExtensionsDirectory, moduleRootDirectory)) {
                // this will override bin/ext-* naming convention.
                return false
            }
        }

        return (moduleRootDirectory.absolutePath.contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX)
            || moduleRootDirectory.absolutePath.contains(HybrisConstants.PLATFORM_OOTB_MODULE_PREFIX_2019))
            && File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML).isFile()
    }

    override fun create(moduleRootDirectory: File) = YOotbRegularModuleDescriptor(
        moduleRootDirectory,
        getExtensionInfo(moduleRootDirectory)
    ).apply {
        SubModuleDescriptorFactory.buildAll(this)
            .forEach { this.addSubModule(it) }
    }
}
