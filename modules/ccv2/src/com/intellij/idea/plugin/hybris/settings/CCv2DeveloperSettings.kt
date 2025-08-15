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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.DeveloperSettingsState
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.SUser
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(
    name = "[y] CCv2 Developer Settings",
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service(Service.Level.PROJECT)
class CCv2DeveloperSettings : SerializablePersistentStateComponent<DeveloperSettingsState>(DeveloperSettingsState()) {

    var activeCCv2SubscriptionID
        get() = state.activeCCv2SubscriptionID
        set(value) {
            updateState { it.copy(activeCCv2SubscriptionID = value) }
        }
    var ccv2Settings
        get() = state.ccv2Settings
        set(value) {
            updateState { it.copy(ccv2Settings = value) }
        }

    fun getActiveCCv2Subscription() = activeCCv2SubscriptionID
        ?.let { CCv2ProjectSettings.getInstance().getCCv2Subscription(it) }

    fun getSUser(id: String) = ccv2Settings
        .sUsers[id]
        ?: SUser()
            .also {
                it.id = id
            }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CCv2DeveloperSettings = project.service()
    }
}