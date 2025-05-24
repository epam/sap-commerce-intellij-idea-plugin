/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.remote.RemoteConfigurationType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.AbstractModuleDescriptor
import com.intellij.idea.plugin.hybris.runConfigurations.LocalSapCXConfigurationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import java.io.File

@Service(Service.Level.APP)
class RunConfigurationConfigurator {

    fun configureAfterImport(project: Project, refresh: Boolean): List<() -> Unit> {
        if (refresh) return emptyList()

        return listOf {
            val debugConfiguration = message("hybris.project.run.configuration.remote.debug")
            val runManager = RunManager.getInstance(project)

            runManager.findConfigurationByName(debugConfiguration)
                ?.let { runManager.selectedConfiguration = it }
        }
    }

    fun configure(
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        project: Project,
        cache: HybrisConfiguratorCache
    ) {
        indicator.text = message("hybris.project.import.runconfiguration")
        val runManager = RunManager.getInstance(project)

        createRunConfiguration(
            runManager,
            RemoteConfigurationType::class.java,
            message("hybris.project.run.configuration.remote.debug")
        ) {
            val remoteConfiguration = it.configuration as RemoteConfiguration
            remoteConfiguration.PORT = getDebugPort(hybrisProjectDescriptor, cache)
            remoteConfiguration.isAllowRunningInParallel = false
        }
        createRunConfiguration(
            runManager,
            LocalSapCXConfigurationType::class.java,
            message("hybris.project.run.configuration.localserver")
        )
    }

    private fun <T : ConfigurationType> createRunConfiguration(
        runManager: RunManager,
        configurationType: Class<T>,
        configurationName: String,
        configurationConsumer: (RunnerAndConfigurationSettings) -> Unit = {}
    ) {
        if (runManager.findConfigurationByName(configurationName) != null) {
            return
        }

        val confType = ConfigurationTypeUtil.findConfigurationType(configurationType)
        val configurationFactory = confType.configurationFactories.first()

        invokeLater {
            runWriteAction {
                val runner = runManager.createConfiguration(
                    configurationName,
                    configurationFactory
                )

                configurationConsumer.invoke(runner)

                runner.isActivateToolWindowBeforeRun = true
                runner.storeInDotIdeaFolder()

                runManager.addConfiguration(runner)
            }
        }
    }

    private fun getDebugPort(hybrisProjectDescriptor: HybrisProjectDescriptor, cache: HybrisConfiguratorCache) = hybrisProjectDescriptor.getConfigHybrisModuleDescriptor()
        ?.let { findPortProperty(it, HybrisConstants.LOCAL_PROPERTIES_FILE, cache) }
        ?: CommonIdeaService.getInstance().getPlatformDescriptor(hybrisProjectDescriptor)
            ?.let { findPortProperty(it, HybrisConstants.PROJECT_PROPERTIES_FILE, cache) }
        ?: HybrisConstants.DEBUG_PORT


    private fun findPortProperty(moduleDescriptor: AbstractModuleDescriptor, fileName: String, cache: HybrisConfiguratorCache) = cache.findPropertyInFile(
        File(moduleDescriptor.moduleRootDirectory, fileName),
        HybrisConstants.TOMCAT_JAVA_DEBUG_OPTIONS
    )
        ?.split(REGEX_SPACE)
        ?.dropLastWhile { it.isEmpty() }
        ?.firstOrNull { it.startsWith(HybrisConstants.X_RUNJDWP_TRANSPORT) }
        ?.split(REGEX_COMMA)
        ?.dropLastWhile { it.isEmpty() }
        ?.firstOrNull { it.startsWith(HybrisConstants.ADDRESS) }
        ?.split(REGEX_EQUALS)
        ?.dropLastWhile { it.isEmpty() }
        ?.getOrNull(1)

    companion object {
        private val REGEX_SPACE = " ".toRegex()
        private val REGEX_COMMA = ",".toRegex()
        private val REGEX_EQUALS = "=".toRegex()
    }
}
