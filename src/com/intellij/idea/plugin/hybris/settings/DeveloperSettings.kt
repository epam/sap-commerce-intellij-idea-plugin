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
import com.intellij.idea.plugin.hybris.settings.state.DeveloperSettingsState
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.SUser
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(
    name = "HybrisDeveloperSpecificProjectSettings",
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service(Service.Level.PROJECT)
class DeveloperSettings : SerializablePersistentStateComponent<DeveloperSettingsState>(DeveloperSettingsState()) {

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
    var activeCCv2SubscriptionID
        get() = state.activeCCv2SubscriptionID
        set(value) {
            updateState { it.copy(activeCCv2SubscriptionID = value) }
        }
    var remoteConnectionSettingsList
        get() = state.remoteConnectionSettingsList
        set(value) {
            updateState { it.copy(remoteConnectionSettingsList = value) }
        }
    var typeSystemDiagramSettings
        get() = state.typeSystemDiagramSettings
        set(value) {
            updateState { it.copy(typeSystemDiagramSettings = value) }
        }
    var beanSystemSettings
        get() = state.beanSystemSettings
        set(value) {
            updateState { it.copy(beanSystemSettings = value) }
        }
    var typeSystemSettings
        get() = state.typeSystemSettings
        set(value) {
            updateState { it.copy(typeSystemSettings = value) }
        }
    var cngSettings
        get() = state.cngSettings
        set(value) {
            updateState { it.copy(cngSettings = value) }
        }
    var bpSettings
        get() = state.bpSettings
        set(value) {
            updateState { it.copy(bpSettings = value) }
        }
    var flexibleSearchSettings
        get() = state.flexibleSearchSettings
        set(value) {
            updateState { it.copy(flexibleSearchSettings = value) }
        }
    var aclSettings
        get() = state.aclSettings
        set(value) {
            updateState { it.copy(aclSettings = value) }
        }
    var polyglotQuerySettings
        get() = state.polyglotQuerySettings
        set(value) {
            updateState { it.copy(polyglotQuerySettings = value) }
        }
    var impexSettings
        get() = state.impexSettings
        set(value) {
            updateState { it.copy(impexSettings = value) }
        }
    var groovySettings
        get() = state.groovySettings
        set(value) {
            updateState { it.copy(groovySettings = value) }
        }
    var jspSettings
        get() = state.jspSettings
        set(value) {
            updateState { it.copy(jspSettings = value) }
        }
    var ccv2Settings
        get() = state.ccv2Settings
        set(value) {
            updateState { it.copy(ccv2Settings = value) }
        }

    fun getActiveCCv2Subscription() = state.activeCCv2SubscriptionID
        ?.let { ApplicationSettings.getInstance().getCCv2Subscription(it) }

    fun getSUser(id: String) = state
        .ccv2Settings
        .sUsers[id]
        ?: SUser()
            .also {
                it.id = id
            }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): DeveloperSettings = project.service()
    }
}