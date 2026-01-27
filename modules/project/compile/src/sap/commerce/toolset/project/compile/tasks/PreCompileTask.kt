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

import com.intellij.compiler.CompilerConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.lang.JavaVersion
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.compile.context.TaskContext
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.util.directoryExists
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.name

abstract class PreCompileTask<T : TaskContext> {

    abstract val taskContext: T

    fun invokeCodeGeneration() = startProcess(
        action = "Code generation",
        before = {
            val pathToBeDeleted = taskContext.bootstrapPath.resolve(ProjectConstants.Directory.GEN_SRC)
            cleanDirectory(pathToBeDeleted)
        }
    ) { getCodeGenerationCommandLine() }

    abstract fun invokeCodeCompilation(): Boolean
    abstract fun invokeModelsJarCreation(): Boolean

    protected abstract fun getCodeGenerationCommandLine(): GeneralCommandLine

    protected fun startProcess(
        action: String,
        before: () -> Unit = {},
        gcl: () -> GeneralCommandLine,
    ): Boolean {
        val context = taskContext.context
        val settings = context.project.ySettings

        before()

        var result = false
        val handler = JavaCommandLineStateUtil.startProcess(gcl(), true)
        handler.addProcessListener(object : ProcessListener {

            override fun startNotified(event: ProcessEvent) {
                context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started $action...", null, -1, -1)
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when {
                    event.text.contains("Process finished with exit code 1") -> context.addMessage(CompilerMessageCategory.ERROR, event.text, null, -1, -1)
                    event.text.contains("Process finished with exit code 0") -> result = true
                    event.text.isNotBlank() -> context.addMessage(CompilerMessageCategory.INFORMATION, event.text, null, -1, -1)
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Completed $action.", null, -1, -1)
            }
        })

        handler.startNotify()

        val waitFor = handler.waitFor(settings.generateCodeTimeoutSeconds * 1000L)
        if (!waitFor) {
            context.addMessage(CompilerMessageCategory.ERROR, "[y] $action failed after waiting for ${settings.generateCodeTimeoutSeconds} second(s).", null, -1, -1)
            handler.destroyProcess()
        }
        return result
    }

    protected fun cleanDirectory(pathToBeDeleted: Path) {
        if (!pathToBeDeleted.directoryExists) return
        val context = taskContext.context
        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started cleaning the: $pathToBeDeleted", null, -1, -1)

        Files.walk(pathToBeDeleted)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)

        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Cleaned: $pathToBeDeleted", null, -1, -1)
    }

    protected fun buildCompileOptions() = buildList {
        val context = taskContext.context
        val sdkVersion = taskContext.sdkVersion
        val platformModule = taskContext.platformModule
        val profile = CompilerConfiguration.getInstance(context.project).getAnnotationProcessingConfiguration(platformModule)
        val sourceOption = sdkVersion.maxLanguageLevel.toJavaVersion().complianceOption()

        add("-encoding")
        add("UTF-8")
        add("-source")
        add(sourceOption)
        add("-target")
        add(sourceOption)

        if (!profile.isEnabled) {
            add("-proc:none")
        } else {
            if (!profile.isObtainProcessorsFromClasspath) {
                val processorsPath = profile.processorPath
                add(if (profile.isUseProcessorModulePath) "--processor-module-path" else "-processorpath")
                add(FileUtil.toSystemDependentName(processorsPath.trim { it <= ' ' }))
            }

            val processors = profile.processors
            if (processors.isNotEmpty()) {
                add("-processor")
                add(StringUtil.join(processors, ","))
            }

            profile.processorOptions.entries.forEach {
                add("-A" + it.key + "=" + it.value)
            }
        }
    }

    protected fun collectSourceFiles() = buildSet {
        Files.walkFileTree(
            taskContext.bootstrapPath.resolve(ProjectConstants.Directory.GEN_SRC),
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.extension == "java" && file.name != "package-info.java") add(file)
                    return super.visitFile(file, attrs)
                }
            })
    }

    private fun JavaVersion.complianceOption() = if (feature < 5) "1.$feature"
    else feature.toString()
}
