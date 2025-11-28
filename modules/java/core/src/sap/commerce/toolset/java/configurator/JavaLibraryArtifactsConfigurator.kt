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
import com.intellij.openapi.application.smartReadAction
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
                supervisorScope {
                    processLibraries(project, libSourceDir)
                }
            }
        }
    }

    private suspend fun processLibraries(project: Project, libSourceDir: File) {
        val workspaceModel = WorkspaceModel.getInstance(project)
        val libraryEntities = smartReadAction(project) {
            workspaceModel.currentSnapshot
                .entities(LibraryEntity::class.java)
                .toList()
        }

        reportProgressScope(libraryEntities.size) { reporter ->
            libraryEntities.map { library ->
                async {
                    fetchSources(project, libSourceDir, library, reporter)
                        ?.let { updateLibrary(workspaceModel, library, it) }
                }
            }.awaitAll()
        }
    }

    private suspend fun updateLibrary(
        workspaceModel: WorkspaceModel,
        libraryEntity: LibraryEntity,
        sourceRoots: Collection<LibraryRoot>
    ) {
        checkCanceled()

        edtWriteAction {
            workspaceModel.updateProjectModel("Updating Library ${libraryEntity.name} ${artifactType.presentationName}") { builder ->
                builder.modifyLibraryEntity(libraryEntity) {
                    this.roots.addAll(sourceRoots)
                }
            }
        }
    }

    private suspend fun fetchSources(project: Project, libSourceDir: File, library: LibraryEntity, reporter: ProgressReporter): List<LibraryRoot>? {
        checkCanceled()

        return reporter.itemStep("Fetching ${artifactType.presentationName} for library '${library.name}'...") {
            library.roots
                .map { libraryRoot -> processRoot(project, libSourceDir, libraryRoot) }
                .flatten()
                .takeIf { it.isNotEmpty() }
        }
    }

    private suspend fun processRoot(project: Project, libSourceDir: File, libraryRoot: LibraryRoot): Collection<LibraryRoot> {
        checkCanceled()

        val libraryJars = libraryRoot
            .takeIf { it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY }
            ?.url?.url
            ?.takeIf { it.endsWith("/lib") }
            ?.let { VirtualFileManager.getInstance().findFileByUrl(it) }
            ?.children
            ?.filter { it.extension == "jar" }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        return supervisorScope {
            reportProgressScope(libraryJars.size) { reporter ->
                libraryJars.map { libraryJar ->
                    async {
                        reporter.itemStep("Fetching ${artifactType.presentationName} for '${libraryJar.nameWithoutExtension}'...") {
                            fetchLibrarySourcesJar(project, libSourceDir, libraryJar)
                        }
                    }
                }
            }
                .awaitAll()
                .filterNotNull()
        }
    }

    private suspend fun fetchLibrarySourcesJar(project: Project, libSourceDir: File, libraryJar: VirtualFile): LibraryRoot? {
        checkCanceled()

        val vfUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
        val resourceFile = libSourceDir.toPath()
            .resolve("${libraryJar.nameWithoutExtension}-${artifactType.mavenPostfix}.jar")
            .toFile()

        // if already downloaded, attach immediately
        return toLibraryRoot(resourceFile, vfUrlManager)
            ?: SonatypeCentralSourceSearcher.getService().findSourceJar(libraryJar, artifactType)
                ?.let { downloadDependency(libSourceDir, resourceFile, it, vfUrlManager) }
    }

    private suspend fun downloadDependency(
        libSourceDir: File,
        targetFile: File,
        artifactSourceUrl: String,
        vfUrlManager: VirtualFileUrlManager
    ): LibraryRoot? {
        val tmp = File.createTempFile("download_${targetFile.nameWithoutExtension}", ".tmp", libSourceDir)
        try {
            checkCanceled()

            reportProgressScope() {
                it.itemStep("Downloading ${targetFile.name}") {
                    retryHttp {
                        HttpRequests
                            .request(artifactSourceUrl)
                            .saveToFile(tmp, null)
                    }
                }
            }

            if (!targetFile.exists()) {
                if (!tmp.renameTo(targetFile)) {
                    tmp.delete()
                }
            }
            if (targetFile.exists()) {
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager)

                if (libraryRoot != null) return libraryRoot
            }
        } catch (e: Exception) {
            if (tmp.exists()) tmp.delete()

            println("download error: $artifactSourceUrl, ${e.stackTraceToString()}")
        }

        return null
    }

    suspend fun <T> retryHttp(
        times: Int = 3,
        initialDelayMs: Long = 250,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs

        repeat(times - 1) {
            try {
                return block()
            } catch (e: IOException) {
                println("interrupted -> ${e.message}")
                // retry
            } catch (e: Exception) {
                println("exception -> ${e.message}")
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
        ?.let { LibraryRoot(it, artifactType.libraryTypeId) }

    private fun getLibrarySourceDir(): File? {
        val path = System.getProperty("idea.library.source.dir")
        val libSourceDir = if (path != null) File(path)
        else File(SystemProperties.getUserHome(), ".ideaLibSources")

        return if (!libSourceDir.exists() && !libSourceDir.mkdirs()) null
        else libSourceDir
    }
}
