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
package sap.commerce.toolset.gradle.project.configurator

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject
import org.jetbrains.plugins.gradle.util.GradleConstants
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.gradle.project.descriptor.GradleModuleDescriptor
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.util.fileExists
import kotlin.io.path.pathString

class GradleConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Gradle"

    override suspend fun configure(context: ProjectPostImportContext) {
        val project = context.project
        PropertiesComponent.getInstance(project).setValue("show.inlinked.gradle.project.popup", false)

        val gradleProjectPaths = context.chosenOtherModuleDescriptors
            .filterIsInstance<GradleModuleDescriptor>()
            .filter { it.gradleFile.fileExists }
            .map { it.gradleFile.pathString }
            .takeIf { it.isNotEmpty() }
            ?: return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                gradleProjectPaths.forEach { externalProjectPath -> linkAndSyncGradleProject(project, externalProjectPath) }
            } catch (e: Exception) {
                thisLogger().error("Can not import Gradle modules due to an error.", e)
            }

            if (!context.refresh) return@launch

            backgroundWriteAction {
                project.triggerAction("ExternalSystem.RefreshAllProjects") {
                    SimpleDataContext.builder()
                        .add(CommonDataKeys.PROJECT, project)
                        .add(ExternalSystemDataKeys.EXTERNAL_SYSTEM_ID, GradleConstants.SYSTEM_ID)
                        .build()
                }
            }
        }
    }
}
