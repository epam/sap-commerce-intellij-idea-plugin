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

import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.facet.YFacetConstants

class YFacetConfigurator : ModuleFacetConfigurator {

    override val name: String
        get() = "SAP CX Facet"

    override fun configureModuleFacet(
        module: Module,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        modifiableRootModel: ModifiableRootModel
    ) {
        WriteAction.runAndWait<RuntimeException> {
            modifiableFacetModel.getFacetByType(YFacetConstants.Y_FACET_TYPE_ID)
                ?.let { modifiableFacetModel.removeFacet(it) }

            val facetType = FacetTypeRegistry.getInstance().findFacetType(YFacetConstants.Y_FACET_TYPE_ID)
            val facet = facetType.createFacet(
                module,
                facetType.defaultFacetName,
                facetType.createDefaultConfiguration(),
                null
            )
            facet.configuration.loadState(moduleDescriptor.extensionDescriptor())

            modifiableFacetModel.addFacet(facet)
        }
    }
}