/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.groovy.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.groovy.settings.state.GroovySettingsState

@State(
    name = "[y] Groovy Developer Settings",
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.LOCAL)]
)
@Service(Service.Level.PROJECT)
class GroovyDeveloperSettings : SerializablePersistentStateComponent<GroovySettingsState>(GroovySettingsState()), ModificationTracker {

    var enableActionsToolbar
        get() = state.enableActionsToolbar
        set(value) {
            updateState { it.copy(enableActionsToolbar = value) }
        }

    var enableActionsToolbarForGroovyTest
        get() = state.enableActionsToolbarForGroovyTest
        set(value) {
            updateState { it.copy(enableActionsToolbarForGroovyTest = value) }
        }

    var enableActionsToolbarForGroovyIdeConsole
        get() = state.enableActionsToolbarForGroovyIdeConsole
        set(value) {
            updateState { it.copy(enableActionsToolbarForGroovyIdeConsole = value) }
        }

    var springContextMode
        get() = state.springContextMode
        set(value) {
            updateState { it.copy(springContextMode = value) }
        }

    var transactionMode
        get() = state.transactionMode
        set(value) {
            updateState { it.copy(transactionMode = value) }
        }

    var execMode
        get() = state.execMode
        set(value) {
            updateState { it.copy(execMode = value) }
        }

    var exceptionHandling
        get() = state.exceptionHandling
        set(value) {
            updateState { it.copy(exceptionHandling = value) }
        }

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): GroovyDeveloperSettings = project.service()
    }
}