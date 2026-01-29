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

package sap.commerce.toolset.java.decompilation

import com.intellij.java.workspace.entities.javaSourceRoots
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.getModuleLibraries
import com.intellij.platform.workspace.jps.entities.modifyLibraryEntity
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jetbrains.java.decompiler.IdeaDecompiler
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.fromPath
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

class OotbDecompiledSourcesService {

    fun start(context: ProjectPostImportContext) {
        val project = context.project
        val logger = thisLogger()

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val consentAccepted = ensureConsentAccepted()
            if (!consentAccepted) return@launch

            withBackgroundProgress(project, "Decompiling OOTB binaries...", true) {
                val contexts = readAction { collectDecompileContexts(context) }
                    .ifEmpty { return@withBackgroundProgress }

                logger.debug("Decompiling ${contexts.size} OOTB bin jars")

                val ideaDecompiler = IdeaDecompiler()

                val decompileResults = supervisorScope {
                    reportProgressScope(contexts.size) { progress ->
                        contexts.map { decompileContext ->
                            async(Dispatchers.IO) {
                                progress.itemStep("Decompiling ${decompileContext.jar.name}") {
                                    checkCanceled()
                                    runCatching { decompileJar(decompileContext, ideaDecompiler) }
                                        .onFailure { logger.warn("Decompilation failed for ${decompileContext.jar.name}", it) }
                                        .getOrElse { DecompileResult.Failed }
                                }
                            }
                        }.awaitAll()
                    }
                }

                val contextsToAttach = contexts.zip(decompileResults)
                    .filter { it.second != DecompileResult.Failed }
                    .map { it.first }

                runCatching {
                    attachDecompiledSources(context, contextsToAttach)
                }.onFailure {
                    if (it is CancellationException) throw it
                    logger.warn("Failed to attach decompiled sources", it)
                }

                notifyFinished(project, decompileResults)
            }
        }
    }

    private suspend fun ensureConsentAccepted(): Boolean {
        val decompilerService = DecompilerService.getInstance()
        if (decompilerService.isConsentGranted()) return true

        val result = withContext(Dispatchers.EDT + ModalityState.defaultModalityState().asContextElement()) {
            decompilerService.ensureConsentAccepted()
        }

        return when (result) {
            DecompilerService.ConsentResult.Accepted -> true
            DecompilerService.ConsentResult.Postponed -> false
            DecompilerService.ConsentResult.Rejected -> {
                Notifications.warning(
                    "Idea Decompiler disabled",
                    "You rejected the Idea Decompiler legal notice, so the bundled 'Java Decompiler' plugin was disabled. " +
                        "Re-enable it in Settings | Plugins and re-import the project if you want OOTB sources decompilation."
                ).hideAfter(10).notify(null)
                false
            }
        }
    }

    private fun collectDecompileContexts(context: ProjectPostImportContext): List<JarDecompileContext> {
        val moduleDescriptorByName = context.chosenHybrisModuleDescriptors
            .associateBy { it.ideaModuleName() }

        return context.storage.entities<ModuleEntity>()
            .mapNotNull { moduleEntity ->
                val moduleDescriptor = moduleDescriptorByName[moduleEntity.name]
                    ?: return@mapNotNull null

                // PLATFORM has sources, decompilation is not needed.
                if (moduleDescriptor.type == ModuleDescriptorType.PLATFORM) return@mapNotNull null
                if (moduleDescriptor.type != ModuleDescriptorType.OOTB && moduleDescriptor.type != ModuleDescriptorType.EXT) return@mapNotNull null

                if (moduleEntity.hasNonGeneratedJavaSources()) return@mapNotNull null

                moduleEntity.getModuleLibraries(context.storage)
                    .flatMap { it.roots.asSequence() }
                    .filter { it.type == LibraryRootTypeId.COMPILED }
                    .mapNotNull { libraryRoot ->
                        val jarRoot = libraryRoot.url.virtualFile ?: return@mapNotNull null
                        if (!jarRoot.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX)) return@mapNotNull null

                        JarDecompileContext(
                            moduleDescriptor = moduleDescriptor,
                            libraryRoot = libraryRoot,
                            jar = jarRoot,
                        )
                    }
                    .toList()
            }
            .flatten()
            .distinctBy { it.libraryRoot.url.url }
            .toList()
    }

    private fun ModuleEntity.hasNonGeneratedJavaSources(): Boolean = contentRoots
        .asSequence()
        .flatMap { it.sourceRoots.asSequence() }
        .flatMap { it.javaSourceRoots.asSequence() }
        .any { !it.generated }

    private suspend fun decompileJar(context: JarDecompileContext, ideaDecompiler: IdeaDecompiler): DecompileResult {
        val logger = thisLogger()
        val jarPath = context.libraryRoot.toCompiledJarPathOrNull() ?: return DecompileResult.Failed

        return withContext(Dispatchers.IO) {
            val outputRoot = context.outputRoot()
            val marker = outputRoot.resolve(".decompiled")
            if (Files.isRegularFile(marker)) return@withContext DecompileResult.Skipped

            val jarRoot = context.jar

            if (Files.exists(outputRoot)) FileUtil.delete(outputRoot.toFile())
            Files.createDirectories(outputRoot)

            var decompiledCount = 0
            var failedCount = 0

            JarFile(jarPath.toFile()).use { jf ->
                val entries = jf.entries()
                while (entries.hasMoreElements()) {
                    checkCanceled()
                    val entry = entries.nextElement()
                    if (!entry.name.endsWith(".class")) continue
                    val vFile = jarRoot.findFileByRelativePath(entry.name) ?: continue

                    val source = runCatching { ideaDecompiler.getText(vFile).toString() }
                        .onFailure {
                            failedCount++
                            logger.debug("Failed to decompile ${vFile.path}", it)
                        }
                        .getOrNull()
                        ?: continue

                    val outFile = outputRoot.resolve(entry.name.removeSuffix(".class") + ".java")
                    Files.createDirectories(outFile.parent)
                    Files.writeString(outFile, source)
                    decompiledCount++
                }
            }

            if (decompiledCount == 0) {
                logger.warn("No classes could be decompiled for ${jarPath.fileName} (failed=$failedCount)")
                FileUtil.delete(outputRoot.toFile())
                return@withContext DecompileResult.Failed
            }

            if (failedCount > 0) logger.debug("Partially decompiled ${jarPath.fileName}: ok=$decompiledCount, failed=$failedCount")

            Files.writeString(marker, "ok")
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputRoot.toFile())
            DecompileResult.Completed
        }
    }

    private fun JarDecompileContext.outputRoot(): Path = moduleDescriptor.moduleRootPath
        .resolve("doc")
        .resolve("decompiledsrc")
        .resolve(jar.name.substringBeforeLast('.', jar.name))

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

        val contextsByCompiledRootUrl = contexts
            .distinctBy { it.libraryRoot.url.url }
            .associateBy { it.libraryRoot.url.url }

        val updatesByLibraryId = moduleLibraryEntities.mapNotNull { libraryEntity ->
            checkCanceled()

            val rootsToAttach = libraryEntity.roots
                .asSequence()
                .filter { it.type == LibraryRootTypeId.COMPILED }
                .mapNotNull { compiledRoot -> contextsByCompiledRootUrl[compiledRoot.url.url] }
                .mapNotNull { decompileContext ->
                    val outputRoot = decompileContext.outputRoot()
                        .takeIf { Files.isDirectory(it) }
                        ?: return@mapNotNull null

                    outputRoot.toSourcesLibraryRoot(vfUrlManager)
                }
                .distinctBy { it.url.url }
                .filterNot { newRoot ->
                    libraryEntity.roots.any { existing ->
                        existing.type == LibraryRootTypeId.SOURCES && existing.url == newRoot.url
                    }
                }
                .toList()

            if (rootsToAttach.isEmpty()) return@mapNotNull null

            logger.debug("Attaching ${rootsToAttach.size} decompiled roots to library '${libraryEntity.name}'")
            LibraryId(libraryEntity.name, libraryEntity.tableId) to rootsToAttach
        }.toMap()

        if (updatesByLibraryId.isEmpty()) return

        context.workspace.update("Attaching decompiled sources") { storage ->
            storage.entities<LibraryEntity>().forEach { entity ->
                val newRoots = updatesByLibraryId[LibraryId(entity.name, entity.tableId)] ?: return@forEach
                storage.modifyLibraryEntity(entity) { this.roots += newRoots }
            }
        }
    }

    private data class LibraryId(
        val name: String,
        val tableId: LibraryTableId
    )

    private fun LibraryRoot.toCompiledJarPathOrNull(): Path? {
        if (type != LibraryRootTypeId.COMPILED) return null

        val rawPath = VfsUtil.urlToPath(url.url)
        val localPath = rawPath
            .substringBefore('!')
            .removeSuffix("/")
            .removeSuffix("\\")
            .takeIf { it.endsWith(".jar") }
            ?: return null

        return try {
            Path.of(localPath).normalize()
        } catch (_: Exception) {
            null
        }
    }

    private fun Path.toSourcesLibraryRoot(vfUrlManager: VirtualFileUrlManager): LibraryRoot? {
        val url = vfUrlManager.fromPath(this) ?: return null
        return LibraryRoot(url, LibraryRootTypeId.SOURCES)
    }

    private fun notifyFinished(project: Project, results: List<DecompileResult>) {
        val completed = results.count { it == DecompileResult.Completed }
        val failed = results.count { it == DecompileResult.Failed }
        val skipped = results.count { it == DecompileResult.Skipped }
        if (completed == 0 && failed == 0) return

        val details = buildList {
            if (completed > 0) add("Completed: $completed")
            if (failed > 0) add("Failed: $failed")
            if (skipped > 0) add("Skipped: $skipped")
        }.joinToString(", ")

        val content = "$details. Decompiled sources are written under 'doc/decompiledsrc' in each module and attached as library sources."

        (if (failed > 0) Notifications.warning("OOTB decompiled sources", content) else Notifications.info("OOTB decompiled sources", content))
            .hideAfter(10)
            .notify(project)
    }

    private enum class DecompileResult {
        Completed,
        Failed,
        Skipped
    }
}
