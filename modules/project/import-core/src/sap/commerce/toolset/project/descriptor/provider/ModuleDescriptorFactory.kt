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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.impl.ConfigModuleDescriptorImpl
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor
import java.io.File
import java.io.IOException

@Service
class ModuleDescriptorFactory {

    @Throws(HybrisConfigurationException::class)
    fun createDescriptor(file: File, importContext: ProjectImportContext.Mutable): ModuleDescriptor {
        val resolvedFile = try {
            file.canonicalFile
        } catch (e: IOException) {
            throw HybrisConfigurationException(e)
        }

        validateModuleDirectory(resolvedFile)

        val originalPath = file.absolutePath
        val newPath = resolvedFile.absolutePath
        val path = if (originalPath != newPath) "$originalPath($newPath)"
        else originalPath

        val context = ModuleDescriptorProviderContext(
            moduleRootDirectory = resolvedFile,
            project = importContext.project,
            externalExtensionsDirectory = importContext.externalExtensionsDirectory,
        )

        return ModuleDescriptorProvider.EP.extensionList
            .firstOrNull { it.isApplicable(context) }
            ?.let { provider ->
                thisLogger().info("Creating '${provider.javaClass.name}' module for $path")
                provider.create(resolvedFile)
            }
            ?: throw HybrisConfigurationException("Could not find suitable module descriptor provider for $path")
    }

    @Throws(HybrisConfigurationException::class)
    fun createRootDescriptor(
        moduleRootDirectory: File,
        name: String
    ): ExternalModuleDescriptor {
        validateModuleDirectory(moduleRootDirectory)

        // TODO: introduce new RootModuleDescriptor
        return ExternalModuleDescriptor(moduleRootDirectory, name, ModuleDescriptorType.NONE)
    }

    // TODO: why not via provider?
    @Throws(HybrisConfigurationException::class)
    fun createConfigDescriptor(
        moduleRootDirectory: File,
        name: String
    ): ConfigModuleDescriptor {
        validateModuleDirectory(moduleRootDirectory)

        return ConfigModuleDescriptorImpl(moduleRootDirectory, name)
    }

    private fun validateModuleDirectory(moduleRootDirectory: File) {
        if (!moduleRootDirectory.isDirectory) {
            throw HybrisConfigurationException("Can not find module directory using path: $moduleRootDirectory")
        }
    }

    companion object {
        fun getInstance(): ModuleDescriptorFactory = application.service()
    }
}