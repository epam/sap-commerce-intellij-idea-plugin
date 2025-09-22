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
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.runConfigurations.createRunConfiguration

class RemoteDebugRunConfigurationConfigurator : ProjectPostImportConfigurator {
    private val regexSpace = " ".toRegex()
    private val regexComma = ",".toRegex()
    private val regexEquals = "=".toRegex()

    override val name: String
        get() = "Run Configurations - Debug"

    override suspend fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val runManager = RunManager.getInstance(project)
        val configurationName = i18n("hybris.project.run.configuration.remote.debug")

        if (hybrisProjectDescriptor.refresh && runManager.findConfigurationByName(configurationName) != null) return

        val debugPort = findPortProperty(project) ?: HybrisConstants.DEBUG_PORT

        createRunConfiguration(
            runManager,
            RemoteConfigurationType::class.java,
            configurationName
        ) {
            val remoteConfiguration = it.configuration as RemoteConfiguration
            remoteConfiguration.PORT = debugPort
            remoteConfiguration.isAllowRunningInParallel = false
        }
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