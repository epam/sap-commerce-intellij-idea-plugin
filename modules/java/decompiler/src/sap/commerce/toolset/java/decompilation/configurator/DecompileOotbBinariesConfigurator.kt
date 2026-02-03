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

package sap.commerce.toolset.java.decompilation.configurator

import com.intellij.java.workspace.entities.javaSourceRoots
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.workspaceModel.ide.toPath
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.java.decompilation.DecompilerConsent
import sap.commerce.toolset.java.decompilation.DecompilerService
import sap.commerce.toolset.java.decompilation.JarDecompileContext
import sap.commerce.toolset.java.decompilation.JarDecompileResult
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.fromPath
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

/**
 * Decompile OOTB bin jars into doc/decompiledsrc and attach as additional sources.
 */
class DecompileOotbBinariesConfigurator : ProjectPostImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Decompile OOTB Binaries"

    override suspend fun configure(context: ProjectPostImportContext) {
        val importSettings = context.settings
        val project = context.project

        if (!importSettings.withDecompiledOotbSources) return
        if (Plugin.JAVA_DECOMPILER.isDisabled()) return

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val consentAccepted = ensureConsentAccepted()
            if (!consentAccepted) return@launch

            withBackgroundProgress(project, "Decompiling OOTB binaries...", true) {
                val contexts = readAction { collectDecompileContexts(context) }
                    .ifEmpty { return@withBackgroundProgress }

                logger.debug("Decompiling ${contexts.size} OOTB bin jars")

                val decompileResults = supervisorScope {
                    reportProgressScope(contexts.size) { progress ->
                        contexts.map { decompileContext ->
                            async(Dispatchers.IO) {
                                progress.itemStep("Decompiling ${decompileContext.jar.name}") {
                                    checkCanceled()
                                    runCatching { decompileJar(decompileContext) }
                                        .onFailure { logger.warn("Decompilation failed for ${decompileContext.jar.name}", it) }
                                        .getOrElse { JarDecompileResult.Failed }
                                }
                            }
                        }.awaitAll()
                    }
                }

                val contextsToAttach = contexts.zip(decompileResults)
                    .filter { it.second != JarDecompileResult.Failed }
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
            DecompilerConsent.Accepted -> true
            DecompilerConsent.Postponed -> false
            DecompilerConsent.Rejected -> {
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

        return buildList {
            for (moduleEntity in context.storage.entities<ModuleEntity>()) {
                val moduleDescriptor = moduleDescriptorByName[moduleEntity.name] ?: continue

                // PLATFORM has sources, decompilation is not needed.
                if (moduleDescriptor.type == ModuleDescriptorType.PLATFORM) continue
                if (moduleDescriptor.type != ModuleDescriptorType.OOTB && moduleDescriptor.type != ModuleDescriptorType.EXT) continue

                if (moduleEntity.hasNonGeneratedJavaSources()) continue

                for (libraryEntity in moduleEntity.getModuleLibraries(context.storage)) {
                    val libraryId = libraryEntity.symbolicId
                    for (libraryRoot in libraryEntity.roots) {
                        if (libraryRoot.type != LibraryRootTypeId.COMPILED) continue
                        val jarRoot = libraryRoot.url.virtualFile ?: continue
                        if (!jarRoot.name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX)) continue

                        add(
                            JarDecompileContext(
                                moduleDescriptor = moduleDescriptor,
                                libraryId = libraryId,
                                libraryRoot = libraryRoot,
                                jar = jarRoot,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun ModuleEntity.hasNonGeneratedJavaSources(): Boolean = contentRoots
        .asSequence()
        .flatMap { it.sourceRoots.asSequence() }
        .flatMap { it.javaSourceRoots.asSequence() }
        .any { !it.generated }

    private suspend fun decompileJar(context: JarDecompileContext): JarDecompileResult {
        val logger = thisLogger()
        val jarPath = context.libraryRoot.toCompiledJarPathOrNull() ?: return JarDecompileResult.Failed

        return withContext(Dispatchers.IO) {
            val outputRoot = context.outputRoot()
            val marker = outputRoot.resolve(".decompiled")
            if (Files.isRegularFile(marker)) return@withContext JarDecompileResult.Skipped

            val jarRoot = context.jar

            if (outputRoot.directoryExists) FileUtilRt.deleteRecursively(outputRoot)
            Files.createDirectories(outputRoot)

            var decompiledCount = 0
            var failedCount = 0

            JarFile(jarPath.toFile()).use { jf ->
                val classEntryNames = buildSet {
                    val allEntries = jf.entries()
                    while (allEntries.hasMoreElements()) {
                        val entry = allEntries.nextElement()
                        if (entry.name.endsWith(".class")) add(entry.name)
                    }
                }

                val entries = jf.entries()

                reportProgressScope {
                    while (entries.hasMoreElements()) {
                        checkCanceled()
                        val entry = entries.nextElement()
                        val entryName = entry.name
                        if (!entryName.endsWith(".class")) continue

                        // Decompile only top-level classes.
                        // Inner/nested/anonymous classes are separate *.class entries (Outer$Inner.class, Outer$1.class, ...).
                        // IntelliJ decompiler reconstructs them into the outer class output anyway, so writing them separately
                        // produces duplicate-looking sources (Outer.java and Outer$Inner.java).
                        if (shouldSkipInnerClassEntry(entryName, classEntryNames)) continue

                        it.indeterminateStep("Decompiling class: $entryName") {
                            val source = runCatching {
                                readAction {
                                    jarRoot.findFileByRelativePath(entryName)
                                        ?.let { LoadTextUtil.loadText(it) }
                                        ?: return@readAction null
                                }
                            }
                                .onFailure {
                                    failedCount++
                                    logger.debug("Failed to decompile ${jarPath.fileName}!/$entryName due: ${it.message}")
                                }
                                .getOrNull()

                            if (source != null) {
                                val outFile = outputRoot.resolve(entryName.removeSuffix(".class") + ".java")
                                Files.createDirectories(outFile.parent)
                                Files.writeString(outFile, source)
                                decompiledCount++
                            }
                        }
                    }
                }
            }

            if (decompiledCount == 0) {
                logger.warn("No classes could be decompiled for ${jarPath.fileName} (failed=$failedCount)")
                FileUtilRt.deleteRecursively(outputRoot)
                return@withContext JarDecompileResult.Failed
            }

            if (failedCount > 0) logger.debug("Partially decompiled ${jarPath.fileName}: ok=$decompiledCount, failed=$failedCount")

            Files.writeString(marker, "ok")
            VfsUtil.markDirtyAndRefresh(true, true, true, outputRoot.toFile())
            JarDecompileResult.Completed
        }
    }

    private fun shouldSkipInnerClassEntry(entryName: String, classEntryNames: Set<String>): Boolean {
        val dollarIndex = entryName.indexOf('$')
        if (dollarIndex < 0) return false

        val outerClassEntryName = entryName.substring(0, dollarIndex) + ".class"
        return classEntryNames.contains(outerClassEntryName)
    }

    private fun JarDecompileContext.outputRoot(): Path = moduleDescriptor.moduleRootPath
        .resolve("doc")
        .resolve("decompiledsrc")
        .resolve(jar.name.substringBeforeLast('.', jar.name))

    private suspend fun attachDecompiledSources(
        context: ProjectPostImportContext,
        contexts: List<JarDecompileContext>
    ) {
        val vfUrlManager = context.workspace.getVirtualFileUrlManager()
        val sourcesRootsByLibraryId = contexts
            .mapNotNull { decompileContext ->
                val outputRoot = decompileContext.outputRoot()
                    .takeIf { it.directoryExists }
                    ?: return@mapNotNull null
                val sourcesRoot = outputRoot.toSourcesLibraryRoot(vfUrlManager)
                    ?: return@mapNotNull null

                decompileContext.libraryId to sourcesRoot
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .takeIf { it.isNotEmpty() }
            ?: return

        context.workspace.update("Attaching decompiled sources") { storage ->
            storage.entities<LibraryEntity>()
                .forEach { entity ->
                    val sourceLibraryRoots = sourcesRootsByLibraryId[entity.symbolicId]
                        ?: return@forEach
                    val existingSources = entity.roots
                        .filter { it.type == LibraryRootTypeId.SOURCES }
                    val newSourceRoots = sourceLibraryRoots
                        .filterNot { existingSources.contains(it) }
                        .takeIf { it.isNotEmpty() }
                        ?: return@forEach

                    storage.modifyLibraryEntity(entity) {
                        this.roots += newSourceRoots
                    }
                }
        }
    }

    private fun LibraryRoot.toCompiledJarPathOrNull(): Path? {
        if (type != LibraryRootTypeId.COMPILED) return null

        return runCatching { url.toPath().normalize() }.getOrNull()
    }

    private fun Path.toSourcesLibraryRoot(vfUrlManager: VirtualFileUrlManager): LibraryRoot? = vfUrlManager.fromPath(this)
        ?.let { LibraryRoot(it, LibraryRootTypeId.SOURCES) }

    private fun notifyFinished(project: Project, results: List<JarDecompileResult>) {
        val completed = results.count { it == JarDecompileResult.Completed }
        val failed = results.count { it == JarDecompileResult.Failed }
        val skipped = results.count { it == JarDecompileResult.Skipped }
        if (completed == 0 && failed == 0) return

        val details = buildList {
            if (completed > 0) add("Completed: $completed")
            if (failed > 0) add("Failed: $failed")
            if (skipped > 0) add("Skipped: $skipped")
        }.joinToString(", ")

        Notifications.create(
            type = if (failed > 0) NotificationType.WARNING else NotificationType.INFORMATION,
            title = "Decompiled OOTB sources",
            content = "$details. Decompiled sources are written under 'doc/decompiledsrc' in each module and attached as library sources."
        )
            .hideAfter(10)
            .notify(project)
    }
}
