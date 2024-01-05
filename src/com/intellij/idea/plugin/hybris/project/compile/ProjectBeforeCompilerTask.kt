/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.compile

import com.intellij.compiler.CompilerConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.root
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.compiler.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.ZipUtil
import org.jetbrains.jps.incremental.java.JavaBuilder
import org.jetbrains.jps.model.java.JpsJavaSdkType
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.jar.JarOutputStream
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name

class ProjectBeforeCompilerTask : CompileTask {

    override fun execute(context: CompileContext): Boolean {
        val modules = context.compileScope.affectedModules
        val platformModule = modules.firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_PLATFORM }
            ?: return true

        val settings = HybrisProjectSettingsComponent.getInstance(context.project)
        if (!settings.isHybrisProject()) return true
        if (!settings.state.generateCodeOnRebuild) {
            context.addMessage(CompilerMessageCategory.WARNING, "[y] Code generation is disabled, to enable it adjust SAP Commerce Project specific settings.", null, -1, -1)
            return true
        }

        val platformModuleRoot = platformModule.root()
            ?: return true
        val coreModuleRoot = modules
            .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_CORE }
            ?.root()
            ?: return true

        val sdk = ModuleRootManager.getInstance(platformModule).sdk
            ?: return true
        val sdkVersion = JavaSdk.getInstance().getVersion(sdk)
            ?: return true
        val vmExecutablePath = (sdk.sdkType as? JavaSdkType)
            ?.getVMExecutablePath(sdk)
            ?: return true

        val bootstrapDirectory = platformModuleRoot.resolve(HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY)
        if (!invokeCodeGeneration(context, platformModuleRoot, bootstrapDirectory, coreModuleRoot, vmExecutablePath)) {
            ProjectCompileUtil.triggerRefreshGeneratedFiles(bootstrapDirectory)
            return false
        }
        if (!invokeCodeCompilation(context, platformModule, bootstrapDirectory, sdkVersion)) {
            ProjectCompileUtil.triggerRefreshGeneratedFiles(bootstrapDirectory)
            return false
        }
        if (!invokeModelsJarCreation(context, bootstrapDirectory)) return false

        return true;
    }

    private fun invokeCodeGeneration(
        context: CompileContext,
        platformModuleRoot: Path,
        bootstrapDirectory: Path,
        coreModuleRoot: Path,
        vmExecutablePath: String,
    ): Boolean {
        val pathToBeDeleted = bootstrapDirectory.resolve(HybrisConstants.GEN_SRC_DIRECTORY)
        cleanDirectory(context, pathToBeDeleted)

        val classpath = setOf(
            coreModuleRoot.resolve("lib").resolve("*"),
            bootstrapDirectory.resolve(HybrisConstants.BIN_DIRECTORY).resolve("ybootstrap.jar")
        )
        val gcl = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(platformModuleRoot.toFile())
            .withExePath(vmExecutablePath)
            .withCharset(StandardCharsets.UTF_8)
            .withParameters(
                "-cp",
                classpath.joinToString(":"),
                HybrisConstants.CLASS_FQN_CODE_GENERATOR,
                platformModuleRoot.toString()
            )

        var result = false
        val handler = JavaCommandLineStateUtil.startProcess(gcl, true)
        handler.addProcessListener(object : ProcessAdapter() {

            override fun startNotified(event: ProcessEvent) {
                context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started code generation...", null, -1, -1)
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when {
                    event.text.contains("Process finished with exit code 1") -> context.addMessage(CompilerMessageCategory.ERROR, event.text, null, -1, -1)
                    event.text.contains("Process finished with exit code 0") -> result = true
                    event.text.isNotBlank() -> context.addMessage(CompilerMessageCategory.INFORMATION, event.text, null, -1, -1)
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Completed code generation.", null, -1, -1)
            }
        })

        handler.startNotify()
        val waitFor = handler.waitFor(10000)
        if (!waitFor) {
            context.addMessage(CompilerMessageCategory.ERROR, "[y] Code generation failed after waiting for 10 seconds.", null, -1, -1)
            handler.destroyProcess()
        }
        return result
    }

    private fun invokeCodeCompilation(
        context: CompileContext,
        platformModule: Module,
        bootstrapDirectory: Path,
        sdkVersion: JavaSdkVersion
    ): Boolean {
        val pathToBeDeleted = bootstrapDirectory.resolve(HybrisConstants.PLATFORM_MODEL_CLASSES_DIRECTORY)
        cleanDirectory(context, pathToBeDeleted)

        try {
            context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started compilation of the generated code...", null, -1, -1)
            val sourceFiles = mutableSetOf<File>()
            Files.walkFileTree(
                bootstrapDirectory.resolve(HybrisConstants.GEN_SRC_DIRECTORY),
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                        if (file?.extension == "java" && file.name != "package-info.java") sourceFiles.add(file.toFile())
                        return super.visitFile(file, attrs)
                    }
                })

            val profile = CompilerConfiguration.getInstance(context.project).getAnnotationProcessingConfiguration(platformModule)
            val sourceOption = JpsJavaSdkType.complianceOption(sdkVersion.maxLanguageLevel.toJavaVersion())
            val options = mutableListOf(
                "-encoding",
                "UTF-8",
                "-source",
                sourceOption,
                "-target",
                sourceOption
            )
            JavaBuilder.addAnnotationProcessingOptions(options, profile)

            val rootManager = ModuleRootManager.getInstance(platformModule)
            val classpath = rootManager.orderEntries().compileOnly().recursively().exportedOnly().withoutSdk().pathsList.pathList
                .map { File(it) }
            val platformClasspath = rootManager.orderEntries().compileOnly().sdkOnly().pathsList.pathList
                .map { File(it) }

            val classes = CompilerManager.getInstance(context.project).compileJavaCode(
                options,
                platformClasspath,
                classpath,
                emptyList(),
                emptyList(),
                listOf(bootstrapDirectory.resolve(HybrisConstants.GEN_SRC_DIRECTORY).toFile()),
                sourceFiles,
                bootstrapDirectory.resolve(HybrisConstants.PLATFORM_MODEL_CLASSES_DIRECTORY).toFile()
            )
            context.addMessage(CompilerMessageCategory.STATISTICS, "[y] Compiled ${classes.size} generated classes.", null, -1, -1)
            val flushedClasses = classes
                .filter { it.content != null }
                .map { it.path to it.content!! }
                .onEach { (path, bytes) -> FileUtil.writeToFile(File(path), bytes) }
            context.addMessage(CompilerMessageCategory.STATISTICS, "[y] Flushed ${flushedClasses.size} compiled classes.", null, -1, -1)
            context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Completed compilation of the generated code.", null, -1, -1)
        } catch (e: CompilationException) {
            e.messages.forEach {
                context.addMessage(CompilerMessageCategory.WARNING, it.text, null, -1, -1)
            }
            context.addMessage(CompilerMessageCategory.ERROR, "[y] Generated code compilation failed.", null, -1, -1)
            return false
        }

        return true
    }

    private fun invokeModelsJarCreation(context: CompileContext, bootstrapDirectory: Path): Boolean {
        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started creation of the models.jar file...", null, -1, -1)

        val bootstrapDir = (System.getenv(HybrisConstants.ENV_HYBRIS_BOOTSTRAP_BIN_DIR)
            ?.let { Paths.get(it) }
            ?: bootstrapDirectory.resolve(HybrisConstants.BIN_DIRECTORY))

        val modelsFile = bootstrapDir.resolve(HybrisConstants.JAR_MODELS).toFile()
        if (modelsFile.exists()) modelsFile.delete()

        try {
            val bos = BufferedOutputStream(FileOutputStream(modelsFile))
            JarOutputStream(bos).use { jos ->
                ZipUtil.addFileOrDirRecursively(
                    jos,
                    null,
                    bootstrapDirectory.resolve(HybrisConstants.PLATFORM_MODEL_CLASSES_DIRECTORY).toFile(),
                    "",
                    null,
                    null
                )
            }
        } catch (e: IOException) {
            context.addMessage(CompilerMessageCategory.ERROR, e.toString(), null, -1, -1)
            context.addMessage(CompilerMessageCategory.ERROR, "[y] Generated code compilation failed.", null, -1, -1)
            return false
        }
        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Completed creation of the models.jar file.", null, -1, -1)

        return true;
    }

    private fun cleanDirectory(context: CompileContext, pathToBeDeleted: Path) {
        if (!pathToBeDeleted.exists()) return

        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started cleaning the: $pathToBeDeleted", null, -1, -1)

        Files.walk(pathToBeDeleted)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)

        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Cleaned: $pathToBeDeleted", null, -1, -1)
    }

}
