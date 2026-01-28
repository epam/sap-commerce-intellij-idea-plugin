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
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.compile.context.CompileTaskContext
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
        if (HybrisConstants.RunConfiguration.JUNIT == typeId && !settings.generateCodeOnJUnitRunConfiguration) return true
        if (HybrisConstants.RunConfiguration.SAP_CX == typeId && !settings.generateCodeOnServerRunConfiguration) return true

        val preCompileTask = getPreCompileTask(context) ?: return true

        if (!preCompileTask.invokeCodeGeneration()
            || !preCompileTask.invokeCodeCompilation()
            || !preCompileTask.invokeModelsJarCreation()
        ) {
            ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(preCompileTask.taskContext.bootstrapPath)
            return false
        }

        return true
    }

    private fun getPreCompileTask(context: CompileContext): PreCompileTask? {
        val moduleMapping = context.project.ySettings.module2extensionMapping
        val modules = application.runReadAction<Array<Module>> { context.compileScope.affectedModules }
            .associateBy { it.yExtensionName(moduleMapping) }

        val platformModule = modules[EiConstants.Extension.PLATFORM] ?: return null
        val coreModulePath = modules[EiConstants.Extension.CORE]?.contentRoot ?: return null
        val platformModulePath = platformModule.contentRoot ?: return null
        val sdk = ModuleRootManager.getInstance(platformModule).sdk ?: return null
        val sdkVersion = JavaSdk.getInstance().getVersion(sdk) ?: return null
        val javaSdkType = sdk.sdkType.asSafely<JavaSdkType>() ?: return null
        val vmExecutablePath = javaSdkType.getVMExecutablePath(sdk) ?: return null

        val taskContext = CompileTaskContext(
            context = context,
            sdkVersion = sdkVersion,
            platformModule = platformModule,
            platformModulePath = platformModulePath,
            coreModulePath = coreModulePath,
            vmExecutablePath = vmExecutablePath,
        )

        return WslPath.parseWindowsUncPath(platformModulePath.pathString)
            ?.distribution
            ?.let { wslDistribution ->
                val wslContext = javaSdkType.getBinPath(sdk)
                    ?.let { WslCompileTaskContext(wslDistribution, it) }
                    ?: return null

                WslGenerateCodePreCompileTask(taskContext, wslContext)
            }
            ?: GenerateCodePreCompileTask(taskContext)
    }
}
