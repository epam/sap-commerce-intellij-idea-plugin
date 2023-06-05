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
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType

/**
 * Kotlin Facets will be configured only if `kotlinnature` extension is available and
 */
class KotlinFacetConfigurator : FacetConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        if (moduleDescriptor !is YModuleDescriptor) return

        val hasKotlinDirectories = YModuleDescriptorUtil.hasKotlinDirectories(moduleDescriptor)

        WriteAction.runAndWait<RuntimeException> {
            // Remove previously registered Kotlin Facet for extensions with removed kotlin sources
            modifiableFacetModel.getFacetByType(KotlinFacetType.TYPE_ID)
                ?.takeUnless { hasKotlinDirectories }
                ?.let { modifiableFacetModel.removeFacet(it) }

            if (!hasKotlinDirectories) return@runAndWait
            if (hybrisProjectDescriptor.kotlinNatureModuleDescriptor == null) return@runAndWait

            val facet = KotlinFacet.get(javaModule)
                ?: createFacet(javaModule)

            modifiableFacetModel.addFacet(facet)
        }
    }

    private fun createFacet(javaModule: Module) = with(KotlinFacetType.INSTANCE) {
        createFacet(
            javaModule,
            defaultFacetName,
            createDefaultConfiguration(),
            null
        )
    }

}
