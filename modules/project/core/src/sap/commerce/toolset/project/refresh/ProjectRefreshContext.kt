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

package sap.commerce.toolset.project.refresh

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import sap.commerce.toolset.project.descriptor.ProjectImportContext
import sap.commerce.toolset.project.settings.ProjectSettings
import java.nio.file.Path

data class ProjectRefreshContext(
    val project: Project,
    val projectPath: Path,
    val projectSettings: ProjectSettings,
    val importContext: ProjectImportContext,
    val removeOldProjectData: Boolean,
    val removeExternalModules: Boolean,
) {
    fun mutable() = Mutable(
        project = project,
        projectPath = projectPath,
        projectSettings = projectSettings,
        importContext = importContext.mutable(),
        removeOldProjectData = AtomicBooleanProperty(removeOldProjectData),
        removeExternalModules = AtomicBooleanProperty(removeExternalModules),
    )

    data class Mutable(
        val project: Project,
        val projectPath: Path,
        val projectSettings: ProjectSettings,
        val importContext: ProjectImportContext.Mutable,
        val removeOldProjectData: AtomicBooleanProperty,
        val removeExternalModules: AtomicBooleanProperty,
    ) {
        fun immutable() = ProjectRefreshContext(
            project = project,
            projectPath = projectPath,
            projectSettings = projectSettings,
            importContext = importContext.immutable(),
            removeOldProjectData = removeOldProjectData.get(),
            removeExternalModules = removeExternalModules.get(),
        )
    }
}