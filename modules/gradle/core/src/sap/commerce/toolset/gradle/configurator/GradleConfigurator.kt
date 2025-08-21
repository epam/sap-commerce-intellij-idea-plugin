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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.observation.launchTracked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings
import sap.commerce.toolset.gradle.descriptor.GradleModuleDescriptor
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.configurator.ConfiguratorCache
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.settings.ProjectSettings

class GradleConfigurator : ProjectImportConfigurator, ProjectRefreshConfigurator {

    override fun configure(
        project: Project,
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>,
        rootProjectModifiableModel: ModifiableModuleModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        cache: ConfiguratorCache
    ) {
        indicator.text = i18n("hybris.project.import.gradle")

        try {
            hybrisProjectDescriptor
                .modulesChosenForImport
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

    override fun beforeRefresh(project: Project) {
        if (!ProjectSettings.getInstance(project).removeExternalModulesOnRefresh) return

        GradleSettings.getInstance(project).linkedProjectsSettings = emptyList()
    }

}
