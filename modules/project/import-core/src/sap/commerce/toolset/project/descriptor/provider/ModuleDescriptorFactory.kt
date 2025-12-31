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
import kotlin.io.path.pathString

@Service
class ModuleDescriptorFactory {

    @Throws(HybrisConfigurationException::class)
    fun createDescriptor(importContext: ProjectImportContext.Mutable, moduleRoot: ModuleRoot): ModuleDescriptor {
        val realPath = moduleRoot.path
        val originalPath = moduleRoot.path.pathString
        val newPath = realPath.pathString
        val path = if (originalPath != newPath) "$originalPath($newPath)"
        else originalPath

        val context = ModuleDescriptorProviderContext(
            moduleRootDirectory = realPath.toFile(),
            project = importContext.project,
            externalExtensionsDirectory = importContext.externalExtensionsDirectory,
            moduleRoot = moduleRoot,
        )

        return ModuleDescriptorProvider.EP.extensionList
            .firstOrNull { it.isApplicable(context) }
            ?.let { provider ->
                thisLogger().info("Creating '${provider.javaClass.name}' module for $path")
                provider.create(context)
            }
            ?: throw HybrisConfigurationException("Could not find suitable module descriptor provider for $path")
    }

    @Throws(HybrisConfigurationException::class)
    fun createRootDescriptor(importContext: ProjectImportContext.Mutable, moduleRoot: ModuleRoot): RootModuleDescriptor {
//        resolvePath(importContext, moduleRoot)

        return RootModuleDescriptor(moduleRoot)
    }

    // TODO: resolution of the Symliks and normalization of the Path MAY be done during ModuleRoots scanning
//    private fun resolvePath(importContext: ProjectImportContext.Mutable, moduleRoot: ModuleRoot): Path = try {
//        if (importContext.settings.followSymlink) moduleRoot.path.toRealPath()
//        else moduleRoot.path.toRealPath(LinkOption.NOFOLLOW_LINKS)
//    } catch (e: IOException) {
//        throw HybrisConfigurationException(e)
//    }
//        .takeIf { it.directoryExists }
//        ?: throw HybrisConfigurationException("Can not find module directory using path: ${moduleRoot.path}")

    companion object {
        fun getInstance(): ModuleDescriptorFactory = application.service()
    }
}