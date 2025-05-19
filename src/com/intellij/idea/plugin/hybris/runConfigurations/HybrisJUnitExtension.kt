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

import com.intellij.execution.Executor
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import java.util.*

class HybrisJUnitExtension : RunConfigurationExtension() {

    private companion object {
        const val JVM_ADDITIONAL_PREFIX = "wrapper.java.additional."
        const val STRIP_QUOTES_SUFFIX = ".stripquotes"
    }

    override fun isApplicableFor(configuration: RunConfigurationBase<*>) =
        if (configuration !is JUnitConfiguration) false
        else ProjectSettingsComponent.getInstance(configuration.project)
            .isHybrisProject()

    override fun <T : RunConfigurationBase<*>?> updateJavaParameters(
        configuration: T & Any,
        params: JavaParameters,
        runnerSettings: RunnerSettings?,
        executor: Executor
    ) {
        val project = configuration.project
        val junitConfig = (configuration as JUnitConfiguration)

        if (isApplicableFor(configuration) && !isPureUnitTest(junitConfig, project)) {
            updateSapCXJVMProperties(project, params, executor)
        }

        super.updateJavaParameters(configuration, params, runnerSettings, executor)
    }

    private fun updateSapCXJVMProperties(project: Project, params: JavaParameters, executor: Executor) {
        PropertyService.getInstance(project)
            ?.let { propertyService ->

                val vmParameters = params.vmParametersList

                val tomcatWrapperProperties = when {
                    executor.id == DefaultDebugExecutor.EXECUTOR_ID -> propertyService.getTomcatWrapperProperties("debug")
                    else -> propertyService.getTomcatWrapperProperties()
                }

                getTomcatWrapperJVMProperties(tomcatWrapperProperties).forEach {
                    addVmParameterIfNotExist(vmParameters, it)
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

        enhanceClassPath(params, project)
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

    private fun getTomcatWrapperJVMProperties(
        properties: Properties
    ): List<String> = properties.entries
        .asSequence()
        .map { it.key.toString() to it.value.toString() }
        .filter { (key, _) -> key.startsWith(JVM_ADDITIONAL_PREFIX) }
        .filter { (key, _) -> !key.endsWith(STRIP_QUOTES_SUFFIX) }
        .sortedBy { (key, _) ->
            key.removePrefix(JVM_ADDITIONAL_PREFIX)
                .toIntOrNull() ?: Int.MAX_VALUE
        }
        .map { (key, value) ->
            when {
                shouldStripQuotes(key, properties) -> value.replace("\"", "").trim()
                else -> value.trim()
            }
        }
        .toList()

    private fun shouldStripQuotes(key: String, properties: Properties): Boolean =
        properties["$key$STRIP_QUOTES_SUFFIX"]
            ?.toString()
            ?.uppercase() == "TRUE"

    private fun addVmParameterIfNotExist(vmParameters: ParametersList, newParam: String) {
        if (!vmParameters.hasParameter(newParam)) {
            vmParameters.add(newParam)
        }
    }

}
