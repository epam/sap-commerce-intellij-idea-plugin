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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.util.application
import sap.commerce.toolset.project.descriptor.*

@Service
class ProjectImportLocalExtensionsProcessor {

    @Throws(InterruptedException::class)
    fun processLocalExtensions(projectDescriptor: HybrisProjectDescriptor) {
        val localExtensionsScanner = ProjectImportLocalExtensionsScanner.getInstance()
        val configHybrisModuleDescriptor = localExtensionsScanner.findConfigDir(projectDescriptor)

        if (configHybrisModuleDescriptor == null) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showErrorDialog(
                    """
                    The ‘config’ module hasn’t been detected, which will affect the following functionality:
                    
                     · module auto-selection
                     · building modules in IDE
                     · resolving properties
                    
                    """.trimIndent(),
                    "Project Configuration Incomplete"
                )
            }
            return
        }
        val explicitlyDefinedModules = localExtensionsScanner.processHybrisConfig(projectDescriptor, configHybrisModuleDescriptor)
        preselectModules(configHybrisModuleDescriptor, explicitlyDefinedModules, projectDescriptor)
    }

    private fun preselectModules(
        configHybrisModuleDescriptor: ConfigModuleDescriptor,
        explicitlyDefinedModules: Set<String>,
        projectDescriptor: HybrisProjectDescriptor
    ) {
        projectDescriptor.foundModules
            .filter { explicitlyDefinedModules.contains(it.name) }
            .filterIsInstance<YRegularModuleDescriptor>()
            .forEach {
                it.isInLocalExtensions = true
                it.getDirectDependencies()
                    .filterIsInstance<YRegularModuleDescriptor>()
                    .forEach { it.isNeededDependency = true }
            }

        preselectConfigModules(configHybrisModuleDescriptor, projectDescriptor.foundModules)
    }

    private fun preselectConfigModules(
        configHybrisModuleDescriptor: ConfigModuleDescriptor,
        foundModules: Collection<ModuleDescriptor>
    ) {
        configHybrisModuleDescriptor.importStatus = ModuleDescriptorImportStatus.MANDATORY
        configHybrisModuleDescriptor.isMainConfig = true
        configHybrisModuleDescriptor.setPreselected(true)

        val preselectedNames = mutableSetOf<String>().also {
            it.add(configHybrisModuleDescriptor.name)
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
        fun getInstance(): ProjectImportLocalExtensionsProcessor = application.service()
    }
}