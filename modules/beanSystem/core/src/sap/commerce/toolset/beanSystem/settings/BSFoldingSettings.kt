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

package sap.commerce.toolset.beanSystem.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import sap.commerce.toolset.beanSystem.settings.state.BSFoldingSettingsState

@State(
    name = "[y] Bean System Folding Settings",
    category = SettingsCategory.CODE,
    storages = [Storage(value = "editor.xml")]
)
@Service
class BSFoldingSettings : SerializablePersistentStateComponent<BSFoldingSettingsState>(BSFoldingSettingsState()), ModificationTracker {

    var enabled: Boolean
        get() = state.enabled
        set(value) {
            updateState { it.copy(enabled = value) }
        }
    var tablifyProperties: Boolean
        get() = state.tablifyProperties
        set(value) {
            updateState { it.copy(tablifyProperties = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(): BSFoldingSettings = application.service()
    }
}
