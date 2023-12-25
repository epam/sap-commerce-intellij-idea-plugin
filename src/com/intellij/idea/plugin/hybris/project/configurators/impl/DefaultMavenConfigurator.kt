/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.project.configurators.MavenConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.MavenModuleDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.idea.maven.model.MavenConstants
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.wizards.MavenProjectBuilder
import java.io.File

class DefaultMavenConfigurator : MavenConfigurator {

    override fun configureAfterImport(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor
    ): List<() -> Unit> {
        val mavenModules = hybrisProjectDescriptor.modulesChosenForImport
            .filterIsInstance<MavenModuleDescriptor>()
            .takeIf { it.isNotEmpty() }
            ?: return emptyList()

        val mavenProjectFiles = mavenModules
            .map { it.moduleRootDirectory }
            .map { File(it, MavenConstants.POM_XML) }
            .filter { it.exists() && it.isFile }
            .mapNotNull { VfsUtil.findFileByIoFile(it, true) }

        val actions = mavenProjectFiles
            .map {
                val builder = MavenProjectBuilder()
                builder.isUpdate = MavenProjectsManager.getInstance(project).isMavenizedProject
                builder.setFileToImport(it)
                builder
            }
            .filter {
                val path = it.rootPath
                    ?.toAbsolutePath()
                    ?.toString()

                mavenModules.any { module: MavenModuleDescriptor -> module.moduleRootDirectory.absolutePath == path }
            }
            .map {
                {
                    try {
                        it.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER)
                    } finally {
                        it.cleanup()
                    }
                    Unit
                }
            }
            .toMutableList()

        actions.add() {
            MavenProjectsManager.getInstance(project).importProjects()
        }

        return actions
    }

}
