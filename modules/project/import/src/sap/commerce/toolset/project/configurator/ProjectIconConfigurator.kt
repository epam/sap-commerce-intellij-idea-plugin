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

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class ProjectIconConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Project Icon"

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val rootDirectory = hybrisProjectDescriptor.rootDirectory ?: return

        val target = Paths.get(rootDirectory.path, ".idea", "icon.svg")
        val targetDark = Paths.get(rootDirectory.path, ".idea", "icon_dark.svg")

        // do not override existing Icon
        if (Files.exists(target)) return

        val projectIconFile = hybrisProjectDescriptor.projectIconFile
        if (projectIconFile == null) {
            this::class.java.getResourceAsStream("/icons/hybrisIcon.svg")
                ?.use { input ->
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
            }
            // as for now, Dark icon supported only for Plugin's icons
            this::class.java.getResourceAsStream("/icons/hybrisIcon_dark.svg")
                ?.use { input ->
                Files.copy(input, targetDark, StandardCopyOption.REPLACE_EXISTING)
            }
        } else {
            FileInputStream(projectIconFile).use { input ->
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}