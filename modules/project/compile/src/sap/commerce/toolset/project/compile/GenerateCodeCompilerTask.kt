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

import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.util.application
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.compile.context.CompileTaskContext
import sap.commerce.toolset.project.compile.context.TaskContext
import sap.commerce.toolset.project.compile.context.WslCompileTaskContext
import sap.commerce.toolset.project.compile.tasks.GenerateCodePreCompileTask
import sap.commerce.toolset.project.compile.tasks.PreCompileTask
import sap.commerce.toolset.project.compile.tasks.WslGenerateCodePreCompileTask
import sap.commerce.toolset.project.contentRoot
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.yExtensionName
import kotlin.io.path.pathString

class GenerateCodeCompilerTask : CompileTask {

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

        val task = getPreCompileTask(context)
            ?: return true

        if (!task.invokeCodeGeneration()) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(task.taskContext.bootstrapDirectory)
            return false
        }
        if (!task.invokeCodeCompilation()) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(task.taskContext.bootstrapDirectory)
            return false
        }
        if (!task.invokeModelsJarCreation()) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(task.taskContext.bootstrapDirectory)
            return false
        }

        return true
    }

    private fun getPreCompileTask(context: CompileContext): PreCompileTask<out TaskContext>? {
        val modules = application.runReadAction<Array<Module>> { context.compileScope.affectedModules }
        val moduleMapping = context.project.ySettings.module2extensionMapping
        val platformModule = modules.firstOrNull { it.yExtensionName(moduleMapping) == EiConstants.Extension.PLATFORM }
            ?: return null

        val platformModuleRoot = platformModule.contentRoot
            ?: return null
        val coreModuleRoot = modules
            .firstOrNull { it.yExtensionName(moduleMapping) == EiConstants.Extension.CORE }
            ?.contentRoot
            ?: return null

        val sdk = ModuleRootManager.getInstance(platformModule).sdk
            ?: return null
        val sdkVersion = JavaSdk.getInstance().getVersion(sdk)
            ?: return null
        val javaSdkType = sdk.sdkType as? JavaSdkType
            ?: return null
        val vmExecutablePath = javaSdkType
            .getVMExecutablePath(sdk)
            ?: return null
        val vmBinPath = javaSdkType
            .getBinPath(sdk)
            ?: return null

        val bootstrapDirectory = platformModuleRoot.resolve(ProjectConstants.Directory.BOOTSTRAP)
        val wslDistribution = WslPath.parseWindowsUncPath(bootstrapDirectory.pathString)
            ?.distribution

        return if (wslDistribution != null) {
            WslGenerateCodePreCompileTask(
                WslCompileTaskContext(
                    context = context,
                    wslDistribution = wslDistribution,
                    platformModuleRoot = platformModuleRoot,
                    bootstrapDirectory = bootstrapDirectory,
                    coreModuleRoot = coreModuleRoot,
                    vmExecutablePath = vmExecutablePath,
                    sdkVersion = sdkVersion,
                    vmBinPath = vmBinPath,
                    platformModule = platformModule
                )
            )
        } else GenerateCodePreCompileTask(
            CompileTaskContext(
                context = context,
                platformModuleRoot = platformModuleRoot,
                bootstrapDirectory = bootstrapDirectory,
                coreModuleRoot = coreModuleRoot,
                vmExecutablePath = vmExecutablePath,
                sdkVersion = sdkVersion,
                platformModule = platformModule
            )
        )
    }
}
