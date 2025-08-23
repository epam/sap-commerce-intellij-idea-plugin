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

package sap.commerce.toolset.project.configurator

import com.intellij.execution.RunManager
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.remote.RemoteConfigurationType
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.runConfigurations.createRunConfiguration
import java.io.File

class RemoteDebugRunConfigurationConfigurator : ProjectImportConfigurator, ProjectPostImportConfigurator{
    private val regexSpace = " ".toRegex()
    private val regexComma = ",".toRegex()
    private val regexEquals = "=".toRegex()

    override fun configure(
        project: Project,
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        cache: ConfiguratorCache
    ) {
        indicator.text = i18n("hybris.project.import.runconfiguration")
        val runManager = RunManager.getInstance(project)

        createRunConfiguration(
            runManager,
            RemoteConfigurationType::class.java,
            i18n("hybris.project.run.configuration.remote.debug")
        ) {
            val remoteConfiguration = it.configuration as RemoteConfiguration
            remoteConfiguration.PORT = getDebugPort(hybrisProjectDescriptor, cache)
            remoteConfiguration.isAllowRunningInParallel = false
        }
    }

    override fun postImport(
        project: Project,
        refresh: Boolean,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: List<ModuleDescriptor>
    ): List<() -> Unit> {
        if (refresh) return emptyList()

        return listOf {
            val debugConfiguration = i18n("hybris.project.run.configuration.remote.debug")
            val runManager = RunManager.getInstance(project)

            runManager.findConfigurationByName(debugConfiguration)
                ?.let { runManager.selectedConfiguration = it }
        }
    }

    private fun getDebugPort(hybrisProjectDescriptor: HybrisProjectDescriptor, cache: ConfiguratorCache) = hybrisProjectDescriptor.configHybrisModuleDescriptor
        ?.let { findPortProperty(it, HybrisConstants.LOCAL_PROPERTIES_FILE, cache) }
        ?: hybrisProjectDescriptor
            .foundModules
            .firstNotNullOfOrNull { it as? PlatformModuleDescriptor }
            ?.let { findPortProperty(it, HybrisConstants.PROJECT_PROPERTIES_FILE, cache) }
        ?: HybrisConstants.DEBUG_PORT


    private fun findPortProperty(moduleDescriptor: ModuleDescriptor, fileName: String, cache: ConfiguratorCache) = cache.findPropertyInFile(
        File(moduleDescriptor.moduleRootDirectory, fileName),
        HybrisConstants.TOMCAT_JAVA_DEBUG_OPTIONS
    )
        ?.split(regexSpace)
        ?.dropLastWhile { it.isEmpty() }
        ?.firstOrNull { it.startsWith(HybrisConstants.X_RUNJDWP_TRANSPORT) }
        ?.split(regexComma)
        ?.dropLastWhile { it.isEmpty() }
        ?.firstOrNull { it.startsWith(HybrisConstants.ADDRESS) }
        ?.split(regexEquals)
        ?.dropLastWhile { it.isEmpty() }
        ?.getOrNull(1)
}