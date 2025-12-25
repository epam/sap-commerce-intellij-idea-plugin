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

package sap.commerce.toolset.project.localextensions

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor

@Service
class ProjectLocalExtensionsProcessor {

    @Throws(InterruptedException::class)
    fun process(importContext: ProjectImportContext.Mutable, configModuleDescriptor: ConfigModuleDescriptor) {
        val explicitlyDefinedModules = ProjectLocalExtensionsScanner.getInstance()
            .processHybrisConfig(importContext, configModuleDescriptor)

        preselectModules(importContext, configModuleDescriptor, explicitlyDefinedModules)
    }

    private fun preselectModules(
        importContext: ProjectImportContext.Mutable,
        configModuleDescriptor: ConfigModuleDescriptor,
        explicitlyDefinedModules: Set<String>
    ) {
        importContext.foundModules
            .filter { explicitlyDefinedModules.contains(it.name) }
            .filterIsInstance<YRegularModuleDescriptor>()
            .forEach { moduleDescriptor ->
                moduleDescriptor.isInLocalExtensions = true
                moduleDescriptor.getDirectDependencies()
                    .filterIsInstance<YRegularModuleDescriptor>()
                    .forEach { it.isNeededDependency = true }
            }

        preselectConfigModules(configModuleDescriptor, importContext.foundModules)
    }

    private fun preselectConfigModules(
        configModuleDescriptor: ConfigModuleDescriptor,
        foundModules: Collection<ModuleDescriptor>
    ) {
        configModuleDescriptor.importStatus = ModuleDescriptorImportStatus.MANDATORY
        configModuleDescriptor.isMainConfig = true
        configModuleDescriptor.setPreselected(true)

        val preselectedNames = mutableSetOf<String>().also {
            it.add(configModuleDescriptor.name)
        }

        foundModules
            .filterIsInstance<ConfigModuleDescriptor>()
            .filterNot { preselectedNames.contains(it.name) }
            .forEach {
                it.setPreselected(true)
                preselectedNames.add(it.name)
            }
    }

    companion object {
        fun getInstance(): ProjectLocalExtensionsProcessor = application.service()
    }
}