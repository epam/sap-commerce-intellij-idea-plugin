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
package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_BUNDLED_SERVER_TYPE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_BIN_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_BOOTSTRAP_BIN_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_CONFIG_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_DATA_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_LOG_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_ROLES_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_HYBRIS_TEMP_DIR
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_PLATFORMHOME
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_STANDALONE_JAVAOPTIONS
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROPERTY_STANDALONE_JDKMODULESEXPORTS
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import java.util.*

class HybrisJUnitExtension : RunConfigurationExtension() {

    override fun isApplicableFor(configuration: RunConfigurationBase<*>) =
        if (configuration !is JUnitConfiguration) false
        else ProjectSettingsComponent.getInstance(configuration.project).isHybrisProject()

    override fun <T : RunConfigurationBase<*>?> updateJavaParameters(
        configuration: T & Any, params: JavaParameters, runnerSettings: RunnerSettings?
    ) {

        if (runnerSettings != null || !isApplicableFor(configuration)) return

        val junitConfig = (configuration as JUnitConfiguration)
        val project = configuration.project

        if (isPureUnitTest(junitConfig, project)) return

        val vmParameters = params.vmParametersList

        addVmParameterIfNotExist(vmParameters, "-ea")

        addPlatformHome(junitConfig, vmParameters, project)
        addJavaRunProperties(project, vmParameters)
        addHybrisEnvProperties(project, vmParameters)
    }

    private fun isPureUnitTest(configuration: JUnitConfiguration, project: Project): Boolean {
        val runClass = configuration.runClass ?: return false
        val psiClass = JavaPsiFacade.getInstance(project)
            .findClass(runClass, GlobalSearchScope.allScope(project)) ?: return false

        return hasSpecificAnnotation(psiClass, "de.hybris.bootstrap.annotations.UnitTest")
    }

    private fun hasSpecificAnnotation(psiClass: PsiClass, annotationFQN: String): Boolean {
        return psiClass.annotations.any { it.qualifiedName == annotationFQN }
    }

    private fun addPlatformHome(junitConfig: JUnitConfiguration, vmParameters: ParametersList, project: Project) {
        val platforhomePrefix = "-D$PROPERTY_PLATFORMHOME"
        if (vmParameters.parameters.none { it.startsWith(platforhomePrefix) }) {
            PropertyService.getInstance(project)
                ?.getPlatformHome()
                ?.let { vmParameters.add("$platforhomePrefix=$it") }
        }
    }

    private fun addJavaRunProperties(project: Project, vmParameters: ParametersList) {
        PropertyService.getInstance(project)
            ?.findProperty(PROPERTY_STANDALONE_JAVAOPTIONS)
            ?.let { property -> StringTokenizer(property.trim { it <= ' ' }) }
            ?.let {
                while (it.hasMoreTokens()) {
                    val newParam = sanitizeParameter(it.nextToken())
                    addVmParameterIfNotExist(vmParameters, newParam)
                }
            }

        PropertyService.getInstance(project)
            ?.findProperty(PROPERTY_STANDALONE_JDKMODULESEXPORTS)
            ?.let { property -> StringTokenizer(property.trim { it <= ' ' }) }
            ?.let {
                while (it.hasMoreTokens()) {
                    val newParam = sanitizeParameter(it.nextToken())
                    addVmParameterIfNotExist(vmParameters, newParam)
                }
            }

        PropertyService.getInstance(project)
            ?.findProperty(PROPERTY_BUNDLED_SERVER_TYPE)
            ?.let {
                addVmParameterIfNotExist(vmParameters, "-D$PROPERTY_BUNDLED_SERVER_TYPE=\"$it\"")
            }
    }

    private fun sanitizeParameter(param: String): String {
        return param.replace("\"", "")
    }

    private fun addHybrisEnvProperties(project: Project, vmParameters: ParametersList) {
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_BIN_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_TEMP_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_ROLES_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_LOG_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_BOOTSTRAP_BIN_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_DATA_DIR);
        addPropertyToVmParameter(project, vmParameters, PROPERTY_HYBRIS_CONFIG_DIR);
    }

    private fun addPropertyToVmParameter(project: Project, vmParameters: ParametersList, propertyKey: String) {
        PropertyService.getInstance(project)
            ?.findProperty(propertyKey)
            ?.let {
                addVmParameterIfNotExist(vmParameters, "-D$propertyKey=$it")
            }
    }

    private fun addVmParameterIfNotExist(vmParameters: ParametersList, newParam: String) {
        if (!vmParameters.hasParameter(newParam)) {
            vmParameters.add(newParam)
        }
    }

}
