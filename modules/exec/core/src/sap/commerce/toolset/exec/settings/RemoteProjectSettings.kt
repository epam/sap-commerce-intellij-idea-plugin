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

package sap.commerce.toolset.exec.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exec.settings.state.ProjectSettingsState

@State(
    name = "[y] Remote Project Settings",
    storages = [
        Storage(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED),
    ]
)
@Service(Service.Level.PROJECT)
class RemoteProjectSettings : SerializablePersistentStateComponent<ProjectSettingsState>(ProjectSettingsState()), ModificationTracker {

    var remoteConnectionSettingsList
        get() = state.remoteConnectionSettingsList
        set(value) {
            updateState { it.copy(remoteConnectionSettingsList = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): RemoteProjectSettings = project.service()
    }
}