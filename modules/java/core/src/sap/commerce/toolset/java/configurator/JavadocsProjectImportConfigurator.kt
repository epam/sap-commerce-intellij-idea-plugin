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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.roots.JavaModuleExternalPaths
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.isNonCustomModuleDescriptor

class JavadocsProjectImportConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Javadocs"

    override fun configure(
        context: ProjectPostImportContext,
        legacyWorkspace: IdeModifiableModelsProvider,
        edtActions: MutableList<() -> Unit>
    ) {
        val javadocUrl = context.javadocUrl ?: return

        context.chosenHybrisModuleDescriptors
            .filter { it.isNonCustomModuleDescriptor }
            .filterNot { it is ConfigModuleDescriptor }
            .forEach { moduleDescriptor ->
                val module = context.moduleBridges[moduleDescriptor.name] ?: run {
                    thisLogger().warn("Could not find module for ${moduleDescriptor.name}")
                    return@forEach
                }

                val modifiableRootModel = legacyWorkspace.getModifiableRootModel(module)
                val javaModuleExternalPaths = modifiableRootModel.getModuleExtension(JavaModuleExternalPaths::class.java)

                javaModuleExternalPaths.javadocUrls = listOf(javadocUrl).toTypedArray()
            }
    }
}
