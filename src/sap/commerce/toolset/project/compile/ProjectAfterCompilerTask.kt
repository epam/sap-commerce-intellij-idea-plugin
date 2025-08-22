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
package sap.commerce.toolset.project.compile

import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.root
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.yExtensionName

class ProjectAfterCompilerTask : CompileTask {

    override fun execute(context: CompileContext): Boolean = application.runReadAction<Boolean> {
        val project = context.project
        val settings = ProjectSettings.getInstance(project)
        if (!project.isHybrisProject) return@runReadAction true
        if (!settings.generateCodeOnRebuild) return@runReadAction true

        val typeId = context.compileScope.getUserData(CompilerManager.RUN_CONFIGURATION_TYPE_ID_KEY)
        // do not rebuild sources in case of JUnit
        // see JUnitConfigurationType
        if ("JUnit" == typeId && !settings.generateCodeOnJUnitRunConfiguration) return@runReadAction true

        val modules = context.compileScope.affectedModules
        val platformModule = modules.firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_PLATFORM }
            ?: return@runReadAction true

        val bootstrapDirectory = platformModule
            .root()
            ?.resolve(HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY)
            ?: return@runReadAction true

        ProjectCompileService.getInstance(project).triggerRefreshGeneratedFiles(bootstrapDirectory)

        return@runReadAction true
    }
}
