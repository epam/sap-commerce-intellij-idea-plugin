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

package sap.commerce.toolset.project.configurator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.util.fileExists
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ProjectIconConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Project Icon"

    override suspend fun configure(context: ProjectImportContext) {
        val ideaDirectory = context.rootDirectory.resolve(".idea")

        val target = ideaDirectory.resolve("icon.svg")
        val targetDark = ideaDirectory.resolve("icon_dark.svg")

        // do not override existing Icon
        if (target.fileExists) return

        val projectIconFile = context.projectIconFile
        if (projectIconFile == null) {
            this::class.java.getResourceAsStream("/icons/hybrisIcon.svg")
                ?.use { input ->
                    withContext(Dispatchers.IO) {
                        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            // as for now, Dark icon supported only for Plugin's icons
            this::class.java.getResourceAsStream("/icons/hybrisIcon_dark.svg")
                ?.use { input ->
                    withContext(Dispatchers.IO) {
                        Files.copy(input, targetDark, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
        } else {
            withContext(Dispatchers.IO) {
                Files.copy(projectIconFile, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}