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
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.remote.RemoteConfigurationType
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.context.ProjectPostImportContext

class RemoteDebugRunConfigurationWhenSmartConfigurator : ProjectImportWhenSmartConfigurator {

    private val regexSpace by lazy { " ".toRegex() }
    private val regexComma by lazy { ",".toRegex() }
    private val regexEquals by lazy { "=".toRegex() }

    override val name: String
        get() = "Run Configurations - Debug"

    override suspend fun configure(context: ProjectPostImportContext) {
        val runManager = RunManager.getInstance(context.project)
        val configurationName = i18n("hybris.project.run.configuration.remote.debug")

        if (runManager.findConfigurationByName(configurationName) != null) return

        val confType = ConfigurationTypeUtil.findConfigurationType(RemoteConfigurationType::class.java)
        val configurationFactory = confType.configurationFactories.first()
        val debugPort = findPortProperty(context.project) ?: HybrisConstants.DEBUG_PORT
        val runner = runManager.createConfiguration(
            configurationName,
            configurationFactory
        )

        runner.configuration.asSafely<RemoteConfiguration>()
            ?.apply {
                this.PORT = debugPort
                this.isAllowRunningInParallel = false
            }

        runner.isActivateToolWindowBeforeRun = true
        runner.storeInDotIdeaFolder()

        runManager.addConfiguration(runner)
        runManager.selectedConfiguration = runner
    }

    private suspend fun findPortProperty(project: Project) = smartReadAction(project) {
        PropertyService.getInstance(project)
            .findProperty(HybrisConstants.TOMCAT_JAVA_DEBUG_OPTIONS)
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
}