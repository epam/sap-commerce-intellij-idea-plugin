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

import com.intellij.ide.BrowserUtil
import com.intellij.jarRepository.RemoteRepositoriesConfiguration
import com.intellij.jarRepository.settings.RemoteRepositoriesConfigurable
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.ShowSettingsUtil
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
import com.intellij.util.io.delete
import kotlinx.coroutines.*
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.java.jarFinder.LibraryRootLookup
import sap.commerce.toolset.java.jarFinder.LibraryRootLookupScope
import sap.commerce.toolset.java.jarFinder.LibraryRootLookupService
import sap.commerce.toolset.java.jarFinder.LibraryRootType
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class JavaLibrarySourcesConfigurator : ProjectPostImportConfigurator {

    override val name
        get() = "Libraries Sources"

    override fun postImport(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val libraryRootTypes = buildSet {
            if (importContext.settings.withExternalLibrarySources) add(LibraryRootType.SOURCES)
            if (importContext.settings.withExternalLibraryJavadocs) add(LibraryRootType.JAVADOC)
        }
            .takeIf { it.isNotEmpty() }
            ?: return

        val project = importContext.project
        val librarySourceDir = getLibrarySourceDir() ?: return
        val lookupRepositories = getLookupRepositories(project)

        if (lookupRepositories == null) {
            Notifications.warning(
                "Libraries sources cannot be downloaded",
                "Maven repositories are not configured, please specify them via 'Remote Jar Repositories' project settings and try again."
            )
                .addAction("Configure") { _, _ ->
                    invokeLater {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, RemoteRepositoriesConfigurable::class.java)
                    }
                }
                .system(true)
                .notify(project)
            return
        }

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            withBackgroundProgress(project, "Fetching libraries sources...", true) {
                supervisorScope {
                    val libraries = processLibraries(workspaceModel, lookupRepositories, librarySourceDir, libraryRootTypes)

                    updateLibraries(project, workspaceModel, libraries, librarySourceDir)
                }
            }
        }
    }

    private suspend fun processLibraries(
        workspaceModel: WorkspaceModel,
        lookupRepositories: List<String>,
        librarySourceDir: Path,
        libraryRootTypes: Set<LibraryRootType>
    ): Map<LibraryEntity, Collection<LibraryRootLookup>> {
        val libraryEntities = workspaceModel.currentSnapshot
            .entities(LibraryEntity::class.java)
            .toList()

        return reportProgressScope(libraryEntities.size) { reporter ->
            libraryEntities
                .map { libraryEntity ->
                    checkCanceled()

                    async {
                        libraryEntity to fetchSources(workspaceModel, lookupRepositories, librarySourceDir, libraryRootTypes, libraryEntity, reporter)
                    }
                }
                .awaitAll()
                .associate { it.first to it.second }
        }
    }

    private suspend fun updateLibraries(
        project: Project,
        workspaceModel: WorkspaceModel,
        libraries: Map<LibraryEntity, Collection<LibraryRootLookup>>,
        librarySourceDir: Path
    ) {
        checkCanceled()

        val librarySourceDirVf = backgroundWriteAction {
            workspaceModel.updateProjectModel("Updating libraries sources") { builder ->
                libraries.forEach { (libraryEntity, LibraryRootLookups) ->
                    val libraryRoots = LibraryRootLookups.mapNotNull { it.libraryRoot }
                    builder.modifyLibraryEntity(libraryEntity) {
                        this.roots += libraryRoots
                    }
                }
            }

            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(librarySourceDir)
        }
            ?: return

        val updatedLibraries = libraries.size
        val details = libraries.values.flatten().groupBy { it.type }
            .mapValues { (_, lookups) ->
                lookups
                    .groupBy { it.scope }
                    .mapValues { (_, scopedLookups) -> scopedLookups.size }
            }
            .map { (type, scopeCounts) ->
                val missing = scopeCounts[LibraryRootLookupScope.MISSING] ?: 0
                val cached = scopeCounts[LibraryRootLookupScope.CACHED] ?: 0
                val new = scopeCounts[LibraryRootLookupScope.NEW] ?: 0

                " - ${type.presentableName}: $cached cached, $new new, $missing missing"
            }
            .joinToString("<br>")

        Notifications.info(
            "Libraries sources successfully updated",
            """
                    Updated $updatedLibraries libraries with:<br>
                    $details
                """.trimIndent()
        )
            .addAction("Open Libraries Directory") { _, _ -> BrowserUtil.browse(librarySourceDirVf) }
            .system(true)
            .notify(project)
    }

    private suspend fun fetchSources(
        workspaceModel: WorkspaceModel,
        lookupRepositories: List<String>,
        librarySourceDir: Path,
        libraryRootTypes: Set<LibraryRootType>,
        library: LibraryEntity,
        reporter: ProgressReporter
    ): Collection<LibraryRootLookup> {
        checkCanceled()

        return reporter.itemStep("Fetching sources for library '${library.name}'...") {
            library.roots.flatMap { libraryRoot -> processLibraryRoot(workspaceModel, lookupRepositories, librarySourceDir, libraryRootTypes, libraryRoot) }
        }
    }

    private suspend fun processLibraryRoot(
        workspaceModel: WorkspaceModel,
        lookupRepositories: List<String>,
        librarySourceDir: Path,
        libraryRootTypes: Set<LibraryRootType>,
        libraryRoot: LibraryRoot
    ): Collection<LibraryRootLookup> {
        checkCanceled()

        val libraryJars = libraryRoot
            .takeIf { it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT || it.inclusionOptions == LibraryRoot.InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY }
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
                            fetchLibrarySourcesJars(workspaceModel, lookupRepositories, librarySourceDir, libraryRootTypes, libraryJar)
                        }
                    }
                }
            }
                .awaitAll()
                .flatten()
        }
    }

    private suspend fun fetchLibrarySourcesJars(
        workspaceModel: WorkspaceModel,
        lookupRepositories: List<String>,
        librarySourceDir: Path,
        libraryRootTypes: Set<LibraryRootType>,
        libraryJar: VirtualFile
    ): Collection<LibraryRootLookup> {
        checkCanceled()

        val vfUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRootLookups = libraryRootTypes
            .map { sourceType ->
                val targetFile = librarySourceDir
                    .resolve("${libraryJar.nameWithoutExtension}-${sourceType.mavenPostfix}.jar")
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager, sourceType)

                LibraryRootLookup(sourceType, targetFile, libraryRoot, scope = LibraryRootLookupScope.CACHED)
            }
        // identify not yet downloaded sources
        val missingLibraryRootLookups = libraryRootLookups.filter { it.libraryRoot == null }
            .onEach { it.scope = LibraryRootLookupScope.MISSING }

        // find and set urls for each not yet downloaded source jar
        LibraryRootLookupService.getService().findJarUrls(lookupRepositories, libraryJar, missingLibraryRootLookups)

        // download not yet downloaded source jars
        missingLibraryRootLookups.forEach { libraryRootLookup ->
            downloadSourceJar(librarySourceDir, vfUrlManager, libraryRootLookup)
                ?.let {
                    libraryRootLookup.libraryRoot = it
                    libraryRootLookup.scope = LibraryRootLookupScope.NEW
                }
        }

        // we're operating on the same objects, so it should be safe to return local
        return libraryRootLookups
    }

    private suspend fun downloadSourceJar(
        librarySourceDir: Path,
        vfUrlManager: VirtualFileUrlManager,
        libraryRootLookup: LibraryRootLookup
    ): LibraryRoot? {
        val artifactSourceUrl = libraryRootLookup.url ?: return null
        val targetFile = libraryRootLookup.targetFile
        val tmp = withContext(Dispatchers.IO) {
            Files.createTempFile(librarySourceDir, "download_${targetFile.nameWithoutExtension}", ".tmp")
        }

        try {
            checkCanceled()

            reportProgressScope {
                it.itemStep("Downloading ${targetFile.name}") {
                    retryHttp {
                        HttpRequests
                            .request(artifactSourceUrl)
                            .saveToFile(tmp, null)
                    }
                }
            }

            if (!targetFile.fileExists) {
                runCatching {
                    withContext(Dispatchers.IO) {
                        Files.move(tmp, targetFile)
                    }
                }.also { if (it.isFailure) Files.delete(tmp) }
            }
            if (targetFile.fileExists) {
                val libraryRoot = toLibraryRoot(targetFile, vfUrlManager, libraryRootLookup.type)

                if (libraryRoot != null) return libraryRoot
            }
        } catch (e: Exception) {
            if (tmp.fileExists) tmp.delete()

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
        targetFile: Path,
        vfUrlManager: VirtualFileUrlManager,
        type: LibraryRootType,
    ): LibraryRoot? = targetFile
        .takeIf { it.fileExists }
        ?.let { LocalFileSystem.getInstance().refreshAndFindFileByNioFile(it) }
        ?.let { JarFileSystem.getInstance().getJarRootForLocalFile(it) }
        ?.let { vfUrlManager.getOrCreateFromUrl(it.url) }
        ?.let { LibraryRoot(it, type.id) }

    private fun getLibrarySourceDir(): Path? {
        val path = System.getProperty("idea.library.source.dir")
        val librarySourceDir = if (path != null) Path(path)
        else Path(SystemProperties.getUserHome(), ".ideaLibSources")

        return if (!librarySourceDir.directoryExists && !librarySourceDir.toFile().mkdirs()) null
        else librarySourceDir
    }

    private fun getLookupRepositories(project: Project): List<String>? = RemoteRepositoriesConfiguration.getInstance(project).repositories
        .map { it.url }
        .takeIf { it.isNotEmpty() }
}
