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
package sap.commerce.toolset.kotlin.configurator

import com.intellij.facet.FacetManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.kotlin.idea.compiler.configuration.Kotlin2JvmCompilerArgumentsHolder
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinJpsPluginSettings
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.kotlin.idea.configuration.KotlinJavaModuleConfigurator
import org.jetbrains.kotlin.idea.configuration.NotificationMessageCollector
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import org.jetbrains.kotlin.idea.projectConfiguration.KotlinProjectConfigurationBundle
import org.jetbrains.kotlin.idea.projectConfiguration.getDefaultJvmTarget
import org.jetbrains.kotlin.idea.util.application.isUnitTestMode
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.kotlin.KotlinConstants
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectPostImportAsyncConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectPostImportContext

class KotlinConfigurator : ProjectImportConfigurator, ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "Kotlin"

    override suspend fun configure(context: ProjectImportContext) {
        val project = context.project
        val hasKotlinnatureExtension = context.chosenHybrisModuleDescriptors
            .any { EiConstants.Extension.KOTLIN_NATURE == it.name }
        if (!hasKotlinnatureExtension) return

        readAction {
            setKotlinCompilerVersion(project, KotlinConstants.KOTLIN_COMPILER_FALLBACK_VERSION)
        }
        setKotlinJvmTarget(project)
    }

    override suspend fun configure(context: ProjectPostImportContext) {
        val project = context.project
        val hasKotlinnatureExtension = context.chosenHybrisModuleDescriptors
            .any { EiConstants.Extension.KOTLIN_NATURE == it.name }
        if (!hasKotlinnatureExtension) {
            removeKotlinFacets(project)
            return
        }

        smartReadAction(project) {
            val compilerVersion = PropertyService.getInstance(project)
                .findProperty(KotlinConstants.KOTLIN_COMPILER_VERSION_PROPERTY_KEY)
                ?: KotlinConstants.KOTLIN_COMPILER_FALLBACK_VERSION

            setKotlinCompilerVersion(project, compilerVersion)
        }

        registerKotlinLibrary(project)
    }

    private suspend fun removeKotlinFacets(project: Project) {
        val writeActions = readAction {
            ModuleManager.getInstance(project).modules
                .mapNotNull { module ->
                    val facetManager = FacetManager.getInstance(module)
                    val kotlinFacet = facetManager.getFacetByType(KotlinFacetType.TYPE_ID)
                        ?: return@mapNotNull null
                    val createModifiableModel = facetManager.createModifiableModel()

                    val writeAction = {
                        createModifiableModel.removeFacet(kotlinFacet)
                        createModifiableModel.commit()
                    }
                    writeAction
                }
        }

        backgroundWriteAction {
            writeActions.forEach { it() }
        }
    }

    private suspend fun registerKotlinLibrary(project: Project) {
        val kotlinAwareModules = readAction {
            ModuleManager.getInstance(project).modules
                .filter { FacetManager.getInstance(it).getFacetByType(KotlinFacetType.TYPE_ID) != null }
                .toSet()
        }
            .takeIf { it.isNotEmpty() }
            ?: return

        val collector = NotificationMessageCollector.create(project)

        backgroundWriteAction {
            KotlinJavaModuleConfigurator.instance.getOrCreateKotlinLibrary(project, collector)
        }

        val writeActions = mutableListOf<() -> Unit>()

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

        backgroundWriteAction {
            writeActions.forEach { it() }
        }
    }

    // Kotlin compiler version will be updated after project import / refresh in BGT
    // we have to have indexes ready to be able to get the correct value of the project property responsible for a custom Kotlin compiler version
    private fun setKotlinCompilerVersion(project: Project, compilerVersion: String) {
        KotlinJpsPluginSettings.getInstance(project).update {
            version = compilerVersion
        }
    }

    private suspend fun setKotlinJvmTarget(project: Project) = readAction {
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
