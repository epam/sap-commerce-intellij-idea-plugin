/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.java.configurator.library

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.workspace.jps.entities.LibraryEntityBuilder
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.configurator.ProjectLibraryConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YHacExtModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor

class HacProjectLibraryConfigurator : ProjectLibraryConfigurator {

    override val name: String
        get() = JavaConstants.ProjectLibrary.HAC

    override suspend fun configure(context: ProjectImportContext): LibraryEntityBuilder? {
        val workspace = context.workspace
        val hacWebModuleDescriptor = context.chosenHybrisModuleDescriptors
            .filterIsInstance<YHacExtModuleDescriptor>()
            .firstOrNull()
            ?.getSubModules()
            ?.filterIsInstance<YWebSubModuleDescriptor>()
            ?.firstOrNull()

        if (hacWebModuleDescriptor == null) {
            thisLogger().debug("Project library '${JavaConstants.ProjectLibrary.HAC}' will not be created because ${EiConstants.Extension.HAC} extension is not used.")
            workspace.removeProjectLibrary(JavaConstants.ProjectLibrary.HAC)
            return null
        }

        val virtualFileUrlManager = workspace.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            addAll(hacWebModuleDescriptor.webRootClasses(virtualFileUrlManager))
            addAll(hacWebModuleDescriptor.webRootJars(virtualFileUrlManager))
            addAll(hacWebModuleDescriptor.docSources(virtualFileUrlManager))
        }

        return context.project.configureProjectLibrary(
            libraryName = JavaConstants.ProjectLibrary.HAC,
            libraryRoots = libraryRoots,
        )
    }
}