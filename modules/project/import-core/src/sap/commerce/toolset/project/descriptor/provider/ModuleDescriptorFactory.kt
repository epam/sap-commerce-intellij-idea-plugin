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

package sap.commerce.toolset.project.descriptor.provider

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.project.context.ModuleDescriptorProviderContext
import sap.commerce.toolset.project.context.ModuleRoot
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.RootModuleDescriptor

@Service
class ModuleDescriptorFactory {

    @Throws(HybrisConfigurationException::class)
    fun createDescriptor(importContext: ProjectImportContext.Mutable, moduleRoot: ModuleRoot): ModuleDescriptor {
        val path = moduleRoot.path
        val context = ModuleDescriptorProviderContext(
            project = importContext.project,
            externalExtensionsDirectory = importContext.externalExtensionsDirectory,
            moduleRoot = moduleRoot,
        )

        return ModuleDescriptorProvider.EP.extensionList
            .firstOrNull { it.isApplicable(context) }
            ?.create(context)
            ?.also { thisLogger().info("Creating '${it.type}' module for $path") }
            ?: throw HybrisConfigurationException("Could not find suitable module descriptor provider for $path")
    }

    fun createRootDescriptor(moduleRoot: ModuleRoot) = RootModuleDescriptor(moduleRoot)

    companion object {
        fun getInstance(): ModuleDescriptorFactory = application.service()
    }
}