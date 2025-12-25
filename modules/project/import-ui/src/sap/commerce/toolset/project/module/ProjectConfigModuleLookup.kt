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

package sap.commerce.toolset.project.module

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.application
import com.intellij.util.asSafely
import org.apache.commons.io.FilenameUtils
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.factories.ModuleDescriptorFactory
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class ProjectConfigModuleLookup {

    @Throws(HybrisConfigurationException::class)
    fun getConfigModuleDescriptor(importContext: ProjectImportContext.Mutable) = find(importContext)
        ?: throw HybrisConfigurationException(
            """
                The ‘config’ module hasn’t been detected, which will affect the following functionality:
                
                 · module auto-selection
                 · building modules in IDE
                 · resolving properties
                 """.trimIndent()
        )

    private fun find(importContext: ProjectImportContext.Mutable): ConfigModuleDescriptor? {
        val foundConfigModules = importContext.foundModules
            .filterIsInstance<ConfigModuleDescriptor>()
        val platformHybrisModuleDescriptor = importContext.foundModules
            .filterIsInstance<PlatformModuleDescriptor>()
            .firstOrNull() ?: return null

        if (foundConfigModules.size == 1) return foundConfigModules[0]

        val configDir: File?
        val externalConfigDirectory = importContext.externalConfigDirectory
        if (externalConfigDirectory != null) {
            configDir = externalConfigDirectory
            if (!configDir.isDirectory()) return null
        } else {
            configDir = getExpectedConfigDir(platformHybrisModuleDescriptor)
            if (configDir == null || !configDir.isDirectory()) {
                return if (foundConfigModules.size == 1) foundConfigModules[0]
                else null
            }
        }
        val configHybrisModuleDescriptor = foundConfigModules
            .firstOrNull { FileUtil.filesEqual(it.moduleRootDirectory, configDir) }
        if (configHybrisModuleDescriptor != null) return configHybrisModuleDescriptor

        if (!ProjectModuleResolver.getInstance().isConfigModule(configDir)) return null

        return try {
            val configHybrisModuleDescriptor = ModuleDescriptorFactory.createConfigDescriptor(
                configDir, configDir.getName()
            )
            thisLogger().info("Creating Overridden Config module in local.properties for ${configDir.absolutePath}")

            importContext.addFoundModule(configHybrisModuleDescriptor)

            configHybrisModuleDescriptor
        } catch (e: HybrisConfigurationException) {
            thisLogger().warn(e)
            null
        }
    }

    private fun getExpectedConfigDir(platformModuleDescriptor: PlatformModuleDescriptor): File? {
        val expectedConfigDir = Path(platformModuleDescriptor.moduleRootDirectory.absolutePath, HybrisConstants.CONFIG_RELATIVE_PATH)
        if (!expectedConfigDir.isDirectory()) return null

        val propertiesFile = expectedConfigDir.resolve(ProjectConstants.File.LOCAL_PROPERTIES)
        if (!propertiesFile.exists()) return expectedConfigDir.toFile()

        val properties = Properties()
        try {
            FileReader(propertiesFile.toFile()).use { fr ->
                properties.load(fr)
            }
        } catch (e: IOException) {
            thisLogger().warn(e)
            return expectedConfigDir.toFile()
        }

        var hybrisConfig = properties[HybrisConstants.ENV_HYBRIS_CONFIG_DIR]
            ?.asSafely<String>()
            ?: return expectedConfigDir.toFile()

        hybrisConfig = hybrisConfig.replace(
            HybrisConstants.PLATFORM_HOME_PLACEHOLDER,
            platformModuleDescriptor.moduleRootDirectory.path
        )
        hybrisConfig = FilenameUtils.separatorsToSystem(hybrisConfig)

        val hybrisConfigDir = Path(hybrisConfig)
        if (hybrisConfigDir.isDirectory()) return hybrisConfigDir.toFile()

        return expectedConfigDir.toFile()
    }

    companion object {
        fun getInstance(): ProjectConfigModuleLookup = application.service()
    }

}