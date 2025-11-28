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

import com.intellij.openapi.application.edtWriteAction
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

abstract class JavaLibraryArtifactsConfigurator(private val artifactType: ArtifactType) : ProjectPostImportConfigurator {

    protected abstract fun shouldProcess(hybrisProjectDescriptor: HybrisProjectDescriptor): Boolean

    override fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        if (!shouldProcess(hybrisProjectDescriptor)) return
        val project = hybrisProjectDescriptor.project ?: return

        val libSourceDir = getLibrarySourceDir() ?: return

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            withBackgroundProgress(project, "Fetching libraries ${artifactType.presentationName}...", true) {
                fetch(project, libSourceDir)
            }
        }
    }

    private suspend fun fetch(project: Project, libSourceDir: File) {
        val workspaceModel = WorkspaceModel.getInstance(project)
        val storage = workspaceModel.currentSnapshot
        val libraries = storage.entities(LibraryEntity::class.java).toList()
        val sourceSearcher = SonatypeCentralSourceSearcher.getService()

        supervisorScope {
            reportProgressScope(libraries.size) { reporter ->
                libraries.map { library ->
                    async(Dispatchers.IO) {
                        fetchLibrary(reporter, library, project, libSourceDir, sourceSearcher, workspaceModel)
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun fetchLibrary(
        reporter: ProgressReporter,
        library: LibraryEntity,
        project: Project,
        libSourceDir: File,
        sourceSearcher: SonatypeCentralSourceSearcher,
        workspaceModel: WorkspaceModel
    ) {
        checkCanceled()

        reporter.itemStep("Fetching ${artifactType.presentationName} for library '${library.name}'...") {
            val sourceRoots = processLibrary(project, libSourceDir, library, sourceSearcher)

            if (sourceRoots.isNotEmpty()) {
                checkCanceled()

                edtWriteAction {
                    workspaceModel.updateProjectModel("Updating Library ${library.name} ${artifactType.presentationName}") { builder ->
                        builder.modifyLibraryEntity(library) {
                            this.roots.addAll(sourceRoots)
                        }
                    }
                }
            }
        }
    }

    private suspend fun processLibrary(
        project: Project,
        libSourceDir: File,
        library: LibraryEntity,
        sourceSearcher: SonatypeCentralSourceSearcher
    ): List<LibraryRoot> = library.roots
        .map { root -> processRoot(project, libSourceDir, root, sourceSearcher) }
        .flatten()

    private suspend fun processRoot(
        project: Project,
        libSourceDir: File,
        root: LibraryRoot,
        sourceSearcher: SonatypeCentralSourceSearcher
    ): Collection<LibraryRoot> {
        val libraryJars = root
            .takeIf { it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY }
            ?.url?.url
            ?.takeIf { it.endsWith("/lib") }
            ?.let { VirtualFileManager.getInstance().findFileByUrl(it) }
            ?.children
//            ?.filter { it.fileType == ArchiveFileType.INSTANCE } -> this is very slow
            ?.filter { it.extension == "jar" }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        return supervisorScope {
            reportProgressScope(libraryJars.size) { reporter ->
                libraryJars.map { libraryJar ->
                    async(Dispatchers.IO) {
                        reporter.itemStep("Fetching ${artifactType.presentationName} for '${libraryJar.nameWithoutExtension}'...") {
                            processLibraryJar(project, libSourceDir, libraryJar, sourceSearcher, reporter)
                        }
                    }
                }
            }
                .awaitAll()
                .filterNotNull()
        }
    }

    private suspend fun processLibraryJar(
        project: Project,
        libSourceDir: File,
        libraryJar: VirtualFile,
        sourceSearcher: SonatypeCentralSourceSearcher,
        reporter: ProgressReporter
    ): LibraryRoot? {
        checkCanceled()
        val vfUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
        val jarName = libraryJar.nameWithoutExtension
        val fileName = "$jarName-${artifactType.mavenPostfix}.jar"
        val resourceFile = libSourceDir.toPath().resolve(fileName).toFile()

        // if already downloaded, attach immediately
        return toLibraryRoot(resourceFile, vfUrlManager)
            ?: sourceSearcher.findSourceJar(libraryJar, artifactType)
                ?.let { downloadDependency(fileName, libSourceDir, it, resourceFile, vfUrlManager, reporter) }
    }

    private suspend fun downloadDependency(
        fileName: String,
        libSourceDir: File,
        artifactSourceUrl: String,
        targetFile: File,
        vfUrlManager: VirtualFileUrlManager,
        reporter: ProgressReporter
    ): LibraryRoot? {

        val tmp = File.createTempFile("download_$fileName", ".tmp", libSourceDir)
        try {
            checkCanceled()

//            reporter.itemStep("Downloading $artifactSourceUrl...") {
            HttpRequests
                .request(artifactSourceUrl)
                .readTimeout(5000)
                .connectTimeout(10000)
                .saveToFile(tmp, null)
//            }

            if (!targetFile.exists()) {
                if (!tmp.renameTo(targetFile)) {
                    tmp.delete()
                }
            }
            if (targetFile.exists()) {
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager)

                if (libraryRoot != null) return libraryRoot
            } else {
                println("WHYYYYY? -> $targetFile")
            }
        } catch (e: Exception) {
            if (tmp.exists()) tmp.delete()

            println("download error: $artifactSourceUrl, ${e.stackTraceToString()}")
        }

        return null
    }

    suspend fun <T> retryHttp(
        times: Int = 3,
        initialDelayMs: Long = 200,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs

        repeat(times - 1) {
            try {
                return block()
            } catch (_: IOException) {
                println("interrupted")
                // retry
            } catch (e: Exception) {
                throw e
            }

            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
        }

        return block() // final attempt
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
}
