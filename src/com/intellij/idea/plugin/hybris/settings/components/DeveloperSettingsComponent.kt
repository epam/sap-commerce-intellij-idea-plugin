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
package com.intellij.idea.plugin.hybris.settings.components

import com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.idea.plugin.hybris.settings.SUser
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "HybrisDeveloperSpecificProjectSettings",
    storages = [Storage(value = STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service(Service.Level.PROJECT)
class DeveloperSettingsComponent : PersistentStateComponent<DeveloperSettings> {

    private val state = DeveloperSettings()

    override fun getState() = state
    override fun loadState(state: DeveloperSettings) = XmlSerializerUtil.copyBean(state, this.state)

    fun getActiveCCv2Subscription() = state.activeCCv2SubscriptionID
        ?.let { ApplicationSettingsComponent.getInstance().getCCv2Subscription(it) }

    fun getSUser(id: String) = state
        .ccv2Settings
        .sUsers[id]
        ?: SUser()
            .also {
                it.id = id
            }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): DeveloperSettingsComponent = project.service()
    }
}
