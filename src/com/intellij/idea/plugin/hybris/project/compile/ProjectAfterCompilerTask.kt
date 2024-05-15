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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.root
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager

class ProjectAfterCompilerTask : CompileTask {

    override fun execute(context: CompileContext) = ApplicationManager.getApplication().runReadAction<Boolean> {
        val settings = ProjectSettingsComponent.getInstance(context.project)
        if (!settings.isHybrisProject()) return@runReadAction true
        if (!settings.state.generateCodeOnRebuild) return@runReadAction true

        val typeId = context.compileScope.getUserData(CompilerManager.RUN_CONFIGURATION_TYPE_ID_KEY)
        // do not rebuild sources in case of JUnit
        // see JUnitConfigurationType
        if ("JUnit" == typeId && !settings.state.generateCodeOnJUnitRunConfiguration) return@runReadAction true

        val modules = context.compileScope.affectedModules
        val platformModule = modules.firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_PLATFORM }
            ?: return@runReadAction true

        val bootstrapDirectory = platformModule
            .root()
            ?.resolve(HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY)
            ?: return@runReadAction true

        ProjectCompileUtil.triggerRefreshGeneratedFiles(bootstrapDirectory)

        return@runReadAction true
    }
}
