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

package com.intellij.idea.plugin.hybris.project.compile

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class ProjectCompileService(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun triggerRefreshGeneratedFiles(bootstrapDirectory: Path) {
        coroutineScope.launch(Dispatchers.IO) {
            val paths = listOf(
                bootstrapDirectory.resolve(HybrisConstants.GEN_SRC_DIRECTORY),
                bootstrapDirectory.resolve(HybrisConstants.PLATFORM_MODEL_CLASSES_DIRECTORY),
                bootstrapDirectory.resolve(HybrisConstants.BIN_DIRECTORY).resolve(HybrisConstants.JAR_MODELS),
            )

            withBackgroundProgress(project, "Refreshing generated files") {
                LocalFileSystem.getInstance().refreshNioFiles(paths, true, true, null)
            }
        }
    }

    companion object {
        fun getInstance(project: Project): ProjectCompileService = project.service()
    }
}