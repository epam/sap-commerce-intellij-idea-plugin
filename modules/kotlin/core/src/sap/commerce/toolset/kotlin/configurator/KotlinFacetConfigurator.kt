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

import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.configurator.ModuleFacetConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import java.io.File

class KotlinFacetConfigurator : ModuleFacetConfigurator {

    override val name: String
        get() = "Kotlin Facet"

    override fun configureModuleFacet(
        module: Module,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
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

    private fun hasKotlinDirectories(descriptor: ModuleDescriptor) = File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_SRC_DIRECTORY).exists()
        || File(descriptor.moduleRootDirectory, HybrisConstants.KOTLIN_TEST_SRC_DIRECTORY).exists()

}
