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

package sap.commerce.toolset.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.settings.state.WorkspaceSettingsState

@State(
    name = "HybrisProjectWorkspaceSettings",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE), ]
)
@Service(Service.Level.PROJECT)
class WorkspaceSettings : SerializablePersistentStateComponent<WorkspaceSettingsState>(WorkspaceSettingsState()), ModificationTracker {

    var hybrisProject
        get() = state.hybrisProject
        set(value) {
            updateState { it.copy(hybrisProject = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): WorkspaceSettings = project.service()
    }
}