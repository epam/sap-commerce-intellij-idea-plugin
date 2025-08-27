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

package sap.commerce.toolset.cockpitNG.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import sap.commerce.toolset.cockpitNG.settings.state.CngFoldingSettingsState

@State(
    name = "[y] CockpitNG Folding Settings",
    category = SettingsCategory.CODE,
    storages = [Storage(value = "editor.xml")]
)
@Service
class CngFoldingSettings : SerializablePersistentStateComponent<CngFoldingSettingsState>(CngFoldingSettingsState()), ModificationTracker {

    var enabled: Boolean
        get() = state.enabled
        set(value) {
            updateState { it.copy(enabled = value) }
        }
    var tablifyWizardProperties: Boolean
        get() = state.tablifyWizardProperties
        set(value) {
            updateState { it.copy(tablifyWizardProperties = value) }
        }
    var tablifyNavigationNodes: Boolean
        get() = state.tablifyNavigationNodes
        set(value) {
            updateState { it.copy(tablifyNavigationNodes = value) }
        }
    var tablifySearchFields: Boolean
        get() = state.tablifySearchFields
        set(value) {
            updateState { it.copy(tablifySearchFields = value) }
        }
    var tablifyListColumns: Boolean
        get() = state.tablifyListColumns
        set(value) {
            updateState { it.copy(tablifyListColumns = value) }
        }
    var tablifyParameters: Boolean
        get() = state.tablifyParameters
        set(value) {
            updateState { it.copy(tablifyParameters = value) }
        }
    var tablifyMolds: Boolean
        get() = state.tablifyMolds
        set(value) {
            updateState { it.copy(tablifyMolds = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(): CngFoldingSettings = application.service()
    }
}
