/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ENV_HYBRIS_DATA_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_DATA_DIRECTORY
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_STANDALONE_JDKMODULESEXPORTS
import com.intellij.idea.plugin.hybris.project.utils.HybrisRootUtil
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.util.*
import java.util.regex.Pattern

private val PATTERN = Pattern.compile("\"")

class HybrisJUnitExtension : RunConfigurationExtension() {

    override fun isApplicableFor(configuration: RunConfigurationBase<*>) = if (configuration !is JUnitConfiguration) false
    else HybrisProjectSettingsComponent.getInstance(configuration.getProject()).isHybrisProject()

    override fun <T : RunConfigurationBase<*>?> updateJavaParameters(configuration: T & Any, params: JavaParameters, runnerSettings: RunnerSettings?) {
        if (runnerSettings != null || !isApplicableFor(configuration)) return

        val project = configuration.project
        val vmParameters = params.vmParametersList

        addVmParameterIfNotExist(vmParameters, "-ea")

        if (vmParameters.parameters.none { it.startsWith("-Dplatformhome=") }) {
            HybrisRootUtil.findPlatformRootDirectory(project)
                ?.path
                ?.let { vmParameters.add("-Dplatformhome=$it") }
        }

        if (!params.env.containsKey(ENV_HYBRIS_DATA_DIR)) {
            val settings = HybrisProjectSettingsComponent.getInstance(project).state
            val hybrisDataDirPath = FileUtil.toCanonicalPath(
                "${project.basePath}/${settings.hybrisDirectory}/$HYBRIS_DATA_DIRECTORY"
            )

            if (File(hybrisDataDirPath).exists()) {
                params.addEnv(ENV_HYBRIS_DATA_DIR, hybrisDataDirPath)
            }
        }

        PropertyService.getInstance(project)
            ?.findProperty(PROPERTY_STANDALONE_JDKMODULESEXPORTS)
            ?.let { property -> StringTokenizer(property.trim { it <= ' ' }) }
            ?.let {
                while (it.hasMoreTokens()) {
                    val newParam = PATTERN.matcher(it.nextToken()).replaceAll("")
                    addVmParameterIfNotExist(vmParameters, newParam)
                }
            }
    }

    private fun addVmParameterIfNotExist(vmParameters: ParametersList, newParam: String) {
        if (!vmParameters.hasParameter(newParam)) {
            vmParameters.add(newParam)
        }
    }

}
