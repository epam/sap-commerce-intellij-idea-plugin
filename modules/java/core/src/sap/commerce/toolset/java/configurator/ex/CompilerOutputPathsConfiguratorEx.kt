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
package sap.commerce.toolset.java.configurator.ex

import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtilCore
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import java.io.File

internal object CompilerOutputPathsConfiguratorEx {

    fun configure(
        modifiableRootModel: ModifiableRootModel,
        moduleDescriptor: ModuleDescriptor
    ) {
        val useFakeOutputPathForCustomExtensions = moduleDescriptor.rootProjectDescriptor.isUseFakeOutputPathForCustomExtensions
        val outputDirectory = if (moduleDescriptor.descriptorType == ModuleDescriptorType.CUSTOM && !useFakeOutputPathForCustomExtensions)
            File(moduleDescriptor.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_OUTPUT_PATH)
        else
            File(moduleDescriptor.moduleRootDirectory, HybrisConstants.JAVA_COMPILER_FAKE_OUTPUT_PATH)

        with (modifiableRootModel.getModuleExtension(CompilerModuleExtension::class.java)) {
            setCompilerOutputPath(VfsUtilCore.pathToUrl(outputDirectory.absolutePath))
            setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(outputDirectory.absolutePath))

            isExcludeOutput = true
            inheritCompilerOutputPath(false)
        }
    }
}
