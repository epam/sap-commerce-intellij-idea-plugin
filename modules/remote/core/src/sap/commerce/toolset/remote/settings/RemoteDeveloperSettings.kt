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

package sap.commerce.toolset.remote.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.remote.settings.state.DeveloperSettingsState

@State(
    name = "[y] Remote Developer Settings",
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service(Service.Level.PROJECT)
class RemoteDeveloperSettings : SerializablePersistentStateComponent<DeveloperSettingsState>(DeveloperSettingsState()) {

    var activeRemoteConnectionID
        get() = state.activeRemoteConnectionID
        set(value) {
            updateState { it.copy(activeRemoteConnectionID = value) }
        }
    var activeSolrConnectionID
        get() = state.activeSolrConnectionID
        set(value) {
            updateState { it.copy(activeSolrConnectionID = value) }
        }
    var remoteConnectionSettingsList
        get() = state.remoteConnectionSettingsList
        set(value) {
            updateState { it.copy(remoteConnectionSettingsList = value) }
        }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): RemoteDeveloperSettings = project.service()
    }
}