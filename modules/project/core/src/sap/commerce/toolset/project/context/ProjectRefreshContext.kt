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

package sap.commerce.toolset.project.context

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import java.nio.file.Path

data class ProjectRefreshContext(
    val project: Project,
    val projectPath: Path,
    val importSettings: ProjectImportSettings,
    val removeExternalModules: Boolean,
) {
    val workspace = WorkspaceModel.getInstance(project)

    fun mutable() = Mutable(
        project = project,
        projectPath = projectPath,
        importSettings = this@ProjectRefreshContext.importSettings.mutable(),
        removeExternalModules = AtomicBooleanProperty(removeExternalModules),
    )

    data class Mutable(
        val project: Project,
        val projectPath: Path,
        val importSettings: ProjectImportSettings.Mutable,
        val removeExternalModules: AtomicBooleanProperty,
    ) {
        fun immutable() = ProjectRefreshContext(
            project = project,
            projectPath = projectPath,
            importSettings = importSettings.immutable(),
            removeExternalModules = removeExternalModules.get(),
        )
    }
}