/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.spring.contexts.model.LocalXmlModel
import com.intellij.spring.facet.SpringFacet
import java.io.File

class SpringFacetConfigurator : FacetConfigurator {

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableFacetModel: ModifiableFacetModel,
        moduleDescriptor: ModuleDescriptor,
        javaModule: Module,
        modifiableRootModel: ModifiableRootModel
    ) {
        if (moduleDescriptor !is YModuleDescriptor) return

        WriteAction.runAndWait<RuntimeException> {
            val springFacet = SpringFacet.getInstance(javaModule)
                ?.also { it.removeFileSets() }
                ?: SpringFacet.getSpringFacetType()
                    .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
                    ?.let { it.createFacet(javaModule, it.defaultFacetName, it.createDefaultConfiguration(), null) }
                ?: return@runAndWait

            val facetId = moduleDescriptor.name + SpringFacet.FACET_TYPE_ID
            val springFileSet = springFacet.addFileSet(facetId, facetId)

            moduleDescriptor.springFileSet
                .mapNotNull { VfsUtil.findFileByIoFile(File(it), true) }
                .forEach { springFileSet.addFile(it) }

            val setting = springFacet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED)
            if (setting != null) {
                setting.booleanValue = false
                setting.apply()
            }
            modifiableFacetModel.addFacet(springFacet)
        }
    }
}
