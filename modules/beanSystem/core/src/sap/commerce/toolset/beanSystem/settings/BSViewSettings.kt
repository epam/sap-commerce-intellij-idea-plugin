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
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.settings.event.BSViewSettingsListener
import sap.commerce.toolset.beanSystem.settings.state.BSViewSettingsState
import sap.commerce.toolset.beanSystem.settings.state.ChangeType

@State(name = "[y] Bean System View settings", category = SettingsCategory.PLUGINS)
@Storage(value = HybrisConstants.STORAGE_HYBRIS_BS_VIEW, roamingType = RoamingType.LOCAL)
@Service(Service.Level.PROJECT)
class BSViewSettings(private val project: Project) : SerializablePersistentStateComponent<BSViewSettingsState>(BSViewSettingsState()) {

    fun fireSettingsChanged(changeType: ChangeType) = changeType.also { project.messageBus.syncPublisher(BSViewSettingsListener.TOPIC).settingsChanged(changeType) }

    var showCustomOnly
        get() = state.showCustomOnly
        set(value) {
            updateState { it.copy(showCustomOnly = value) }
        }
    var showDeprecatedOnly
        get() = state.showDeprecatedOnly
        set(value) {
            updateState { it.copy(showDeprecatedOnly = value) }
        }
    var showEnumValues
        get() = state.showEnumValues
        set(value) {
            updateState { it.copy(showEnumValues = value) }
        }
    var showBeanProperties
        get() = state.showBeanProperties
        set(value) {
            updateState { it.copy(showBeanProperties = value) }
        }

    companion object {
        fun getInstance(project: Project): BSViewSettings = project.service()
    }
}