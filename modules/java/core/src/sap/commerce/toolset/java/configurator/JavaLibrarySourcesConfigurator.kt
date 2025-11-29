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
import com.intellij.openapi.diagnostic.thisLogger
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
import sap.commerce.toolset.java.jarFinder.LibraryRootLookup
import sap.commerce.toolset.java.jarFinder.LibraryRootType
import sap.commerce.toolset.java.jarFinder.SonatypeCentralSourceSearcher
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.io.File
import java.io.IOException

class JavaLibrarySourcesConfigurator : ProjectPostImportConfigurator {

    override val name
        get() = "Libraries Sources"

    override fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val libraryRootTypes = buildSet {
            if (hybrisProjectDescriptor.isWithExternalLibrarySources) add(LibraryRootType.SOURCES)
            if (hybrisProjectDescriptor.isWithExternalLibraryJavadocs) add(LibraryRootType.JAVADOC)
        }
            .takeIf { it.isNotEmpty() }
            ?: return

        val project = hybrisProjectDescriptor.project ?: return
        val libSourceDir = getLibrarySourceDir() ?: return

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            withBackgroundProgress(project, "Fetching libraries sources...", true) {
                supervisorScope {
                    val workspaceModel = WorkspaceModel.getInstance(project)
                    val libraries = processLibraries(project, workspaceModel, libSourceDir, libraryRootTypes)

                    updateLibraries(workspaceModel, libraries)
                }
            }
        }
    }

    private suspend fun processLibraries(
        project: Project,
        workspaceModel: WorkspaceModel,
        libSourceDir: File,
        libraryRootTypes: Set<LibraryRootType>
    ): Map<LibraryEntity, Collection<LibraryRoot>> {
        val libraryEntities = smartReadAction(project) {
            workspaceModel.currentSnapshot
                .entities(LibraryEntity::class.java)
                .toList()
        }

        return reportProgressScope(libraryEntities.size) { reporter ->
            libraryEntities
                .map { libraryEntity ->
                    async {
                        libraryEntity to fetchSources(project, libSourceDir, libraryRootTypes, libraryEntity, reporter)
                    }
                }
                .awaitAll()
                .associate { it.first to it.second }
        }
    }

    private suspend fun updateLibraries(workspaceModel: WorkspaceModel, libraries: Map<LibraryEntity, Collection<LibraryRoot>>) {
        checkCanceled()

        edtWriteAction {
            workspaceModel.updateProjectModel("Updating libraries sources") { builder ->
                libraries.forEach { (libraryEntity, libraryRoots) ->
                    builder.modifyLibraryEntity(libraryEntity) {
                        this.roots.addAll(libraryRoots)
                    }
                }
            }
        }
    }

    private suspend fun fetchSources(
        project: Project,
        libSourceDir: File,
        libraryRootTypes: Set<LibraryRootType>,
        library: LibraryEntity,
        reporter: ProgressReporter
    ): Collection<LibraryRoot> {
        checkCanceled()

        return reporter.itemStep("Fetching sources for library '${library.name}'...") {
            library.roots
                .map { libraryRoot -> processLibraryRoot(project, libSourceDir, libraryRootTypes, libraryRoot) }
                .flatten()
        }
    }

    private suspend fun processLibraryRoot(
        project: Project,
        libSourceDir: File,
        libraryRootTypes: Set<LibraryRootType>,
        libraryRoot: LibraryRoot
    ): Collection<LibraryRoot> {
        checkCanceled()

        val libraryJars = libraryRoot
            .takeIf { it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY }
            ?.url?.url
            ?.takeIf { it.endsWith("/lib") || it.endsWith("/lib/dbdriver") }
            ?.let { VirtualFileManager.getInstance().findFileByUrl(it) }
            ?.children
            ?.filter { it.extension == "jar" }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        return supervisorScope {
            reportProgressScope(libraryJars.size) { reporter ->
                libraryJars.map { libraryJar ->
                    async {
                        reporter.itemStep("Fetching sources for '${libraryJar.nameWithoutExtension}'...") {
                            fetchLibrarySourcesJars(project, libSourceDir, libraryRootTypes, libraryJar)
                        }
                    }
                }
            }
                .awaitAll()
                .flatten()
        }
    }

    private suspend fun fetchLibrarySourcesJars(
        project: Project,
        libSourceDir: File,
        libraryRootTypes: Set<LibraryRootType>,
        libraryJar: VirtualFile
    ): Collection<LibraryRoot> {
        checkCanceled()

        val vfUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
        val libraryRootLookups = libraryRootTypes
            .map { sourceType ->
                val targetFile = libSourceDir.toPath()
                    .resolve("${libraryJar.nameWithoutExtension}-${sourceType.mavenPostfix}.jar")
                    .toFile()
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager, sourceType)

                LibraryRootLookup(sourceType, targetFile, libraryRoot)
            }
        // identify not yet downloaded sources
        val missingLibraryRootLookups = libraryRootLookups.filter { it.libraryRoot == null }

        // find and set urls for each not yet downloaded source jar
        SonatypeCentralSourceSearcher.getService().findSourceJarUrls(libraryJar, missingLibraryRootLookups)

        // download not yet downloaded source jars
        missingLibraryRootLookups.forEach {
            downloadSourceJar(libSourceDir, vfUrlManager, it)
        }

        // we're operating on the same objects, so it should be safe to return local
        return libraryRootLookups.mapNotNull { it.libraryRoot }
    }

    private suspend fun downloadSourceJar(
        libSourceDir: File,
        vfUrlManager: VirtualFileUrlManager,
        libraryRootLookup: LibraryRootLookup
    ): LibraryRoot? {
        val artifactSourceUrl = libraryRootLookup.url ?: return null
        val targetFile = libraryRootLookup.targetFile
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
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager, libraryRootLookup.type)

                if (libraryRoot != null) return libraryRoot
            }
        } catch (e: Exception) {
            if (tmp.exists()) tmp.delete()

            thisLogger().debug("Failed to download ${targetFile.nameWithoutExtension}", e)
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
            } catch (_: IOException) {
                // retry
            } catch (e: Exception) {
                throw e
            }

            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
        }

        return block()
    }

    private fun toLibraryRoot(
        targetFile: File,
        vfUrlManager: VirtualFileUrlManager,
        type: LibraryRootType,
    ): LibraryRoot? = targetFile
        .takeIf { it.exists() }
        ?.let { LocalFileSystem.getInstance().refreshAndFindFileByIoFile(it) }
        ?.let { JarFileSystem.getInstance().getJarRootForLocalFile(it) }
        ?.let { vfUrlManager.getOrCreateFromUrl(it.url) }
        ?.let { LibraryRoot(it, type.id) }

    private fun getLibrarySourceDir(): File? {
        val path = System.getProperty("idea.library.source.dir")
        val libSourceDir = if (path != null) File(path)
        else File(SystemProperties.getUserHome(), ".ideaLibSources")

        return if (!libSourceDir.exists() && !libSourceDir.mkdirs()) null
        else libSourceDir
    }
}
