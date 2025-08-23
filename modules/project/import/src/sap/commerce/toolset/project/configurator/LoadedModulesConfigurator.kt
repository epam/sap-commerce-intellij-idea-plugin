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

package sap.commerce.toolset.project.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.application
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus
import sap.commerce.toolset.project.settings.ProjectSettings

class LoadedModulesConfigurator : ProjectImportConfigurator {

    override fun configure(
        project: Project,
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        cache: ConfiguratorCache
    ) {
        val unusedModuleNames = hybrisProjectDescriptor.modulesChosenForImport
            .filter { it.importStatus == ModuleDescriptorImportStatus.UNUSED }
            .map { it.name }
            .toMutableSet()

        application.invokeAndWait {
            ProjectSettings.getInstance(project).unusedExtensions = unusedModuleNames
        }
    }
}