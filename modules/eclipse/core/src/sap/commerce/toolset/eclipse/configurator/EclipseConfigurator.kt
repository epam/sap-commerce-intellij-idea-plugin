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

package sap.commerce.toolset.eclipse.configurator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import org.jetbrains.idea.eclipse.importWizard.EclipseImportBuilder
import sap.commerce.toolset.eclipse.descriptor.EclipseModuleDescriptor
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

class EclipseConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Eclipse"

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val project = hybrisProjectDescriptor.project ?: return
        val projectList = hybrisProjectDescriptor.chosenModuleDescriptors
            .filterIsInstance<EclipseModuleDescriptor>()
            .map { it.moduleRootDirectory }
            .map { it.path }

        val eclipseImportBuilder = EclipseImportBuilder()
        hybrisProjectDescriptor.modulesFilesDirectory?.let {
            eclipseImportBuilder.parameters.converterOptions.commonModulesDirectory = it.path
        }

        eclipseImportBuilder.list = projectList

        ApplicationManager.getApplication().invokeAndWait {
            // TODO: java.lang.Throwable: Slow operations are prohibited on EDT. See SlowOperations.assertSlowOperationsAreAllowed javadoc.
            eclipseImportBuilder.commit(project)
        }
    }
}