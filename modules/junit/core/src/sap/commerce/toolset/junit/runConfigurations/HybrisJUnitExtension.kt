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

package sap.commerce.toolset.junit.runConfigurations

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisConstants.PROPERTY_PLATFORMHOME
import sap.commerce.toolset.HybrisConstants.PROPERTY_STANDALONE_JDKMODULESEXPORTS
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.settings.WorkspaceSettings

class HybrisJUnitExtension : RunConfigurationExtension() {

    override fun isApplicableFor(configuration: RunConfigurationBase<*>) =
        if (configuration !is JUnitConfiguration) false
        else WorkspaceSettings.getInstance(configuration.project).hybrisProject

    private fun updateSapCXJVMProperties(project: Project, params: JavaParameters) {
        val vmParameters = params.vmParametersList
        PropertyService.getInstance(project).let { service ->
            service.getPlatformHome()?.let {
                addVmParameterIfNotExist(vmParameters, "-D$PROPERTY_PLATFORMHOME=$it")
            }

            service.findProperty(PROPERTY_STANDALONE_JDKMODULESEXPORTS)?.let { propertyValue ->
                propertyValue.split(' ').forEach {
                    addVmParameterIfNotExist(vmParameters, it.replace("\"", ""))
                }
            }
        }
    }

    override fun <T : RunConfigurationBase<*>?> updateJavaParameters(
        configuration: T & Any, params: JavaParameters, runnerSettings: RunnerSettings?
    ) {

        if (runnerSettings != null || !isApplicableFor(configuration)) return

        val junitConfig = (configuration as JUnitConfiguration)
        val project = configuration.project

        if (isPureUnitTest(junitConfig, project)) return

        updateSapCXJVMProperties(project, params)

        enhanceClassPath(params, project)
    }

    private fun isPureUnitTest(configuration: JUnitConfiguration, project: Project) = configuration.runClass
        ?.let {
            JavaPsiFacade.getInstance(project)
                .findClass(it, GlobalSearchScope.allScope(project))
        }
        ?.annotations
        ?.any { it.qualifiedName == HybrisConstants.CLASS_FQN_UNIT_TEST }
        ?: false

    private fun enhanceClassPath(params: JavaParameters, project: Project) {
        val classPathEntries = HashSet<String>()

        val modules: Array<Module> = ModuleManager.getInstance(project).modules
        for (module in modules) {

            if (YFacet.getState(module)?.type?.name.equals("CCV2")) {
                continue
            }

            // Get the module's output paths (both production and test)
            val moduleRootManager = ModuleRootManager.getInstance(module)

            // Get the compiler output paths for production and test
            val productionOutput = moduleRootManager.getModuleExtension(CompilerModuleExtension::class.java)
                ?.compilerOutputPath
            val testOutput = moduleRootManager.getModuleExtension(CompilerModuleExtension::class.java)
                ?.compilerOutputPathForTests

            // Add the output paths to the classpath
            if (productionOutput != null && classPathEntries.add(productionOutput.path)) {
                params.classPath.add(productionOutput.path)
            }
            if (testOutput != null && classPathEntries.add(testOutput.path)) {
                params.classPath.add(testOutput.path)
            }

            // **Add module dependencies to classpath**
            OrderEnumerator.orderEntries(module)
                .recursively()
                .classes().roots.forEach {
                    val path = it.presentableUrl
                    if (classPathEntries.add(path)) {
                        params.classPath.add(it)
                    }
                }
        }
    }

    private fun addVmParameterIfNotExist(vmParameters: ParametersList, newParam: String) {
        if (!vmParameters.hasParameter(newParam)) {
            vmParameters.add(newParam)
        }
    }

}
