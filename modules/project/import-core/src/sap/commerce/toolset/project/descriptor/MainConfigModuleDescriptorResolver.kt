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

package sap.commerce.toolset.project.descriptor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.provider.ModuleDescriptorFactory
import sap.commerce.toolset.project.module.ModuleRootResolver
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.pathString

@Service
class MainConfigModuleDescriptorResolver {

    @Throws(HybrisConfigurationException::class)
    fun resolve(importContext: ProjectImportContext.Mutable) = find(importContext)
        ?.apply {
            importStatus = ModuleDescriptorImportStatus.MANDATORY
            isMainConfig = true
        }
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

        val configDir: Path?
        val externalConfigDirectory = importContext.externalConfigDirectory
        if (externalConfigDirectory != null) {
            configDir = externalConfigDirectory
            if (!configDir.directoryExists) return null
        } else {
            configDir = getExpectedConfigDir(platformHybrisModuleDescriptor)
            if (configDir == null || !configDir.directoryExists) {
                return if (foundConfigModules.size == 1) foundConfigModules[0]
                else null
            }
        }
        val configHybrisModuleDescriptor = foundConfigModules
            .firstOrNull { FileUtil.pathsEqual(it.moduleRootPath.pathString, configDir.pathString) }
        if (configHybrisModuleDescriptor != null) return configHybrisModuleDescriptor

        return ModuleRootResolver.EP.extensionList
            .find { it.isApplicable(importContext, importContext.rootDirectory, configDir) }
            ?.resolve(configDir)
            ?.moduleRoot
            ?.takeIf { it.type == ModuleDescriptorType.CONFIG }
            ?.let {
                thisLogger().info("Creating Overridden Config module in local.properties for: $configDir")
                ModuleDescriptorFactory.getInstance().createDescriptor(importContext, it)
            }
            ?.let { it as? ConfigModuleDescriptor }
            ?.also { importContext.addModule(it) }
    }

    private fun getExpectedConfigDir(platformModuleDescriptor: PlatformModuleDescriptor): Path? {
        val expectedConfigDir = platformModuleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.RELATIVE_CONFIG)
            .takeIf { it.directoryExists } ?: return null
        val propertiesFile = expectedConfigDir.resolve(ProjectConstants.File.LOCAL_PROPERTIES)
            .takeIf { it.fileExists } ?: return expectedConfigDir

        val properties = Properties()
        try {
            propertiesFile.inputStream().use { properties.load(it) }
        } catch (e: IOException) {
            thisLogger().warn(e)
            return expectedConfigDir
        }

        var hybrisConfig = properties[HybrisConstants.ENV_HYBRIS_CONFIG_DIR]
            ?.asSafely<String>()
            ?: return expectedConfigDir

        hybrisConfig = hybrisConfig.replace(
            HybrisConstants.PLATFORM_HOME_PLACEHOLDER,
            platformModuleDescriptor.moduleRootPath.pathString
        )
        hybrisConfig = FileUtil.normalize(hybrisConfig)

        return Path(hybrisConfig)
            .takeIf { it.directoryExists }
            ?: expectedConfigDir
    }

    companion object {
        fun getInstance(): MainConfigModuleDescriptorResolver = application.service()
    }

}