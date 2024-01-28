/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote

import ai.grazie.utils.toLinkedSet
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.openapi.project.Project
import java.util.*

object RemoteConnectionUtil {

    fun generateUrl(ssl: Boolean, host: String?, port: String?, webroot: String?) = buildString {
        if (ssl) append(HybrisConstants.HTTPS_PROTOCOL)
        else append(HybrisConstants.HTTP_PROTOCOL)
        append(host?.trim() ?: "")
        port
            ?.takeIf { it.isNotBlank() }
            ?.takeUnless { "443" == it && ssl }
            ?.takeUnless { "80" == it && !ssl }
            ?.let {
                append(HybrisConstants.URL_PORT_DELIMITER)
                append(it)
            }
            ?: ""
        webroot
            ?.takeUnless { it.isBlank() }
            ?.let {
                append('/')
                append(
                    it
                        .trimStart(' ', '/')
                        .trimEnd(' ', '/')
                )
            }
            ?: ""
    }

    fun getActiveRemoteConnectionSettings(project: Project, type: RemoteConnectionType): HybrisRemoteConnectionSettings {
        val instances = getRemoteConnections(project, type)
        if (instances.isEmpty()) return getDefaultRemoteConnectionSettings(project, type)

        val id = getActiveRemoteConnectionId(project, type)

        return instances
            .firstOrNull { id == it.uuid }
            ?: instances.first()
    }

    fun getActiveRemoteConnectionId(project: Project, type: RemoteConnectionType) = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
        .let {
            when (type) {
                RemoteConnectionType.Hybris -> it.activeRemoteConnectionID
                RemoteConnectionType.SOLR -> it.activeSolrConnectionID
            }
        }

    fun getDefaultRemoteConnectionSettings(project: Project, type: RemoteConnectionType) = HybrisRemoteConnectionSettings()
        .also {
            it.type = type
            when (type) {
                RemoteConnectionType.Hybris -> {
                    it.port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_TOMCAT_SSL_PORT, "9002")
                    it.hacWebroot = getPropertyOrDefault(project, HybrisConstants.PROPERTY_HAC_WEBROOT, "")
                    it.hacPassword = getPropertyOrDefault(project, HybrisConstants.PROPERTY_ADMIN_INITIAL_PASSWORD, "nimda")
                    it.sslProtocol = HybrisConstants.DEFAULT_SSL_PROTOCOL
                }

                RemoteConnectionType.SOLR -> {
                    it.port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_PORT, "8983")
                    it.adminLogin = getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_USER, "solrserver")
                    it.adminPassword = getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_PASSWORD, "server123")
                }
            }
        }

    fun getRemoteConnections(project: Project, type: RemoteConnectionType): Collection<HybrisRemoteConnectionSettings> {
        val projectLevelSettings = getProjectLevelSettings(project, type)
        val projectPersonalLevelSettings = getProjectPersonalLevelSettings(project, type)

        return (projectPersonalLevelSettings + projectLevelSettings)
            .toLinkedSet()
    }

    fun addRemoteConnection(project: Project, settings: HybrisRemoteConnectionSettings) {
        if (settings.uuid == null) {
            settings.uuid = UUID.randomUUID().toString()
        }

        when (settings.scope) {
            RemoteConnectionScope.PROJECT_PERSONAL -> {
                val state = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
                state.remoteConnectionSettingsList.add(settings)
            }

            RemoteConnectionScope.PROJECT -> {
                val state = HybrisProjectSettingsComponent.getInstance(project).state
                state.remoteConnectionSettingsList.add(settings)
            }
        }
    }

    fun saveRemoteConnections(project: Project, type: RemoteConnectionType, settings: Collection<HybrisRemoteConnectionSettings>) {
        HybrisProjectSettingsComponent.getInstance(project).state
            .remoteConnectionSettingsList
            .removeIf { it.type == type }
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state
            .remoteConnectionSettingsList
            .removeIf { it.type == type }

        if (settings.isEmpty()) addRemoteConnection(project, getDefaultRemoteConnectionSettings(project, type))
        else settings.forEach { addRemoteConnection(project, it) }
    }

    fun setActiveRemoteConnectionSettings(project: Project, settings: HybrisRemoteConnectionSettings) {
        val developerSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state

        when (settings.type) {
            RemoteConnectionType.Hybris -> {
                developerSettings.activeRemoteConnectionID = settings.uuid
            }

            RemoteConnectionType.SOLR -> {
                developerSettings.activeSolrConnectionID = settings.uuid
            }
        }
    }

    fun changeRemoteConnectionScope(project: Project, settings: HybrisRemoteConnectionSettings, originalScope: RemoteConnectionScope) {
        val remoteConnectionSettings = when (originalScope) {
            RemoteConnectionScope.PROJECT_PERSONAL -> HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state.remoteConnectionSettingsList
            RemoteConnectionScope.PROJECT -> HybrisProjectSettingsComponent.getInstance(project).state.remoteConnectionSettingsList
        }
        remoteConnectionSettings.remove(settings)
        addRemoteConnection(project, settings)
    }

    private fun getProjectPersonalLevelSettings(
        project: Project,
        type: RemoteConnectionType
    ) = getRemoteConnectionSettings(
        type,
        RemoteConnectionScope.PROJECT_PERSONAL,
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).state.remoteConnectionSettingsList
    )

    private fun getProjectLevelSettings(
        project: Project,
        type: RemoteConnectionType
    ) = getRemoteConnectionSettings(
        type,
        RemoteConnectionScope.PROJECT,
        HybrisProjectSettingsComponent.getInstance(project).state.remoteConnectionSettingsList
    )

    private fun getRemoteConnectionSettings(
        type: RemoteConnectionType,
        scope: RemoteConnectionScope,
        settings: MutableList<HybrisRemoteConnectionSettings>
    ) = settings
        .filter { it.type == type }
        .filter { it.uuid?.isNotBlank() ?: false }
        .onEach { it.scope = scope }

    private fun getPropertyOrDefault(project: Project, key: String, fallback: String) = PropertyService.getInstance(project)
        ?.findProperty(key)
        ?: fallback
}