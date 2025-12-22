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

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.util.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.java.decompiler.IdeaDecompiler
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.settings.ApplicationSettings
import java.io.File
import java.util.jar.JarFile

/**
 * Decompile OOTB bin jars into doc/decompiledsrc and attach as additional sources.
 */
class DecompiledOotbSourcesConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "OOTB Decompiled Sources"

    override suspend fun configure(context: ProjectPostImportContext) {
        val appSettings = ApplicationSettings.getInstance()

        appSettings.withDecompiledOotbSources = context.settings.withDecompiledOotbSources

        if (!appSettings.withDecompiledOotbSources) return
        if (!isDecompilerPluginEnabled()) return

        val project = context.project

        if (!appSettings.decompiledOotbSourcesConsentAsked) {
            application.invokeAndWait({
                val accepted = Messages.showYesNoDialog(
                    project,
                    "Attach decompiled sources for OOTB modules (bin/*.jar) into doc/decompiledsrc so they become searchable. This may take time and disk space. Continue?",
                    "Decompile OOTB Binaries",
                    Messages.getYesButton(),
                    Messages.getNoButton(),
                    null
                ) == Messages.YES

                appSettings.decompiledOotbSourcesConsentAsked = true
                appSettings.withDecompiledOotbSources = accepted
            }, ModalityState.defaultModalityState())

            if (!appSettings.withDecompiledOotbSources) return
        }

        val decompiler = IdeaDecompiler()
        val tasks = collectTargets(context)
            .ifEmpty { return }

        val logger = thisLogger()
        logger.info("Decompiling ${tasks.size} OOTB bin jars")
        val results = withContext(Dispatchers.IO) {
            reportProgressScope(tasks.size) { progress ->
                tasks.map { task ->
                    logger.debug("Decompiling ${task.jar.name}")
                    progress.itemStep("Decompiling ${task.jar.name}") {
                        runCatching { decompileJar(task, decompiler) }
                            .onFailure { logger.warn("Decompilation failed for ${task.jar}", it) }
                            .getOrDefault(false)
                    }
                }
            }
        }

        val successfulTasks = tasks.zip(results)
            .filter { it.second }
            .map { it.first }

        attachDecompiledSources(project, successfulTasks)

        val succeeded = results.count { it }
        val failed = results.size - succeeded
        if (succeeded > 0 || failed > 0) {
            logger.info("Decompiled sources: $succeeded succeeded, $failed failed")
            Notifications.info(
                "OOTB decompiled sources finished",
                "Completed: $succeeded, Failed: $failed. Output: doc/decompiledsrc"
            ).hideAfter(10).notify(project)
        }
    }

    private fun isDecompilerPluginEnabled(): Boolean {
        val pluginId = PluginId.getId("org.jetbrains.java.decompiler")
        val pluginPresent = PluginManagerCore.getPlugin(pluginId) != null
        return pluginPresent && !PluginManagerCore.isDisabled(pluginId)
    }

    private fun collectTargets(context: ProjectPostImportContext): List<JarTask> {
        val modules = context.chosenHybrisModuleDescriptors
            .filterIsInstance<YModuleDescriptor>()
            .filter { it.type === ModuleDescriptorType.OOTB || it.type === ModuleDescriptorType.PLATFORM || it.type === ModuleDescriptorType.EXT }

        val tasks = mutableListOf<JarTask>()
        modules.forEach { descriptor ->
            if (descriptor is YSubModuleDescriptor) return@forEach

            val moduleRoot = descriptor.moduleRootPath.toFile()
            val hasSources = ProjectConstants.Directory.SRC_DIR_NAMES
                .asSequence()
                .map { File(moduleRoot, it) }
                .filter { it.isDirectory }
                .flatMap { (it.listFiles() ?: emptyArray()).asSequence() }
                .any()

            if (hasSources) return@forEach

            val binDir = File(moduleRoot, ProjectConstants.Directory.BIN)
            if (!binDir.isDirectory) return@forEach

            val targetRoot = File(moduleRoot, "doc/decompiledsrc")

            binDir
                .listFiles { _, name: String -> name.endsWith(HybrisConstants.SERVER_JAR_SUFFIX) }
                ?.takeIf { it.isNotEmpty() }
                ?.forEach { jar ->
                    tasks.add(JarTask(descriptor, jar, targetRoot))
                }
        }
        return tasks
    }

    private fun decompileJar(task: JarTask, decompiler: IdeaDecompiler): Boolean {
        val jar = task.jar
        val targetRoot = task.targetRoot

        val outputRoot = File(targetRoot, jar.nameWithoutExtension)
        val marker = File(outputRoot, ".timestamp")
        val markerValue = jarMarkerValue(jar)

        if (outputRoot.isDirectory && marker.isFile && runCatching { marker.readText() }.getOrNull() == markerValue) {
            return true
        }

        val jarRoot = JarFileSystem.getInstance().findFileByPath(jar.path + "!/") ?: return false

        if (outputRoot.exists()) FileUtil.delete(outputRoot)
        outputRoot.mkdirs()

        JarFile(jar).use { jf ->
            val entries = jf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.name.endsWith(".class")) continue
                val vFile = jarRoot.findFileByRelativePath(entry.name) ?: continue
                val source = decompiler.getText(vFile)
                val outFile = File(outputRoot, entry.name.removeSuffix(".class") + ".java")
                outFile.parentFile.mkdirs()
                outFile.writeText(source.toString())
            }
        }

        marker.writeText(markerValue)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outputRoot)
        return true
    }

    private fun jarMarkerValue(jar: File): String = "${jar.lastModified()}:${jar.length()}"

    private suspend fun attachDecompiledSources(project: Project, tasks: List<JarTask>) {
        val perModuleTasks = tasks
            .distinctBy { runCatching { it.jar.canonicalPath }.getOrNull() ?: it.jar.path }
            .groupBy { it.moduleDescriptor.ideaModuleName() }
            .ifEmpty { return }

        val logger = thisLogger()

        withContext(Dispatchers.Default) {
            edtWriteAction {
                val moduleManager = ModuleManager.getInstance(project)
                val localFileSystem = LocalFileSystem.getInstance()

                perModuleTasks.forEach { (moduleName, moduleTasks) ->
                    val module = moduleManager.findModuleByName(moduleName) ?: return@forEach
                    val rootModel = ModuleRootManager.getInstance(module).modifiableModel

                    try {
                        val libraries = rootModel.orderEntries
                            .mapNotNull { it as? LibraryOrderEntry }
                            .mapNotNull { it.library }

                        val urlsToAttachByLibrary = linkedMapOf<Library, MutableSet<String>>()

                        moduleTasks.forEach { task ->
                            val decompiledRoot = File(task.targetRoot, task.jar.nameWithoutExtension)
                                .takeIf { it.isDirectory }
                                ?: return@forEach
                            val decompiledVf = localFileSystem.refreshAndFindFileByIoFile(decompiledRoot)
                                ?: return@forEach

                            val targetLibrary = chooseTargetLibrary(task, libraries)
                            if (targetLibrary == null) {
                                logger.debug("No library found for jar ${task.jar} in module $moduleName")
                                return@forEach
                            }

                            urlsToAttachByLibrary
                                .getOrPut(targetLibrary) { linkedSetOf() }
                                .add(decompiledVf.url)
                        }

                        urlsToAttachByLibrary.forEach { (library, urlsToAttach) ->
                            val libraryModel = library.modifiableModel
                            val existingSourceUrls = libraryModel.getUrls(OrderRootType.SOURCES).toSet()
                            urlsToAttach
                                .filterNot { existingSourceUrls.contains(it) }
                                .forEach { libraryModel.addRoot(it, OrderRootType.SOURCES) }
                            libraryModel.commit()
                        }
                    } finally {
                        rootModel.commit()
                    }
                }
            }
        }
    }

    private fun chooseTargetLibrary(
        task: JarTask,
        libraries: List<Library>
    ): Library? {
        val jarPath = runCatching { task.jar.canonicalPath }.getOrNull() ?: return null
        val jarName = task.jar.name

        return libraries.firstOrNull { library ->
            library.getUrls(OrderRootType.CLASSES)
                .any { url ->
                    val file = libraryClassRootToFile(url) ?: return@any false
                    val canonicalPath = runCatching { file.canonicalPath }.getOrNull() ?: return@any false
                    canonicalPath == jarPath || canonicalPath.contains(jarName)
                }
        }
    }

    private fun libraryClassRootToFile(url: String): File? {
        val path = VfsUtil.urlToPath(url)
        val localPath = path.substringBefore('!')
        if (localPath.isBlank()) return null

        return runCatching { File(localPath) }.getOrNull()
    }


    private data class JarTask(val moduleDescriptor: YModuleDescriptor, val jar: File, val targetRoot: File)
}