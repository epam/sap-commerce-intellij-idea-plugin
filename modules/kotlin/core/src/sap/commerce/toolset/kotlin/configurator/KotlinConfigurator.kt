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

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.kotlin.idea.compiler.configuration.Kotlin2JvmCompilerArgumentsHolder
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinJpsPluginSettings
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.kotlin.idea.projectConfiguration.getDefaultJvmTarget
import sap.commerce.toolset.kotlin.KotlinConstants
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext

class KotlinConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Kotlin"

    override suspend fun configure(context: ProjectImportContext) {
        val project = context.project
        val hasKotlinNatureExtension = context.hasKotlinNatureExtension
        if (!hasKotlinNatureExtension) return

        readAction {
            setKotlinCompilerVersion(project, KotlinConstants.KOTLIN_COMPILER_FALLBACK_VERSION)
        }
        setKotlinJvmTarget(project)
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
