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

import com.intellij.facet.ModifiableFacetModel
import com.intellij.idea.plugin.hybris.project.configurators.FacetConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType

class KotlinFacetConfigurator : FacetConfigurator {

    override fun configure(
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: HybrisModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        // Remove previously registered Kotlin Facet for extensions with removed kotlin sources
        modifiableFacetModel.getFacetByType(KotlinFacetType.TYPE_ID)
            ?.takeUnless { moduleDescriptor.isKotlinEnabled }
            ?.let { modifiableFacetModel.removeFacet(it) }

        if (!moduleDescriptor.isKotlinEnabled) return

        val facet = KotlinFacet.get(javaModule)
            ?: createFacet(javaModule)

        modifiableFacetModel.addFacet(facet)
    }

    private fun createFacet(javaModule: Module) = with(KotlinFacetType.INSTANCE) {
        this.createFacet(
            javaModule,
            this.defaultFacetName,
            this.createDefaultConfiguration(),
            null
        )
    }

}
