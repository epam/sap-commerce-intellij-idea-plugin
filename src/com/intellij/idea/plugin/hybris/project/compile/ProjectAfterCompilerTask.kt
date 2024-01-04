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
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.vfs.LocalFileSystem

class ProjectAfterCompilerTask : CompileTask {

    override fun execute(context: CompileContext): Boolean {
        if (!context.isRebuild) return true
        val settings = HybrisProjectSettingsComponent.getInstance(context.project)
        if (!settings.isHybrisProject()) return true
        if (!settings.state.generateCodeOnRebuild) return true

        val bootstrapDirectory = ModuleManager.getInstance(context.project).modules
            .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_PLATFORM }
            ?.root()
            ?.resolve(HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY)
            ?: return true

        LocalFileSystem.getInstance().refreshNioFiles(
            listOf(
                bootstrapDirectory.resolve(HybrisConstants.GEN_SRC_DIRECTORY),
                bootstrapDirectory.resolve(HybrisConstants.PLATFORM_MODEL_CLASSES_DIRECTORY),
                bootstrapDirectory.resolve(HybrisConstants.BIN_DIRECTORY).resolve(HybrisConstants.JAR_MODELS),
            ), true, true, null
        )

        return true
    }
}
