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
package sap.commerce.toolset.maven.configurator

import com.intellij.openapi.application.readAction
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.idea.maven.buildtool.MavenSyncSpec
import org.jetbrains.idea.maven.model.MavenConstants
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.wizards.MavenProjectAsyncBuilder
import sap.commerce.toolset.maven.descriptor.MavenModuleDescriptor
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.io.File

class MavenConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Maven"

    override suspend fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val mavenModules = hybrisProjectDescriptor.chosenModuleDescriptors
            .filterIsInstance<MavenModuleDescriptor>()
            .takeIf { it.isNotEmpty() }
            ?: return

        val modules = readAction {
            mavenModules
                .asSequence()
                .map { it.moduleRootDirectory }
                .map { File(it, MavenConstants.POM_XML) }
                .filter { it.exists() && it.isFile }
                .mapNotNull { VfsUtil.findFileByIoFile(it, true) }
        }

        modules.forEach {
            MavenProjectAsyncBuilder().commitSync(project, it, null)
        }

        MavenProjectsManager.getInstance(project).scheduleUpdateAllMavenProjects(MavenSyncSpec.full("MavenProjectsManager.importProjects"))
    }
}
