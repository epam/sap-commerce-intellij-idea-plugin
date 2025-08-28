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

package sap.commerce.toolset.typeSystem.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import sap.commerce.toolset.typeSystem.settings.state.TSFoldingSettingsState

@State(
    name = "[y] Type System Folding Settings",
    category = SettingsCategory.CODE,
    storages = [Storage(value = "editor.xml")]
)
@Service
class TSFoldingSettings : SerializablePersistentStateComponent<TSFoldingSettingsState>(TSFoldingSettingsState()), ModificationTracker {

    var enabled: Boolean
        get() = state.enabled
        set(value) {
            updateState { it.copy(enabled = value) }
        }
    var tablifyAtomics: Boolean
        get() = state.tablifyAtomics
        set(value) {
            updateState { it.copy(tablifyAtomics = value) }
        }
    var tablifyCollections: Boolean
        get() = state.tablifyCollections
        set(value) {
            updateState { it.copy(tablifyCollections = value) }
        }
    var tablifyMaps: Boolean
        get() = state.tablifyMaps
        set(value) {
            updateState { it.copy(tablifyMaps = value) }
        }
    var tablifyRelations: Boolean
        get() = state.tablifyRelations
        set(value) {
            updateState { it.copy(tablifyRelations = value) }
        }
    var tablifyItemAttributes: Boolean
        get() = state.tablifyItemAttributes
        set(value) {
            updateState { it.copy(tablifyItemAttributes = value) }
        }
    var tablifyItemIndexes: Boolean
        get() = state.tablifyItemIndexes
        set(value) {
            updateState { it.copy(tablifyItemIndexes = value) }
        }
    var tablifyItemCustomProperties: Boolean
        get() = state.tablifyItemCustomProperties
        set(value) {
            updateState { it.copy(tablifyItemCustomProperties = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(): TSFoldingSettings = application.service()
    }
}
