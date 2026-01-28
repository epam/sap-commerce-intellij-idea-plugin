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

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.jps.entities.modifyLibraryEntity
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
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
import java.util.Locale
import java.util.jar.JarFile

class OotbDecompiledSourcesService {

    fun start(context: ProjectPostImportContext) {
        val project = context.project
        val logger = thisLogger()

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val consentAccepted = ensureConsentAccepted()
            if (!consentAccepted) return@launch

            val contexts = runReadAction { collectDecompileContexts(context) }
                .ifEmpty { return@launch }

            withBackgroundProgress(project, "Decompiling OOTB binaries...", true) {
                logger.debug("Decompiling ${contexts.size} OOTB bin jars")

                val ideaDecompiler = IdeaDecompiler()

                val decompileResults = withContext(Dispatchers.IO) {
                    reportProgressScope(contexts.size) { progress ->
                        contexts.map { decompileContext ->
                            async {
                                ensureActive()
                                progress.itemStep("Decompiling ${decompileContext.jar.fileName}") {
                                    checkCanceled()
                                    runCatching { decompileJar(decompileContext, ideaDecompiler) }
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
        val logger = thisLogger()

        val descriptorTypeByRoot = context.chosenHybrisModuleDescriptors
            .associateBy(
                keySelector = { it.moduleRootPath.normalizePathKey() },
                valueTransform = { it.type }
            )

        val moduleLibraryEntities = context.storage.entities<LibraryEntity>()
            .filter { it.typeId == ProjectConstants.Workspace.yLibraryTypeId }
            .filter { it.tableId is LibraryTableId.ModuleLibraryTableId }
            .toList()

        return moduleLibraryEntities.asSequence()
            .mapNotNull { libraryEntity ->
                // runReadAction is not cancellable, keep it lightweight and side-effect free.

                val moduleName = (libraryEntity.tableId as? LibraryTableId.ModuleLibraryTableId)
                    ?.moduleId
                    ?.name
                    ?: return@mapNotNull null

                val compiledJarPaths = libraryEntity.roots.asSequence()
                    .mapNotNull { it.toCompiledJarPathOrNull() }
                    .filter { it.fileName.toString().endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
                    .toList()

                if (compiledJarPaths.isEmpty()) return@mapNotNull null

                compiledJarPaths.mapNotNull { jarPath ->
                    val moduleRoot = jarPath.moduleRootFromBinJar() ?: return@mapNotNull null

                    val moduleType = descriptorTypeByRoot[moduleRoot.normalizePathKey()]
                        ?: run {
                            logger.debug("Skipping jar not mapped to imported module: $jarPath")
                            return@mapNotNull null
                        }

                    // PLATFORM has sources, decompilation is not needed.
                    if (moduleType == ModuleDescriptorType.PLATFORM) return@mapNotNull null
                    if (moduleType != ModuleDescriptorType.OOTB && moduleType != ModuleDescriptorType.EXT) return@mapNotNull null

                    val hasSources = ProjectConstants.Directory.SRC_DIR_NAMES
                        .asSequence()
                        .map { moduleRoot.resolve(it) }
                        .filter { Files.isDirectory(it) }
                        .flatMap {
                            try {
                                Files.list(it).use { stream -> stream.toList().asSequence() }
                            } catch (_: Exception) {
                                emptySequence()
                            }
                        }
                        .any()
                    if (hasSources) return@mapNotNull null

                    val targetRoot = moduleRoot.resolve("doc").resolve("decompiledsrc")
                    JarDecompileContext(moduleName, jarPath, targetRoot)
                }
            }
            .flatten()
            .distinctBy { it.jar.normalizePathKey() }
            .toList()
    }

    private suspend fun decompileJar(context: JarDecompileContext, ideaDecompiler: IdeaDecompiler): DecompileResult {
        val logger = thisLogger()
        val jar = context.jar
        val targetRoot = context.targetRoot

        val outputRoot = targetRoot.resolve(jar.nameWithoutExtension())
        val marker = outputRoot.resolve(".timestamp")
        val markerValue = jarMarkerValue(jar)

        val previousMarkerValue = try {
            marker.takeIf { Files.isRegularFile(it) }?.let { Files.readString(it) }
        } catch (_: Exception) {
            null
        }
        if (Files.isDirectory(outputRoot) && previousMarkerValue == markerValue) return DecompileResult.Skipped

        val localJarFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(jar.toFile())
            ?: return DecompileResult.Failed
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(localJarFile)
            ?: return DecompileResult.Failed

        if (Files.exists(outputRoot)) FileUtil.delete(outputRoot.toFile())
        Files.createDirectories(outputRoot)

        var decompiledCount = 0
        var failedCount = 0

        withContext(Dispatchers.IO) {
            JarFile(jar.toFile()).use { jf ->
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
        }

        if (decompiledCount == 0) {
            logger.warn("No classes could be decompiled for $jar (failed=$failedCount)")
            FileUtil.delete(outputRoot.toFile())
            return DecompileResult.Failed
        }

        if (failedCount > 0) logger.debug("Partially decompiled $jar: ok=$decompiledCount, failed=$failedCount")

        Files.writeString(marker, markerValue)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputRoot.toFile())
        return DecompileResult.Completed
    }

    private fun jarMarkerValue(jar: Path): String = try {
        val lastModified = Files.getLastModifiedTime(jar).toMillis()
        val size = Files.size(jar)
        "$lastModified:$size"
    } catch (_: Exception) {
        "0:0"
    }

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
            .distinctBy { it.jar.normalizePathKey() }
            .associateBy { it.jar.normalizePathKey() }

        val updatesByLibraryId = moduleLibraryEntities.mapNotNull { libraryEntity ->
            checkCanceled()

            val moduleName = (libraryEntity.tableId as? LibraryTableId.ModuleLibraryTableId)
                ?.moduleId
                ?.name
                ?: return@mapNotNull null

            val rootsToAttach = buildList {
                val compiledJarRoots = libraryEntity.roots
                    .asSequence()
                    .mapNotNull { it.toCompiledJarPathOrNull() }
                    .toList()

                compiledJarRoots.forEach { jarPath ->
                    val decompileContext = contextsByJarPath[jarPath.normalizePathKey()] ?: return@forEach
                    if (decompileContext.moduleName != moduleName) return@forEach

                    val outputRoot = decompileContext.targetRoot
                        .resolve(decompileContext.jar.nameWithoutExtension())
                        .takeIf { Files.isDirectory(it) }
                        ?: return@forEach

                    val libraryRoot = outputRoot.toSourcesLibraryRoot(vfUrlManager) ?: return@forEach

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

    private fun Path.normalizePathKey(): String = try {
        toRealPath().normalize().toString().lowercase(Locale.ROOT)
    } catch (_: Exception) {
        toAbsolutePath().normalize().toString().lowercase(Locale.ROOT)
    }

    private fun Path.moduleRootFromBinJar(): Path? {
        val binDir = parent ?: return null
        if (binDir.fileName.toString() != ProjectConstants.Directory.BIN) return null
        return binDir.parent
    }

    private fun Path.nameWithoutExtension(): String {
        val name = fileName.toString()
        val dot = name.lastIndexOf('.')
        return if (dot <= 0) name else name.substring(0, dot)
    }

    private enum class DecompileResult {
        Completed,
        Failed,
        Skipped
    }
}
