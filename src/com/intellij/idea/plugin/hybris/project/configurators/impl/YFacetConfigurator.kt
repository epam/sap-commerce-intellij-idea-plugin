/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ModifiableFacetModel
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.facet.YFacetState
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel

/**
 * Main [y] SAP Commerce Facet, acts as a holder for all Module specific configurations and settings.
 */
class YFacetConfigurator : FacetConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        WriteAction.runAndWait<RuntimeException> {
            modifiableFacetModel.getFacetByType(HybrisConstants.Y_FACET_TYPE_ID)
                ?.let { modifiableFacetModel.removeFacet(it) }

            val facetType = FacetTypeRegistry.getInstance().findFacetType(HybrisConstants.Y_FACET_TYPE_ID)
            val facet = facetType.createFacet(
                javaModule,
                facetType.defaultFacetName,
                facetType.createDefaultConfiguration(),
                null
            )
            val state = YFacetState(
                name = moduleDescriptor.name,
                readonly = moduleDescriptor.readonly,
                moduleDescriptorType = moduleDescriptor.descriptorType,
                subModuleDescriptorType = (moduleDescriptor as? YSubModuleDescriptor)?.subModuleDescriptorType,
            )
            facet.configuration.loadState(state)

            modifiableFacetModel.addFacet(facet)
        }
    }

}
