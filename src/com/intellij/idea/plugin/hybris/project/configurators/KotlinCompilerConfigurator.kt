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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.facet.FacetManager
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.application
import org.jetbrains.kotlin.idea.compiler.configuration.Kotlin2JvmCompilerArgumentsHolder
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinJpsPluginSettings
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.kotlin.idea.configuration.KotlinJavaModuleConfigurator
import org.jetbrains.kotlin.idea.configuration.NotificationMessageCollector
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import org.jetbrains.kotlin.idea.projectConfiguration.KotlinProjectConfigurationBundle
import org.jetbrains.kotlin.idea.projectConfiguration.getDefaultJvmTarget
import org.jetbrains.kotlin.idea.util.application.isUnitTestMode

class KotlinCompilerConfigurator {

    fun configure(descriptor: HybrisProjectDescriptor, project: Project) {
        val hasKotlinnatureExtension = descriptor.modulesChosenForImport.stream()
            .anyMatch { HybrisConstants.EXTENSION_NAME_KOTLIN_NATURE == it.name }
        if (!hasKotlinnatureExtension) return

        setKotlinCompilerVersion(project, HybrisConstants.KOTLIN_COMPILER_FALLBACK_VERSION)
        setKotlinJvmTarget(project)
    }

    fun configureAfterImport(project: Project): List<() -> Unit> {
        val hasKotlinnatureExtension = ModuleManager.getInstance(project).modules
            .any { HybrisConstants.EXTENSION_NAME_KOTLIN_NATURE == it.yExtensionName() }
        if (!hasKotlinnatureExtension) return emptyList()

        val compilerVersion = PropertyService.getInstance(project)
            .findProperty(HybrisConstants.KOTLIN_COMPILER_VERSION_PROPERTY_KEY)
            ?: HybrisConstants.KOTLIN_COMPILER_FALLBACK_VERSION
        setKotlinCompilerVersion(project, compilerVersion)

        return registerKotlinLibrary(project)
    }

    private fun registerKotlinLibrary(project: Project): List<() -> Unit> {
        val kotlinAwareModules = ModuleManager.getInstance(project).modules
            .filter { FacetManager.getInstance(it).getFacetByType(KotlinFacetType.TYPE_ID) != null }
            .toSet()

        if (kotlinAwareModules.isEmpty()) return emptyList()

        val collector = NotificationMessageCollector.create(project)
        val actions = mutableListOf<() -> Unit>()

        actions.add() {
            val writeActions = mutableListOf<() -> Unit>()

            runWriteAction {
                KotlinJavaModuleConfigurator.instance.getOrCreateKotlinLibrary(project, collector)
            }

            ActionUtil.underModalProgress(project, KotlinProjectConfigurationBundle.message("configure.kotlin.in.modules.progress.text")) {
                val progressIndicator = ProgressManager.getGlobalProgressIndicator()

                for ((index, module) in kotlinAwareModules.withIndex()) {
                    if (!isUnitTestMode()) {
                        progressIndicator?.let {
                            it.checkCanceled()
                            it.fraction = index * 1.0 / kotlinAwareModules.size
                            it.text = KotlinProjectConfigurationBundle.message("configure.kotlin.in.modules.progress.text")
                            it.text2 = KotlinProjectConfigurationBundle.message("configure.kotlin.in.module.0.progress.text", module.name)
                        }
                    }
                    KotlinJavaModuleConfigurator.instance.configureModule(module, collector, writeActions)
                }
            }

            writeActions.forEach { it() }
        }
        return actions
    }

    // Kotlin compiler version will be updated after project import / refresh in BGT
    // we have to have indexes ready to be able to get the correct value of the project property responsible for a custom Kotlin compiler version
    private fun setKotlinCompilerVersion(project: Project, compilerVersion: String) {
        application.runReadAction {
            KotlinJpsPluginSettings.getInstance(project).update {
                version = compilerVersion
            }
        }
    }

    private fun setKotlinJvmTarget(project: Project) {
        application.runReadAction {
            val projectRootManager = ProjectRootManager.getInstance(project)

            projectRootManager.projectSdk
                ?.let { sdk -> getDefaultJvmTarget(sdk, KotlinPluginLayout.ideCompilerVersion) }
                ?.let { defaultJvmTarget ->
                    Kotlin2JvmCompilerArgumentsHolder.getInstance(project).update {
                        jvmTarget = defaultJvmTarget.description
                    }
                }
        }
    }

}
