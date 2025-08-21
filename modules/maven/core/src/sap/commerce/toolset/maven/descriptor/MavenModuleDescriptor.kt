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

package sap.commerce.toolset.maven.descriptor

import org.jetbrains.idea.maven.model.MavenConstants
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorProvider
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor
import java.io.File

class MavenModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    override val descriptorType: ModuleDescriptorType = ModuleDescriptorType.MAVEN
) : ExternalModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, moduleRootDirectory.name) {

    class Provider : ModuleDescriptorProvider {
        override fun isApplicable(moduleRootDirectory: File): Boolean {
            if (moduleRootDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) return false

            return File(moduleRootDirectory, MavenConstants.POM_XML).isFile()
        }

        override fun create(
            moduleRootDirectory: File,
            rootProjectDescriptor: HybrisProjectDescriptor
        ) = MavenModuleDescriptor(moduleRootDirectory, rootProjectDescriptor)
    }
}
