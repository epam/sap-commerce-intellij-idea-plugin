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

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.isCustomModuleDescriptor
import sap.commerce.toolset.util.directoryExists

class KotlinFacetConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Kotlin Facet"

    override fun configure(
        context: ProjectPostImportContext,
        legacyWorkspace: IdeModifiableModelsProvider,
        edtActions: MutableList<() -> Unit>
    ) {
        context.chosenHybrisModuleDescriptors
            .firstOrNull { EiConstants.Extension.KOTLIN_NATURE == it.name }
            ?: return

        val modules = context.modules

        context.chosenHybrisModuleDescriptors
            .filter { it.isCustomModuleDescriptor }
            .filter { it.hasKotlinDirectories }
            .forEach { moduleDescriptor ->
                val ideaModuleName = moduleDescriptor.ideaModuleName()
                val module = modules[ideaModuleName] ?: return@forEach

                val modifiableFacetModel = legacyWorkspace.getModifiableFacetModel(module)

                // Remove previously registered Kotlin Facet for extensions with removed kotlin sources
                modifiableFacetModel.getFacetByType(KotlinFacetType.TYPE_ID)
                    ?.let { modifiableFacetModel.removeFacet(it) }

                val facet = KotlinFacet.get(module)
                    ?: createFacet(module)

                modifiableFacetModel.addFacet(facet)
            }
    }

    private fun createFacet(module: Module) = with(KotlinFacetType.INSTANCE) {
        createFacet(
            module,
            defaultFacetName,
            createDefaultConfiguration(),
            null
        )
    }

    private val ModuleDescriptor.hasKotlinDirectories
        get() = this.moduleRootPath.resolve(ProjectConstants.Directory.KOTLIN_SRC).directoryExists
            || this.moduleRootPath.resolve(ProjectConstants.Directory.KOTLIN_TEST_SRC).directoryExists

}
