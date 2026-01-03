/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.eclipse.project.configurator

import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.module.Module
import org.jetbrains.idea.eclipse.importWizard.EclipseImportBuilder
import sap.commerce.toolset.eclipse.EclipseConstants
import sap.commerce.toolset.eclipse.project.descriptor.EclipseModuleDescriptor
import sap.commerce.toolset.project.configurator.ExternalModuleConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.pathString

class EclipseModuleConfigurator : ExternalModuleConfigurator() {

    override val name: String
        get() = "Eclipse"
    override val moduleTypeId: String
        get() = EclipseConstants.MODULE_TYPE_ID

    override suspend fun import(importContext: ProjectImportContext): Map<ModuleDescriptor, Module> {
        val project = importContext.project
        val eclipseModules = importContext.chosenOtherModuleDescriptors
            .filterIsInstance<EclipseModuleDescriptor>()

        val eclipseImportBuilder = EclipseImportBuilder()
        importContext.modulesFilesDirectory?.let {
            eclipseImportBuilder.parameters.converterOptions.commonModulesDirectory = it.pathString
        }
        eclipseImportBuilder.list = eclipseModules.map { it.moduleRootPath.pathString }

        val modules = backgroundWriteAction {
            eclipseImportBuilder.commit(project)
        }
        return modules
            .mapNotNull { module ->
                val moduleDescriptor = eclipseModules.find { it.name == module.name }
                    ?: return@mapNotNull null
                moduleDescriptor to module
            }
            .associate { it.first to it.second }
    }
}