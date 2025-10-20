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

package sap.commerce.toolset.gradle.descriptor

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorProvider
import java.io.File

class GradleKtsModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
) : GradleModuleDescriptor(
    moduleRootDirectory,
    rootProjectDescriptor,
    File(moduleRootDirectory, HybrisConstants.GRADLE_BUILD_KTS)
) {

    class Provider : ModuleDescriptorProvider {

        override fun isApplicable(project: Project?, moduleRootDirectory: File): Boolean {
            if (moduleRootDirectory.absolutePath.contains(HybrisConstants.PLATFORM_MODULE_PREFIX)) return false

            return File(moduleRootDirectory, HybrisConstants.GRADLE_SETTINGS_KTS).isFile
                ||
                File(moduleRootDirectory, HybrisConstants.GRADLE_BUILD_KTS).isFile
        }

        override fun create(
            moduleRootDirectory: File,
            rootProjectDescriptor: HybrisProjectDescriptor
        ) = GradleKtsModuleDescriptor(moduleRootDirectory, rootProjectDescriptor)

    }

}