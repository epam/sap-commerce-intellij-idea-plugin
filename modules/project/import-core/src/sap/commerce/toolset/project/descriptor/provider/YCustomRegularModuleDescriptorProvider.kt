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
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.EiModelAccess
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor
import java.io.File

class YCustomRegularModuleDescriptorProvider : YModuleDescriptorProvider() {

    override fun isApplicable(context: ModuleDescriptorProviderContext) = File(
        context.moduleRootDirectory,
        HybrisConstants.EXTENSION_INFO_XML
    ).isFile

    @Throws(HybrisConfigurationException::class)
    override fun create(moduleRootDirectory: File): YCustomRegularModuleDescriptor {
        val extensionInfo = EiModelAccess.getInfo(moduleRootDirectory)
            ?: throw HybrisConfigurationException("Cannot unmarshall extensioninfo.xml for $moduleRootDirectory")

        return YCustomRegularModuleDescriptor(moduleRootDirectory, extensionInfo).apply {
            SubModuleDescriptorFactory.buildAll(this)
                .forEach { this.addSubModule(it) }
        }
    }
}
