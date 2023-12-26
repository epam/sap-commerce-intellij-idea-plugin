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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.maven.buildtool.MavenSyncConsole
import org.jetbrains.idea.maven.execution.SoutMavenConsole
import org.jetbrains.idea.maven.model.MavenArtifact
import org.jetbrains.idea.maven.model.MavenExplicitProfiles
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.*
import org.jetbrains.idea.maven.utils.MavenArtifactUtil
import java.io.File
import java.nio.file.Path

interface MavenUtils {
    companion object {
        fun resolveMavenJavadocs(
            modifiableRootModel: ModifiableRootModel,
            moduleDescriptor: ModuleDescriptor,
            progressIndicator: ProgressIndicator, appSettings: HybrisApplicationSettings
        ): List<String?> {
            if (appSettings.withMavenJavadocs) {
                return resolveMavenDependencies(modifiableRootModel, moduleDescriptor, progressIndicator, false, true)
            }
            return emptyList<String>()
        }

        fun resolveMavenSources(
            modifiableRootModel: ModifiableRootModel,
            moduleDescriptor: ModuleDescriptor,
            progressIndicator: ProgressIndicator, appSettings: HybrisApplicationSettings
        ): List<String?> {
            if (appSettings.withMavenSources) {
                return resolveMavenDependencies(modifiableRootModel, moduleDescriptor, progressIndicator, true, false)
            }
            return emptyList<String>()
        }

        private fun resolveMavenDependencies(
            modifiableRootModel: ModifiableRootModel,
            moduleDescriptor: ModuleDescriptor,
            progressIndicator: ProgressIndicator, downloadSources: Boolean, downloadDocs: Boolean
        ): List<String?> {
            val resultPathList: MutableList<String?> = ArrayList()

            val moduleDir = moduleDescriptor.moduleRootDirectory
            val mavenDescriptorFile = File(moduleDir, HybrisConstants.EXTERNAL_DEPENDENCIES_XML)
            if (mavenDescriptorFile.exists()) {
                val project = modifiableRootModel.project

                VfsUtil.findFileByIoFile(mavenDescriptorFile, false)?.let { mavenFile ->
                    val downloadResult: MavenArtifactDownloader.DownloadResult =
                        getDownloadResult(project, downloadSources, downloadDocs, mavenFile, progressIndicator)
                    val manager = MavenProjectsManager.getInstance(project)

                    if (downloadDocs) {
                        resultPathList.addAll(getArtifactLibs(manager, downloadResult.resolvedDocs, "-javadoc.jar"))
                    }
                    if (downloadSources) {
                        resultPathList.addAll(getArtifactLibs(manager, downloadResult.resolvedSources, "-sources.jar"))
                    }
                }

                resultPathList.sortBy { it }
            }
            return resultPathList
        }

        private fun getDownloadResult(
            project: Project,
            downloadSources: Boolean,
            downloadDocs: Boolean,
            mavenFile: VirtualFile,
            progressIndicator: ProgressIndicator
        ): MavenArtifactDownloader.DownloadResult {

            val embeddersManager = MavenEmbeddersManager(project)
            val mavenProjectsTree = MavenProjectsTree(project)
            val mavenConsole: MavenConsole = SoutMavenConsole()
            val mavenSyncConsole = MavenSyncConsole(project)

            val settings = MavenWorkspaceSettingsComponent.getInstance(project).settings
            val generalSettings = settings.generalSettings
            generalSettings.isNonRecursive = true

            val mavenProjectReaderResult =
                MavenProjectReader(project).readProject(generalSettings, mavenFile, mavenProjectsTree.explicitProfiles, mavenProjectsTree.projectLocator)

            val dependencies: List<MavenArtifact> = mavenProjectReaderResult.mavenModel.getDependencies()

            mavenProjectsTree.resetManagedFilesAndProfiles(
                listOf(mavenFile),
                MavenExplicitProfiles.NONE
            )
            mavenProjectsTree.updateAll(false, generalSettings, progressIndicator)

            mavenProjectsTree.projects[0].dependencies.addAll(dependencies)
            val mavenArtifactDownloader = MavenArtifactDownloader(project, mavenProjectsTree, null, progressIndicator, mavenSyncConsole)


            return mavenArtifactDownloader.downloadSourcesAndJavadocs(
                mavenProjectsTree.projects,
                downloadSources,
                downloadDocs,
                embeddersManager,
                mavenConsole
            )
        }

        private fun getArtifactLibs(manager: MavenProjectsManager, mavenIds: MutableSet<MavenId>, suffix: String) =
            mavenIds
                .map { getArtifactLib(manager, it) }
                .map { it.toAbsolutePath().toString() }
                .map { it.replace(".jar", suffix) }

        fun getArtifactLib(manager: MavenProjectsManager, mavenId: MavenId): Path = MavenArtifactUtil
            .getArtifactNioPath(
                manager.localRepository,
                mavenId.groupId,
                mavenId.artifactId,
                mavenId.version,
                "jar"
            )
    }
}
