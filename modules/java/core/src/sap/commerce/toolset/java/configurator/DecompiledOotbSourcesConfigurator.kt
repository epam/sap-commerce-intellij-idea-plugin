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

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.progress.checkCanceled
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.jps.entities.modifyLibraryEntity
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.application
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.java.decompilation.DecompilerService
import sap.commerce.toolset.java.decompilation.JarDecompileContext
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.fromPath
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet
import java.io.File
import java.util.jar.JarFile

/**
 * Decompile OOTB bin jars into doc/decompiledsrc and attach as additional sources.
 */
class DecompiledOotbSourcesConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "OOTB Decompiled Sources"

    override suspend fun configure(context: ProjectPostImportContext) {
        val importSettings = context.settings

        if (!importSettings.withDecompiledOotbSources) return
        if (Plugin.JAVA_DECOMPILER.isDisabled()) return

        val project = context.project

        val decompilerService = DecompilerService.getInstance()

        if (!decompilerService.isConsentGranted()) {
            var consentAccepted = false
            application.invokeAndWait({
                consentAccepted = decompilerService.ensureConsentAccepted()
            }, ModalityState.defaultModalityState())

            if (!consentAccepted) return
        }

        val contexts = collectDecompileContexts(project)
            .ifEmpty { return }

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            withBackgroundProgress(project, "Decompiling OOTB binaries...", true) {
                val logger = thisLogger()
                logger.info("Decompiling ${contexts.size} OOTB bin jars")
                val decompileResults: List<DecompileResult> = withContext(Dispatchers.IO) {
                    reportProgressScope(contexts.size) { progress ->
                        contexts.map { decompileContext ->
                            async {
                                ensureActive()
                                logger.debug("Decompiling ${decompileContext.jar.name}")
                                progress.itemStep("Decompiling ${decompileContext.jar.name}") {
                                    checkCanceled()
                                    runCatching { decompileJar(decompileContext, decompilerService) }
                                        .onFailure { logger.warn("Decompilation failed for ${decompileContext.jar}", it) }
                                        .getOrElse { DecompileResult.Failed }
                                }
                            }
                        }.awaitAll()
                    }
                }

                val successfulContexts = contexts.zip(decompileResults)
                    .filter { it.second == DecompileResult.Completed }
                    .map { it.first }

                runCatching {
                    attachDecompiledSources(context, successfulContexts)
                }.onFailure {
                    if (it is CancellationException) throw it
                    logger.warn("Failed to attach decompiled sources", it)
                }

                val completed = decompileResults.count { it == DecompileResult.Completed }
                val failed = decompileResults.count { it == DecompileResult.Failed }
                val skipped = decompileResults.count { it == DecompileResult.Skipped }

                if (completed > 0 || failed > 0 || skipped > 0) {
                    logger.info("Decompiled sources: $completed succeeded, $failed failed")
                    val details = buildList {
                        if (completed > 0) add("Completed: $completed")
                        if (failed > 0) add("Failed: $failed")
                        if (skipped > 0) add("Skipped: $skipped")
                    }.joinToString(", ")
                    Notifications.info(
                        "OOTB decompiled sources finished",
                        "$details. Some classes may fail to decompile, but sources will still be generated and attached when possible. Output: 'doc/decompiledsrc' folder in each module."
                    ).hideAfter(10).notify(project)
                }
            }
        }
    }

    private fun collectDecompileContexts(project: Project): List<JarDecompileContext> {
        return ModuleManager.getInstance(project).modules
            .flatMap { module ->
                val facet = YFacet.get(module) ?: return@flatMap emptyList()
                val descriptor = facet.configuration.state ?: return@flatMap emptyList()

                if (descriptor.type != ModuleDescriptorType.OOTB &&
                    descriptor.type != ModuleDescriptorType.PLATFORM &&
                    descriptor.type != ModuleDescriptorType.EXT
                ) return@flatMap emptyList()

                val moduleRoot = descriptor.path.takeIf { it.isNotEmpty() }?.let { File(it) } ?: return@flatMap emptyList()
                val hasSources = ProjectConstants.Directory.SRC_DIR_NAMES
                    .asSequence()
                    .map { File(moduleRoot, it) }
                    .filter { it.isDirectory }
                    .flatMap { (it.listFiles() ?: emptyArray()).asSequence() }
                    .any()

                if (hasSources) return@flatMap emptyList()

                val binDir = File(moduleRoot, ProjectConstants.Directory.BIN)
                if (!binDir.isDirectory) return@flatMap emptyList()

                val targetRoot = File(moduleRoot, "doc/decompiledsrc")
                binDir.listFiles { _, name: String -> name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
                    ?.map { jar -> JarDecompileContext(module.name, jar, targetRoot) }
                    ?: emptyList()
            }
    }

    private suspend fun decompileJar(context: JarDecompileContext, decompilerService: DecompilerService): DecompileResult {
        val logger = thisLogger()
        val jar = context.jar
        val targetRoot = context.targetRoot

        val outputRoot = File(targetRoot, jar.nameWithoutExtension)
        val marker = File(outputRoot, ".timestamp")
        val markerValue = jarMarkerValue(jar)

        val previousMarkerValue = try {
            marker.takeIf { it.isFile }?.readText()
        } catch (_: Exception) {
            null
        }
        if (outputRoot.isDirectory && previousMarkerValue == markerValue) {
            return DecompileResult.Skipped
        }

        val localJarFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(jar)
            ?: return DecompileResult.Failed
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(localJarFile)
            ?: return DecompileResult.Failed

        if (outputRoot.exists()) FileUtil.delete(outputRoot)
        outputRoot.mkdirs()

        var decompiledCount = 0
        var failedCount = 0

        withContext(Dispatchers.IO) {
            JarFile(jar).use { jf ->
                val entries = jf.entries()
                while (entries.hasMoreElements()) {
                    checkCanceled()
                    val entry = entries.nextElement()
                    if (!entry.name.endsWith(".class")) continue
                    val vFile = jarRoot.findFileByRelativePath(entry.name) ?: continue

                    val source = runCatching { decompilerService.decompile(vFile) }
                        .onFailure {
                            failedCount++
                            logger.debug("Failed to decompile ${vFile.path}", it)
                        }
                        .getOrNull()
                        ?: continue

                    val outFile = File(outputRoot, entry.name.removeSuffix(".class") + ".java")
                    outFile.parentFile.mkdirs()
                    outFile.writeText(source)
                    decompiledCount++
                }
            }
        }

        if (decompiledCount == 0) {
            logger.warn("No classes could be decompiled for ${jar.path} (failed=$failedCount)")
            FileUtil.delete(outputRoot)
            return DecompileResult.Failed
        }

        if (failedCount > 0) {
            logger.info("Partially decompiled ${jar.path}: ok=$decompiledCount, failed=$failedCount")
        }

        marker.writeText(markerValue)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputRoot)
        return DecompileResult.Completed
    }

    private fun jarMarkerValue(jar: File): String = "${jar.lastModified()}:${jar.length()}"

    private suspend fun attachDecompiledSources(
        context: ProjectPostImportContext,
        contexts: List<JarDecompileContext>
    ) {
        if (contexts.isEmpty()) return

        val logger = thisLogger()
        val vfUrlManager = context.workspace.getVirtualFileUrlManager()

        val moduleLibraryEntities = context.storage.entities<LibraryEntity>()
            .filter { it.typeId == ProjectConstants.Workspace.yLibraryTypeId }
            .filter { it.tableId is LibraryTableId.ModuleLibraryTableId }
            .toList()

        val contextsByJarPath = contexts
            .distinctBy { it.jar.canonicalPathOrPath() }
            .associateBy { it.jar.canonicalPathOrPath() }

        val updatesByLibraryId = moduleLibraryEntities.mapNotNull { libraryEntity ->
            checkCanceled()

            val moduleName = (libraryEntity.tableId as? LibraryTableId.ModuleLibraryTableId)
                ?.moduleId
                ?.name
                ?: return@mapNotNull null

            val rootsToAttach: List<LibraryRoot> = buildList {
                val compiledJarRoots = libraryEntity.roots
                    .asSequence()
                    .mapNotNull { it.toCompiledJarFilePathOrNull() }
                    .toList()

                compiledJarRoots.forEach { jarPath ->
                    val decompileContext = contextsByJarPath[jarPath] ?: return@forEach
                    if (decompileContext.moduleName != moduleName) return@forEach

                    val outputRoot = File(decompileContext.targetRoot, decompileContext.jar.nameWithoutExtension)
                        .takeIf { it.isDirectory }
                        ?: return@forEach

                    val libraryRoot = outputRoot.toSourcesLibraryRoot(vfUrlManager)
                        ?: return@forEach

                    val alreadyAttached = libraryEntity.roots.any { existing ->
                        existing.type == LibraryRootTypeId.SOURCES && existing.url == libraryRoot.url
                    }
                    if (!alreadyAttached) add(libraryRoot)
                }
            }

            if (rootsToAttach.isEmpty()) return@mapNotNull null

            logger.debug("Attaching ${rootsToAttach.size} decompiled roots to library '${libraryEntity.name}'")

            LibraryId(libraryEntity.name, libraryEntity.tableId) to rootsToAttach
        }.toMap()

        if (updatesByLibraryId.isEmpty()) return

        context.workspace.update("Attaching decompiled sources") { storage ->
            storage.entities<LibraryEntity>().forEach { entity ->
                val newRoots = updatesByLibraryId[LibraryId(entity.name, entity.tableId)] ?: return@forEach
                storage.modifyLibraryEntity(entity) {
                    this.roots += newRoots
                }
            }
        }
    }

    private data class LibraryId(
        val name: String,
        val tableId: LibraryTableId
    )

    private fun LibraryRoot.toCompiledJarFilePathOrNull(): String? {
        if (type != LibraryRootTypeId.COMPILED) return null

        val rawPath = VfsUtil.urlToPath(url.url)
        val localPath = rawPath
            .substringBefore('!')
            .removeSuffix("/")
            .removeSuffix("\\")
            .takeIf { it.endsWith(".jar") }
            ?: return null

        return File(localPath).canonicalPathOrPath()
    }

    private fun File.canonicalPathOrPath(): String = try {
        canonicalPath
    } catch (_: Exception) {
        path
    }

    private fun File.toSourcesLibraryRoot(vfUrlManager: VirtualFileUrlManager): LibraryRoot? {
        val url = vfUrlManager.fromPath(toPath()) ?: return null
        return LibraryRoot(url, LibraryRootTypeId.SOURCES)
    }

    private enum class DecompileResult {
        Completed,
        Failed,
        Skipped
    }
}
