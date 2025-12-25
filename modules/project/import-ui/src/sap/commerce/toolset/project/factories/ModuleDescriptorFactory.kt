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
package sap.commerce.toolset.project.factories

import com.intellij.openapi.diagnostic.thisLogger
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.impl.ConfigModuleDescriptorImpl
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor
import sap.commerce.toolset.project.descriptor.provider.ModuleDescriptorProvider
import java.io.File
import java.io.IOException

// TODO: move to core
object ModuleDescriptorFactory {

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

//    @Throws(HybrisConfigurationException::class)
//    private fun getExtensionInfo(moduleRootDirectory: File): ExtensionInfo {
//        val hybrisProjectFile = File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML)
//        val extensionInfo = unmarshalExtensionInfo(hybrisProjectFile)
//        if (null == extensionInfo.extension || extensionInfo.extension.name.isBlank()) {
//            throw HybrisConfigurationException("Can not find module name using path: $moduleRootDirectory")
//        }
//        return extensionInfo
//    }

//    @Throws(HybrisConfigurationException::class)
//    private fun unmarshalExtensionInfo(hybrisProjectFile: File): ExtensionInfo {
//        return try {
//            JAXBContext.newInstance(
//                ObjectFactory::class.java.packageName,
//                ObjectFactory::class.java.classLoader
//            )
//                .createUnmarshaller()
//                .unmarshal(hybrisProjectFile) as ExtensionInfo
//        } catch (e: JAXBException) {
//            LOG.error("Can not unmarshal " + hybrisProjectFile.absolutePath, e)
//            throw HybrisConfigurationException("Can not unmarshal $hybrisProjectFile")
//        }
//    }
}
