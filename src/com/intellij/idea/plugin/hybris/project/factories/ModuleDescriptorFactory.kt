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
package com.intellij.idea.plugin.hybris.project.factories

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ObjectFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.application
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import org.jetbrains.idea.eclipse.EclipseProjectFinder
import java.io.File
import java.io.IOException

object ModuleDescriptorFactory {

    private val LOG = Logger.getInstance(ModuleDescriptorFactory::class.java)

    @Throws(HybrisConfigurationException::class)
    fun createDescriptor(file: File, rootProjectDescriptor: HybrisProjectDescriptor): ModuleDescriptor {
        val hybrisProjectService = application.getService(HybrisProjectService::class.java)
        val resolvedFile = try {
            file.canonicalFile
        } catch (e: IOException) {
            throw HybrisConfigurationException(e)
        }
        validateModuleDirectory(resolvedFile)

        val originalPath = file.absolutePath
        val newPath = resolvedFile.absolutePath
        val path = if (originalPath != newPath) {
            "$originalPath($newPath)"
        } else {
            originalPath
        }

        return when {
            hybrisProjectService.isConfigModule(resolvedFile) -> {
                LOG.info("Creating Config module for $path")
                ConfigModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isCCv2Module(resolvedFile) -> {
                LOG.info("Creating CCv2 module for $path")
                CCv2ModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isPlatformModule(resolvedFile) -> {
                LOG.info("Creating Platform module for $path")
                PlatformModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isCoreExtModule(resolvedFile) -> {
                LOG.info("Creating Core EXT module for $path")
                YCoreExtModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile))
            }

            hybrisProjectService.isPlatformExtModule(resolvedFile) -> {
                LOG.info("Creating Platform EXT module for $path")
                with(YPlatformExtModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile))) {
                    SubModuleDescriptorFactory.buildAll(this)
                        .forEach { this.addSubModule(it) }
                    this
                }
            }

            hybrisProjectService.isOutOfTheBoxModule(resolvedFile, rootProjectDescriptor) -> {
                LOG.info("Creating OOTB module for $path")
                with(YOotbRegularModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile))) {
                    SubModuleDescriptorFactory.buildAll(this)
                        .forEach { this.addSubModule(it) }
                    this
                }
            }

            hybrisProjectService.isHybrisModule(resolvedFile) -> {
                LOG.info("Creating Custom hybris module for $path")
                with(YCustomRegularModuleDescriptor(resolvedFile, rootProjectDescriptor, getExtensionInfo(resolvedFile))) {
                    SubModuleDescriptorFactory.buildAll(this)
                        .forEach { this.addSubModule(it) }
                    this
                }

            }

            hybrisProjectService.isGradleModule(resolvedFile) -> {
                LOG.info("Creating gradle module for $path")
                GradleModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isGradleKtsModule(resolvedFile) -> {
                LOG.info("Creating gradle kts module for $path")
                GradleKtsModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isMavenModule(resolvedFile) -> {
                LOG.info("Creating maven module for $path")
                MavenModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            hybrisProjectService.isAngularModule(resolvedFile) -> {
                LOG.info("Creating Angular module for $path")
                AngularModuleDescriptor(resolvedFile, rootProjectDescriptor)
            }

            else -> {
                LOG.info("Creating eclipse module for $path")
                EclipseModuleDescriptor(resolvedFile, rootProjectDescriptor, getEclipseModuleDescriptorName(resolvedFile))
            }
        }
    }

    @Throws(HybrisConfigurationException::class)
    fun createRootDescriptor(
        moduleRootDirectory: File,
        rootProjectDescriptor: HybrisProjectDescriptor,
        name: String
    ): RootModuleDescriptor {
        validateModuleDirectory(moduleRootDirectory)

        return RootModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, name)
    }

    @Throws(HybrisConfigurationException::class)
    fun createConfigDescriptor(
        moduleRootDirectory: File,
        rootProjectDescriptor: HybrisProjectDescriptor,
        name: String
    ): ConfigModuleDescriptor {
        validateModuleDirectory(moduleRootDirectory)

        return ConfigModuleDescriptor(
            moduleRootDirectory,
            rootProjectDescriptor, name
        )
    }

    private fun validateModuleDirectory(resolvedFile: File) {
        if (!resolvedFile.isDirectory) {
            throw HybrisConfigurationException("Can not find module directory using path: $resolvedFile")
        }
    }

    private fun getEclipseModuleDescriptorName(moduleRootDirectory: File) = EclipseProjectFinder.findProjectName(moduleRootDirectory.absolutePath)
        ?.trim { it <= ' ' }
        ?.takeIf { it.isNotBlank() }
        ?: moduleRootDirectory.name

    @Throws(HybrisConfigurationException::class)
    private fun getExtensionInfo(moduleRootDirectory: File): ExtensionInfo {
        val hybrisProjectFile = File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML)
        val extensionInfo = unmarshalExtensionInfo(hybrisProjectFile)
        if (null == extensionInfo.extension || extensionInfo.extension.name.isBlank()) {
            throw HybrisConfigurationException("Can not find module name using path: $moduleRootDirectory")
        }
        return extensionInfo
    }

    @Throws(HybrisConfigurationException::class)
    private fun unmarshalExtensionInfo(hybrisProjectFile: File): ExtensionInfo {
        return try {
            JAXBContext.newInstance(
                "com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo",
                ObjectFactory::class.java.classLoader
            )
                .createUnmarshaller()
                .unmarshal(hybrisProjectFile) as ExtensionInfo
        } catch (e: JAXBException) {
            LOG.error("Can not unmarshal " + hybrisProjectFile.absolutePath, e)
            throw HybrisConfigurationException("Can not unmarshal $hybrisProjectFile")
        }
    }
}
