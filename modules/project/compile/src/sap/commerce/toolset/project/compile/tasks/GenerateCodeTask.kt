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

package sap.commerce.toolset.project.compile.tasks

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.compiler.CompilationException
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.ZipUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.util.jar.JarOutputStream
import kotlin.io.path.pathString

open class GenerateCodeTask(override val taskContext: CompileTaskContext) : CodeTask<CompileTaskContext>() {

    override fun getCodeGenerationCommandLine( ): GeneralCommandLine {
        val platformModuleRoot = taskContext.platformModuleRoot
        val classpath = setOf(
            taskContext.coreModuleRoot.resolve("lib").pathString + "/*",
            taskContext.bootstrapDirectory.resolve(ProjectConstants.Directory.BIN).resolve("ybootstrap.jar").toString()
        )
            .joinToString(File.pathSeparator)

        val commandLine = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(platformModuleRoot)
            .withExePath(taskContext.vmExecutablePath)
            .withCharset(Charsets.UTF_8)
            .withParameters(
                "-Dfile.encoding=UTF-8",
                "-cp",
                classpath,
                HybrisConstants.CLASS_FQN_CODE_GENERATOR,
                platformModuleRoot.pathString
            )

        return commandLine
    }

    override fun invokeCodeCompilation(): Boolean {
        val context = taskContext.context
        val bootstrapDirectory = taskContext.bootstrapDirectory
        val pathToBeDeleted = bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES)
        cleanDirectory(pathToBeDeleted)

        try {
            context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started compilation of the generated code...", null, -1, -1)
            val sourceFiles = collectSourceFiles().map { it.toFile() }

            val rootManager = ModuleRootManager.getInstance(taskContext.platformModule)
            val classpath = rootManager.orderEntries().compileOnly().recursively().exportedOnly().withoutSdk().pathsList.pathList
                .map { File(it) }
            val platformClasspath = rootManager.orderEntries().compileOnly().sdkOnly().pathsList.pathList
                .map { File(it) }

            val classes = CompilerManager.getInstance(context.project).compileJavaCode(
                buildCompileOptions(),
                platformClasspath,
                classpath,
                emptyList(),
                emptyList(),
                listOf(bootstrapDirectory.resolve(ProjectConstants.Directory.GEN_SRC).toFile()),
                sourceFiles,
                bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES).toFile()
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

    override fun invokeModelsJarCreation(): Boolean {
        val context = taskContext.context
        val bootstrapDirectory = taskContext.bootstrapDirectory
        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started creation of the models.jar file...", null, -1, -1)

        val bootstrapDir = System.getenv(HybrisConstants.ENV_HYBRIS_BOOTSTRAP_BIN_DIR)
            ?.let { Paths.get(it) }
            ?: bootstrapDirectory.resolve(ProjectConstants.Directory.BIN)

        val modelsFile = bootstrapDir.resolve(HybrisConstants.JAR_MODELS).toFile()
        if (modelsFile.exists()) modelsFile.delete()

        try {
            val bos = BufferedOutputStream(FileOutputStream(modelsFile))
            JarOutputStream(bos).use { jos ->
                ZipUtil.addFileOrDirRecursively(
                    jos,
                    null,
                    bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES).toFile(),
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

        return true
    }
}