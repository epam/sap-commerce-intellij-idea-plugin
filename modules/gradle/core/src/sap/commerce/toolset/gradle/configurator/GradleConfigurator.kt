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
package sap.commerce.toolset.gradle.configurator

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.observation.launchTracked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import sap.commerce.toolset.gradle.descriptor.GradleModuleDescriptor
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.triggerAction

class GradleConfigurator : ProjectImportConfigurator, ProjectPostImportConfigurator, ProjectRefreshConfigurator {

    override val name: String
        get() = "Gradle"

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val project = hybrisProjectDescriptor.project ?: return
        PropertiesComponent.getInstance(project)
            .setValue("show.inlinked.gradle.project.popup", false)

        try {
            hybrisProjectDescriptor
                .chosenModuleDescriptors
                .filterIsInstance<GradleModuleDescriptor>()
                .mapNotNull { it.gradleFile.path }
                .forEach { externalProjectPath ->
                    CoroutineScope(Dispatchers.Default).launchTracked {
                        linkAndSyncGradleProject(project, externalProjectPath)
                    }
                }
        } catch (e: Exception) {
            thisLogger().error("Can not import Gradle modules due to an error.", e)
        }
    }

    override suspend fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        if (!hybrisProjectDescriptor.refresh) return
        val project = hybrisProjectDescriptor.project ?: return

        edtWriteAction {
            project.triggerAction("ExternalSystem.RefreshAllProjects") {
                SimpleDataContext.builder()
                    .add(CommonDataKeys.PROJECT, project)
                    .add(ExternalSystemDataKeys.EXTERNAL_SYSTEM_ID, GradleConstants.SYSTEM_ID)
                    .build()
            }
        }
    }

    override fun beforeRefresh(project: Project) {
        if (!ProjectSettings.getInstance(project).removeExternalModulesOnRefresh) return

        GradleSettings.getInstance(project).linkedProjectsSettings = emptyList()
    }

}
