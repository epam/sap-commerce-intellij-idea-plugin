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
package sap.commerce.toolset.project.configurators.impl

import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.configurators.FacetConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import java.io.File

/**
 * Kotlin Facets will be configured only if `kotlinnature` extension is available and
 */
@Service
class KotlinFacetConfigurator : FacetConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        if (moduleDescriptor !is YModuleDescriptor) return

        val hasKotlinDirectories = hasKotlinDirectories(moduleDescriptor)

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

    private fun hasKotlinDirectories(descriptor: ModuleDescriptor) = File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_SRC_DIRECTORY).exists()
        || File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_TEST_SRC_DIRECTORY).exists()

}
