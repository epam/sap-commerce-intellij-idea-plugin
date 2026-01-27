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
package sap.commerce.toolset.project.compile

import com.intellij.compiler.CompilerConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.wsl.WSLCommandLineOptions
import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.compiler.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.application
import com.intellij.util.io.ZipUtil
import com.intellij.util.lang.JavaVersion
import org.jetbrains.jps.model.java.compiler.AnnotationProcessingConfiguration
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.contentRoot
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.yExtensionName
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
import kotlin.io.path.pathString

class WslGenerateCodeCompilerTask : CompileTask {

    override fun execute(context: CompileContext): Boolean {
        val project = context.project
        val settings = ProjectSettings.getInstance(project)
        if (!project.isHybrisProject) return true
        if (!settings.generateCodeOnRebuild) {
            context.addMessage(CompilerMessageCategory.WARNING, "[y] Code generation is disabled, to enable it adjust SAP Commerce Project specific settings.", null, -1, -1)
            return true
        }

        val typeId = context.compileScope.getUserData(CompilerManager.RUN_CONFIGURATION_TYPE_ID_KEY)
        // do not rebuild sources in case of JUnit
        // see JUnitConfigurationType
        if ("JUnit" == typeId && !settings.generateCodeOnJUnitRunConfiguration) return true


        val modules = application.runReadAction<Array<Module>> { context.compileScope.affectedModules }
        val moduleMapping = project.ySettings.module2extensionMapping
        val platformModule = modules.firstOrNull { it.yExtensionName(moduleMapping) == EiConstants.Extension.PLATFORM }
            ?: return true

        val platformModuleRoot = platformModule.contentRoot
            ?: return true
        val coreModuleRoot = modules
            .firstOrNull { it.yExtensionName(moduleMapping) == EiConstants.Extension.CORE }
            ?.contentRoot
            ?: return true

        val sdk = ModuleRootManager.getInstance(platformModule).sdk
            ?: return true
        val sdkVersion = JavaSdk.getInstance().getVersion(sdk)
            ?: return true
        val vmExecutablePath = (sdk.sdkType as? JavaSdkType)
            ?.getVMExecutablePath(sdk)
            ?: return true
        val vmBinPath = (sdk.sdkType as? JavaSdkType)
            ?.getBinPath(sdk)
            ?: return true

        val bootstrapDirectory = platformModuleRoot.resolve(ProjectConstants.Directory.BOOTSTRAP)

        val wslDistribution = WslDistributionManager.getInstance().installedDistributions.firstOrNull()
            ?.takeIf { WslPath.parseWindowsUncPath(bootstrapDirectory.pathString) != null }
            ?: return true

        if (!invokeCodeGeneration(context, platformModuleRoot, bootstrapDirectory, coreModuleRoot, vmExecutablePath, settings)) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(bootstrapDirectory)
            return false
        }
        if (!compileGeneratedSources(context, wslDistribution, platformModule, platformModuleRoot, bootstrapDirectory, vmBinPath, settings)) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(bootstrapDirectory)
            return false
        }
        if (!invokeModelsJarCreation(context, bootstrapDirectory)) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(bootstrapDirectory)
            return false
        }

        return true
    }

    private fun compileGeneratedSources(
        context: CompileContext,
        wslDistribution: WSLDistribution,
        platformModule: Module,
        platformModuleRoot: Path,
        bootstrapDirectory: Path,
        vmBinPath: String,
        settings: ProjectSettings,
    ): Boolean {
        val pathToBeDeleted = bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES)
        cleanDirectory(context, pathToBeDeleted)

        val gcl = getCodeGenerationCommandLineCompile(context, wslDistribution, bootstrapDirectory, platformModule, platformModuleRoot, vmBinPath)

        var result = false
        val handler = JavaCommandLineStateUtil.startProcess(gcl, true)
        handler.addProcessListener(object : ProcessListener {

            override fun startNotified(event: ProcessEvent) {
                context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started compilation of the generated code...", null, -1, -1)

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

        val waitFor = handler.waitFor(settings.generateCodeTimeoutSeconds * 1000L)
        if (!waitFor) {
            context.addMessage(CompilerMessageCategory.ERROR, "[y] Code generation failed after waiting for ${settings.generateCodeTimeoutSeconds} second(s).", null, -1, -1)
            handler.destroyProcess()
        }
        return result
    }

    private fun invokeCodeGeneration(
        context: CompileContext,
        platformModuleRoot: Path,
        bootstrapDirectory: Path,
        coreModuleRoot: Path,
        vmExecutablePath: String,
        settings: ProjectSettings,
    ): Boolean = startProcess(context, settings, bootstrapDirectory, ProjectConstants.Directory.GEN_SRC, "Code generation") {
        getCodeGenerationCommandLine(coreModuleRoot, bootstrapDirectory, platformModuleRoot, vmExecutablePath)
    }

    private fun startProcess(
        context: CompileContext,
        settings: ProjectSettings,
        bootstrapDirectory: Path,
        targetDirectory: String,
        action: String,
        gcl: () -> GeneralCommandLine,
    ): Boolean {
        val pathToBeDeleted = bootstrapDirectory.resolve(targetDirectory)
        cleanDirectory(context, pathToBeDeleted)

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

    private fun getCodeGenerationCommandLine(
        coreModuleRoot: Path,
        bootstrapDirectory: Path,
        platformModuleRoot: Path,
        vmExecutablePath: String
    ): GeneralCommandLine {
        val platformModuleRootWslPath = WslPath.parseWindowsUncPath(platformModuleRoot.toString())
        val platformModuleRootPath = platformModuleRootWslPath
            ?.linuxPath
            ?: platformModuleRoot.toString()

        val classpathSeparator = if (platformModuleRootWslPath != null) ":" else File.pathSeparator
        val classpath = setOf(
            osSpecificPath(coreModuleRoot.resolve("lib").pathString + "/*"),
            osSpecificPath(bootstrapDirectory.resolve(ProjectConstants.Directory.BIN).resolve("ybootstrap.jar").pathString)
        )
            .joinToString(classpathSeparator)

        val commandLine = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(platformModuleRoot)
            .withExePath(osSpecificPath(vmExecutablePath))
            .withCharset(Charsets.UTF_8)
            .withParameters(
                "-Dfile.encoding=UTF-8",
                "-cp",
                classpath,
                HybrisConstants.CLASS_FQN_CODE_GENERATOR,
                platformModuleRootPath
            )

        return commandLine
    }

    private fun getCodeGenerationCommandLineCompile(
        context: CompileContext,
        wslDistribution: WSLDistribution,
        bootstrapDirectory: Path,
        platformModule: Module,
        platformModuleRoot: Path,
        vmBinPath: String
    ): GeneralCommandLine {
        val rootManager = ModuleRootManager.getInstance(platformModule)
        val classpathSeparator = ":"
        val classpath = rootManager.orderEntries().compileOnly().recursively().exportedOnly().withoutSdk().pathsList.pathList
            .joinToString(classpathSeparator) { osSpecificPath(it) }

        val mntClasspathFile = Files.createTempFile("compile", "cx")
            .also { it.toFile().deleteOnExit() }
            .apply { Files.writeString(this, classpath, StandardCharsets.UTF_8) }
            .let { wslDistribution.getWslPath(it) }

        val sourceFiles = collectSourceFiles(bootstrapDirectory)
            .joinToString("\n") { osSpecificPath(it) }
        val mntSourceFilesFile = Files.createTempFile("sources", "cx")
            .also { it.toFile().deleteOnExit() }
            .apply { Files.writeString(this, sourceFiles, StandardCharsets.UTF_8) }
            .let { wslDistribution.getWslPath(it) }
        val outputDir = osSpecificPath(bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES))


        val commandLine = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkingDirectory(platformModuleRoot)
            .withExePath(osSpecificPath("$vmBinPath\\javac"))
            .withCharset(Charsets.UTF_8)
            .withParameters(
                "-nowarn",
                "-d",
                outputDir,
                "-cp",
                "@$mntClasspathFile",
                "@$mntSourceFilesFile"
            )

        patchCommandLineToRunViaWsl(wslDistribution, context, commandLine)

        return commandLine
    }

    private fun patchCommandLineToRunViaWsl(wslDistribution: WSLDistribution, context: CompileContext, commandLine: GeneralCommandLine) {
        val options = WSLCommandLineOptions()
            .setExecuteCommandInShell(true)
            .setLaunchWithWslExe(true)
        wslDistribution.patchCommandLine<GeneralCommandLine>(commandLine, context.project, options)
    }

    private fun invokeCodeCompilation(
        context: CompileContext,
        platformModule: Module,
        bootstrapDirectory: Path,
        sdkVersion: JavaSdkVersion
    ): Boolean {
        try {
            val pathToBeDeleted = bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES)
            cleanDirectory(context, pathToBeDeleted)

            context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started compilation of the generated code...", null, -1, -1)

            val options = buildCompileOptions(context, platformModule, sdkVersion)

            val rootManager = ModuleRootManager.getInstance(platformModule)
            val platformClasspath = rootManager.orderEntries().compileOnly().sdkOnly().pathsList.pathList
                .map { it.osSpecificFile }
            val classpath = rootManager.orderEntries().compileOnly().recursively().exportedOnly().withoutSdk().pathsList.pathList
                .map { it.osSpecificFile }

            val sourcePath = listOf(bootstrapDirectory.resolve(ProjectConstants.Directory.GEN_SRC).osSpecificFile)
            val sourceFiles = collectSourceFiles(bootstrapDirectory)
                .map { it.osSpecificFile }
            val outputDir = bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES).osSpecificFile

            val classes = CompilerManager.getInstance(context.project).compileJavaCode(
                options,
                platformClasspath,
                classpath,
                emptyList(),
                emptyList(),
                sourcePath,
                sourceFiles,
                outputDir
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

    private fun collectSourceFiles(bootstrapDirectory: Path): Set<Path> {
        val sourceFiles = mutableSetOf<Path>()
        Files.walkFileTree(
            bootstrapDirectory.resolve(ProjectConstants.Directory.GEN_SRC),
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.extension == "java" && file.name != "package-info.java") sourceFiles.add(file)
                    return super.visitFile(file, attrs)
                }
            })
        return sourceFiles
    }

    private fun JavaVersion.complianceOption() = if (feature < 5) "1.$feature" else feature.toString()

    private fun invokeModelsJarCreation(context: CompileContext, bootstrapDirectory: Path): Boolean {
        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started creation of the models.jar file...", null, -1, -1)

        val bootstrapDir = System.getenv(HybrisConstants.ENV_HYBRIS_BOOTSTRAP_BIN_DIR)
            ?.let { Paths.get(it) }
            ?: bootstrapDirectory.resolve(ProjectConstants.Directory.BIN)

        val modelsFile = bootstrapDir.resolve(HybrisConstants.JAR_MODELS).osSpecificFile
        if (modelsFile.exists()) modelsFile.delete()

        try {
            val bos = BufferedOutputStream(FileOutputStream(modelsFile))
            JarOutputStream(bos).use { jos ->
                ZipUtil.addFileOrDirRecursively(
                    jos,
                    null,
                    bootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES).osSpecificFile,
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

    private fun cleanDirectory(context: CompileContext, pathToBeDeleted: Path) {
        if (!pathToBeDeleted.exists()) return

        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Started cleaning the: $pathToBeDeleted", null, -1, -1)

        Files.walk(pathToBeDeleted)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)

        context.addMessage(CompilerMessageCategory.INFORMATION, "[y] Cleaned: $pathToBeDeleted", null, -1, -1)
    }

    private fun addAnnotationProcessingOptions(options: MutableList<String>, profile: AnnotationProcessingConfiguration) {
        if (!profile.isEnabled) {
            options.add("-proc:none")
            return
        }

        if (!profile.isObtainProcessorsFromClasspath) {
            val processorsPath = profile.processorPath
            options.add(if (profile.isUseProcessorModulePath) "--processor-module-path" else "-processorpath")
            options.add(FileUtil.toSystemDependentName(processorsPath.trim { it <= ' ' }))
        }

        val processors = profile.processors
        if (processors.isNotEmpty()) {
            options.add("-processor")
            options.add(StringUtil.join(processors, ","))
        }

        profile.processorOptions.entries.forEach {
            options.add("-A" + it.key + "=" + it.value)
        }
    }

    private fun buildCompileOptions(context: CompileContext, platformModule: Module, sdkVersion: JavaSdkVersion) = buildList {
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

    private val Path.osSpecificFile
        get() = this
            .pathString
            .let { osSpecificPath(it) }
            .let { File(it) }

    private val String.osSpecificFile
        get() = File(osSpecificPath(this))

    private fun osSpecificPath(path: Path) = path
        .pathString
        .let { osSpecificPath(it) }

    private fun osSpecificPath(path: String) = path
        .let { WslPath.parseWindowsUncPath(it)?.linuxPath ?: it }
}
