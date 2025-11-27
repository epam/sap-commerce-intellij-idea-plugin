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

package sap.commerce.toolset.java.configurator

import com.intellij.ide.highlighter.ArchiveFileType
import com.intellij.jarFinder.SonatypeSourceSearcher
import com.intellij.jarFinder.SourceSearcher
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.jps.entities.modifyLibraryEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.SystemProperties
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.*
import sap.commerce.toolset.java.jarFinder.SonatypeCentralSourceSearcher
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.io.File
import java.io.IOException

class JavaLibrarySourcesConfigurator : ProjectPostImportConfigurator {

    private val artifactIdentifier = "[A-Za-z0-9.\\-_]+".toRegex()

    override val name = "Libraries sources configurator"

    override suspend fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return

        val libSourceDir = getLibrarySourceDir() ?: return

        val workspaceModel = WorkspaceModel.getInstance(project)
        val storage = workspaceModel.currentSnapshot
        val libraries = storage.entities(LibraryEntity::class.java).toList()
        val sourceSearchers = arrayOf(SonatypeCentralSourceSearcher(), SonatypeSourceSearcher())

        val task = object : Task.Backgroundable(project, "Downloading workspace library sources", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false

                // tie coroutines lifetime to this background task
                runBlocking {
                    withBackgroundProgress(project, "Fetching libraries sources...", true) {
                        reportProgressScope(libraries.size) { reporter ->
                            supervisorScope {
                                val jobs = libraries.map { library ->
                                    async(Dispatchers.IO) {
                                        checkCanceled()

                                        reporter.itemStep("Fetching sources for library '${library.name}'...") {
                                            val sourceRoots = processLibrary(project, libSourceDir, indicator, library, sourceSearchers)

                                            if (sourceRoots.isNotEmpty()) {
                                                edtWriteAction {
                                                    workspaceModel.updateProjectModel("Updating Library ${library.name} sources") { builder ->
                                                        builder.modifyLibraryEntity(library) {
                                                            this.roots.addAll(sourceRoots)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                jobs.awaitAll()
                            }
                        }
                    }
                }
            }
        }

        ProgressManager.getInstance().run(task)
    }

    private suspend fun processLibrary(
        project: Project,
        libSourceDir: File,
        indicator: ProgressIndicator,
        library: LibraryEntity,
        sourceSearchers: Array<SourceSearcher>
    ): List<LibraryRoot> = library.roots
        .map { root -> processRoot(project, libSourceDir, indicator, root, sourceSearchers) }
        .flatten()

    private suspend fun processRoot(
        project: Project,
        libSourceDir: File,
        indicator: ProgressIndicator,
        root: LibraryRoot,
        sourceSearchers: Array<SourceSearcher>
    ): Collection<LibraryRoot> {
        val libraryJars = root
            .takeIf { it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY }
            ?.url?.url
            ?.takeIf { it.endsWith("/lib") }
            ?.let { VirtualFileManager.getInstance().findFileByUrl(it) }
            ?.children
            ?.filter { it.fileType == ArchiveFileType.INSTANCE }
            ?: return emptyList()

        return reportProgressScope(libraryJars.size) { reporter ->
            libraryJars.map { libraryJar ->
                async(Dispatchers.IO) {
                    reporter.itemStep("Fetching sources for '${libraryJar.nameWithoutExtension}'...") {
                        processLibraryJar(project, libSourceDir, indicator, reporter, libraryJar, sourceSearchers)
                    }
                }
            }
        }
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun processLibraryJar(
        project: Project,
        libSourceDir: File,
        indicator: ProgressIndicator,
        reporter: ProgressReporter,
        libraryJar: VirtualFile,
        sourceSearchers: Array<SourceSearcher>
    ): LibraryRoot? {
        checkCanceled()
        val vfUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()

        val coords = parse(libraryJar)
            ?.takeIf { artifactIdentifier.matches(it.artifactId) && artifactIdentifier.matches(it.version) }
            ?: return null
        val jarName = libraryJar.nameWithoutExtension
        val targetFileName = "$jarName-sources.jar"
        val targetPath = libSourceDir.toPath().resolve(targetFileName)
        val targetFile = targetPath.toFile()

        val existingLibraryRoot = toLibraryRoot(targetFile, vfUrlManager)

        // if already downloaded, attach immediately
        if (existingLibraryRoot != null) return existingLibraryRoot

        val artifactUrl = sourceSearchers.firstNotNullOfOrNull {
            try {
                it.findSourceJar(indicator, coords.artifactId, coords.version, libraryJar)
            } catch (_: Exception) {
                null
            }
        } ?: return null

        return downloadDependency(targetFileName, libSourceDir, artifactUrl, indicator, targetFile, vfUrlManager)
    }

    private fun downloadDependency(
        targetFileName: String,
        libSourceDir: File,
        artifactUrl: String,
        indicator: ProgressIndicator,
        targetFile: File,
        vfUrlManager: VirtualFileUrlManager
    ): LibraryRoot? {
        try {
            val tmp = File.createTempFile("download_$targetFileName", ".tmp", libSourceDir)

            HttpRequests.request(artifactUrl).saveToFile(tmp, indicator)

            if (!targetFile.exists()) {
                if (!tmp.renameTo(targetFile)) {
                    tmp.delete()
                }
            }
            if (targetFile.exists()) {
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager)

                if (libraryRoot != null) return libraryRoot
            }
        } catch (_: IOException) {
            //
        }

        return null
    }

    private fun toLibraryRoot(
        targetFile: File,
        vfUrlManager: VirtualFileUrlManager
    ): LibraryRoot? = targetFile
        .takeIf { it.exists() }
        ?.let { LocalFileSystem.getInstance().refreshAndFindFileByIoFile(it) }
        ?.let { JarFileSystem.getInstance().getJarRootForLocalFile(it) }
        ?.let { vfUrlManager.getOrCreateFromUrl(it.url) }
        ?.let { LibraryRoot(it, LibraryRootTypeId.SOURCES) }

    private fun getLibrarySourceDir(): File? {
        val path = System.getProperty("idea.library.source.dir")
        val libSourceDir = if (path != null) File(path)
        else File(SystemProperties.getUserHome(), ".ideaLibSources")

        return if (!libSourceDir.exists() && !libSourceDir.mkdirs()) null
        else libSourceDir
    }

    private fun parse(jar: VirtualFile): MavenCoords? = parsePath(jar) ?: parseName(jar)

    private fun parsePath(jar: VirtualFile): MavenCoords? {
        val jarName = jar.nameWithoutExtension
        val parent1 = jar.parent ?: return null
        val parent2 = parent1.parent ?: return null
        val artifactId = parent2.name
        val version = parent1.name
        val jarPathName = "$artifactId-$version"

        return if (jarPathName != jarName) null
        else MavenCoords(artifactId, version)
    }

    private fun parseName(jar: VirtualFile): MavenCoords? {
        val jarName = jar.nameWithoutExtension
        val idx = jarName.lastIndexOf('-')
        if (idx == -1) return null
        val version = jarName.substring(idx + 1)
        val artifactId = jarName.take(idx)
        return MavenCoords(artifactId, version)
    }

    private data class MavenCoords(val artifactId: String, val version: String)

}