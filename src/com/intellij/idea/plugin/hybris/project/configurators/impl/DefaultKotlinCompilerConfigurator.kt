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

import com.intellij.facet.FacetManager
import com.intellij.ide.GeneralSettingsConfigurableEP
import com.intellij.idea.plugin.hybris.project.configurators.HybrisConfiguratorCache
import com.intellij.idea.plugin.hybris.project.configurators.KotlinCompilerConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.kotlin.cli.common.arguments.unfrozen
import org.jetbrains.kotlin.idea.compiler.configuration.Kotlin2JvmCompilerArgumentsHolder
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinCompilerSettings
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType

class DefaultKotlinCompilerConfigurator : KotlinCompilerConfigurator {

    override fun configure(descriptor: HybrisProjectDescriptor, project: Project, cache: HybrisConfiguratorCache) {
//        val moduleRootManager = ModuleRootManager.getInstance(project)
//        val modules = moduleRootManager.modules
//
//
//        for (module in modules) {
//            val kotlinFacet = KotlinFacet.get(module)
//
//            if (kotlinFacet == null) {
//                val facetManager = FacetManager.getInstance(module)
//                val facetType = KotlinFacetType.INSTANCE
//                val facet = facetManager.createFacet(facetType, facetType.defaultFacetName, null)
//                facetManager.addFacet(facet)
//
//                val facetConfiguration = facet.configuration
//                facetConfiguration.settings.compilerSettings.kotlinCompilerVersion = "1.5.0"
//
//                // Save the changes
//                facetConfiguration.commit()
//            } else {
//                val facetConfiguration = kotlinFacet.configuration
//
//                // Set the desired Kotlin compiler version
//                facetConfiguration.kotlinCompilerVersion = "1.5.0"
//
//                // Save the changes
//                kotlinFacet.configuration.commit()
//            }
//        }


//
//        val kotlinCompilerSettings = KotlinCompilerSettings.getInstance(project)
//
//        val compilerSettings = kotlinCompilerSettings.settings.unfrozen()
//        val k2JVMCompilerArguments = Kotlin2JvmCompilerArgumentsHolder.getInstance(project).settings.unfrozen()
//
//        org.jetbrains.kotlin.config.KotlinCompilerSettings
//        k2JVMCompilerArguments.apiVersion = "1.8"
    }
}
