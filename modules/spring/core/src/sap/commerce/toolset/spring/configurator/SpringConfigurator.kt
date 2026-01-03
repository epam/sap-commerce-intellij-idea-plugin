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

package sap.commerce.toolset.spring.configurator

import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.spring.facet.SpringFacet
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.yExtensionName

class SpringConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Spring"

    override suspend fun configure(
        importContext: ProjectImportContext,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        if (Plugin.SPRING.isDisabled()) return

        val facetModels = modifiableModelsProvider.modules
            .associate { it.yExtensionName() to modifiableModelsProvider.getModifiableFacetModel(it) }

        importContext.chosenHybrisModuleDescriptors
            .forEach { configureFacetDependencies(it, facetModels, it.getDirectDependencies()) }
    }

    private fun configureFacetDependencies(
        moduleDescriptor: ModuleDescriptor,
        facetModels: Map<String, ModifiableFacetModel>,
        dependencies: Set<ModuleDescriptor>
    ) {
        val springFileSet = getSpringFileSet(facetModels, moduleDescriptor)
            ?: return

        dependencies
            .sorted()
            .mapNotNull { getSpringFileSet(facetModels, it) }
            .forEach { springFileSet.addDependency(it) }
    }

    private fun getSpringFileSet(
        facetModels: Map<String, ModifiableFacetModel>,
        moduleDescriptor: ModuleDescriptor
    ) = facetModels[moduleDescriptor.name]
        ?.getFacetByType(SpringFacet.FACET_TYPE_ID)
        ?.fileSets
        ?.takeIf { it.isNotEmpty() }
        ?.iterator()
        ?.next()
}
