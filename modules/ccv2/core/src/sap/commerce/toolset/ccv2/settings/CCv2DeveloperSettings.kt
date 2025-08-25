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

package sap.commerce.toolset.ccv2.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.event.CCv2SettingsListener
import sap.commerce.toolset.ccv2.settings.state.CCv2DeveloperSettingsState
import sap.commerce.toolset.ccv2.settings.state.SUser

@State(
    name = "[y] CCv2 Developer Settings",
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.LOCAL)]
)
@Service(Service.Level.PROJECT)
class CCv2DeveloperSettings(private val project: Project) : SerializablePersistentStateComponent<CCv2DeveloperSettingsState>(CCv2DeveloperSettingsState()), ModificationTracker {

    var activeCCv2SubscriptionID
        get() = state.activeCCv2SubscriptionID
        set(value) {
            updateState { it.copy(activeCCv2SubscriptionID = value) }

            project.messageBus
                .syncPublisher(CCv2SettingsListener.TOPIC)
                .onActivation(getActiveCCv2Subscription())
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
        ?: SUser(
            id = id,
        )

    override fun getModificationCount() = stateModificationCount

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CCv2DeveloperSettings = project.service()
    }
}