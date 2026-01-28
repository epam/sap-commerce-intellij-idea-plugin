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
import com.intellij.execution.wsl.WSLCommandLineOptions
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.roots.ModuleRootManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.compile.context.CompileTaskContext
import sap.commerce.toolset.project.compile.context.WslCompileTaskContext
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class WslGenerateCodePreCompileTask(
    taskContext: CompileTaskContext,
    val wslContext: WslCompileTaskContext
) : PreCompileTask(taskContext) {

    override fun getCodeGenerationCommandLine(): GeneralCommandLine {
        val platformModulePath = taskContext.platformModulePath
        val classpath = setOf(
            osSpecificPath(taskContext.coreModulePath.resolve("lib").pathString + "/*"),
            osSpecificPath(taskContext.bootstrapPath.resolve(ProjectConstants.Directory.BIN).resolve("ybootstrap.jar").pathString)
        )
            .joinToString(":")

        val commandLine = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(platformModulePath)
            .withExePath(osSpecificPath(taskContext.vmExecutablePath))
            .withCharset(Charsets.UTF_8)
            .withParameters("-Dfile.encoding=UTF-8", "-cp", classpath, HybrisConstants.CLASS_FQN_CODE_GENERATOR, osSpecificPath(platformModulePath))

        return commandLine
    }

    override fun invokeCodeCompilation() = startProcess(
        "code compilation",
        beforeProcessStart = {
            val pathToBeDeleted = taskContext.bootstrapPath.resolve(ProjectConstants.Directory.MODEL_CLASSES)
            cleanDirectory(pathToBeDeleted)
        }
    ) {
        val wslDistribution = wslContext.wslDistribution
        val classpath = ModuleRootManager.getInstance(taskContext.platformModule)
            .orderEntries().compileOnly().recursively().exportedOnly().withoutSdk()
            .pathsList.pathList
            .joinToString(":") { osSpecificPath(it) }

        val mntClasspathFile = Files.createTempFile("compile", "cx")
            .also { it.toFile().deleteOnExit() }
            .apply { Files.writeString(this, classpath, StandardCharsets.UTF_8) }
            .let { wslDistribution.getWslPath(it) }

        val sourceFiles = collectSourceFiles()
            .joinToString("\n") { osSpecificPath(it) }
        val mntSourceFilesFile = Files.createTempFile("sources", "cx")
            .also { it.toFile().deleteOnExit() }
            .apply { Files.writeString(this, sourceFiles, StandardCharsets.UTF_8) }
            .let { wslDistribution.getWslPath(it) }
        val outputDir = osSpecificPath(taskContext.bootstrapPath.resolve(ProjectConstants.Directory.MODEL_CLASSES))

        GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(taskContext.platformModulePath)
            .withExePath(osSpecificPath("${wslContext.vmBinPath}\\javac"))
            .withCharset(Charsets.UTF_8)
            .withParameters("-nowarn", "-d", outputDir, "-cp", "@$mntClasspathFile", "@$mntSourceFilesFile")
            .apply {
                val context = taskContext.context
                val wslDistribution = wslDistribution
                val options = WSLCommandLineOptions()
                    .setExecuteCommandInShell(true)
                    .setLaunchWithWslExe(true)
                wslDistribution.patchCommandLine<GeneralCommandLine>(this, context.project, options)
            }
    }

    override fun invokeModelsJarCreation() = startProcess("models.jar creation") {
        val bootstrapPath = taskContext.bootstrapPath
        val wslDistribution = wslContext.wslDistribution
        val outputDir = osSpecificPath(bootstrapPath.resolve(ProjectConstants.Directory.MODEL_CLASSES))
        val modelsFile = osSpecificPath(bootstrapPath.resolve(ProjectConstants.Directory.BIN).resolve(HybrisConstants.JAR_MODELS))

        bootstrapPath.resolve(ProjectConstants.Directory.BIN).resolve(HybrisConstants.JAR_MODELS)
            .let { wslDistribution.getWslPath(it) }
            ?.let { Path(it) }
            ?.let { Files.deleteIfExists(it) }

        GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(taskContext.platformModulePath)
            .withExePath(osSpecificPath("${wslContext.vmBinPath}\\jar"))
            .withCharset(Charsets.UTF_8)
            .withParameters("cf", modelsFile, "-C", outputDir, ".")
            .apply {
                val context = taskContext.context
                val options = WSLCommandLineOptions()
                    .setExecuteCommandInShell(true)
                    .setLaunchWithWslExe(true)
                wslDistribution.patchCommandLine<GeneralCommandLine>(this, context.project, options)
            }
    }

    private fun osSpecificPath(path: Path) = path
        .pathString
        .let { osSpecificPath(it) }

    private fun osSpecificPath(path: String) = path
        .let { WslPath.parseWindowsUncPath(it)?.linuxPath ?: it }
}