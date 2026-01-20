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
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.runConfigurations.LocalSapCXConfigurationType

class LocalServerRunConfigurationWhenSmartConfigurator : ProjectImportWhenSmartConfigurator {

    override val name: String
        get() = "Run Configurations - Local SAP CX"

    override suspend fun configure(context: ProjectPostImportContext) {
        val runManager = RunManager.getInstance(context.project)
        val configurationName = i18n("hybris.project.run.configuration.localserver")

        if (runManager.findConfigurationByName(configurationName) != null) return

        val confType = ConfigurationTypeUtil.findConfigurationType(LocalSapCXConfigurationType::class.java)
        val configurationFactory = confType.configurationFactories.first()
        val runner = runManager.createConfiguration(
            configurationName,
            configurationFactory
        )

        runner.isActivateToolWindowBeforeRun = true
        runner.storeInDotIdeaFolder()

        runManager.addConfiguration(runner)
    }
}