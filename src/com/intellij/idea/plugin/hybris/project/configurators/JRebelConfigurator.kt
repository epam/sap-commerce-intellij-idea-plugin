/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.facet.FacetType
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.zeroturnaround.javarebel.idea.plugin.actions.ToggleRebelFacetAction
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacet
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacetType

class JRebelConfigurator {

    fun configureAfterImport(project: Project, moduleDescriptors: List<ModuleDescriptor>) = moduleDescriptors
        .filter {
            it is YCustomRegularModuleDescriptor
                || (it is YSubModuleDescriptor && it.owner is YCustomRegularModuleDescriptor)
        }
        .mapNotNull { ModuleManager.getInstance(project).findModuleByName(it.ideaModuleName()) }
        .mapNotNull { configure(it) }

    private fun configure(javaModule: Module): (() -> Unit)? {
        val facet = JRebelFacet.getInstance(javaModule)

        if (facet != null) return null

        val facetType = FacetType.findInstance(JRebelFacetType::class.java)

        if (!facetType.isSuitableModuleType(ModuleType.get(javaModule))) return null

        return { ToggleRebelFacetAction.conditionalEnableJRebelFacet(javaModule, false, false) }
    }

    companion object {
        fun getInstance(): JRebelConfigurator? = ApplicationManager.getApplication().getService(JRebelConfigurator::class.java)
    }
}