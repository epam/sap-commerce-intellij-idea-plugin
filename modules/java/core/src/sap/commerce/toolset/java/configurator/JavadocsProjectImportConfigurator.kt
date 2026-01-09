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

package sap.commerce.toolset.java.configurator

import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.JavaModuleExternalPaths
import com.intellij.platform.backend.workspace.WorkspaceModel
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet

class JavadocsProjectImportConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Javadocs"

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        val javadocUrl = importContext.javadocUrl ?: return
        val modifiableModelsProvider = IdeModifiableModelsProviderImpl(importContext.project)

        ModuleManager.getInstance(importContext.project).modules
            .filterNot { module ->
                YFacet.getState(module)
                    ?.type
                    ?.let { it != ModuleDescriptorType.CUSTOM && it == ModuleDescriptorType.CONFIG }
                    ?: false
            }
            .forEach {
                val modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(it)
                val javaModuleExternalPaths = modifiableRootModel.getModuleExtension(JavaModuleExternalPaths::class.java)

                javaModuleExternalPaths.javadocUrls = listOf(javadocUrl).toTypedArray()
            }

        backgroundWriteAction { modifiableModelsProvider.commit() }
    }
}