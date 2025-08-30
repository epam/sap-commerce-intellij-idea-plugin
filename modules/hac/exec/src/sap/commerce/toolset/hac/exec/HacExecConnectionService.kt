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

package sap.commerce.toolset.hac.exec

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exec.ExecConnectionService
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.hac.exec.settings.HacExecDeveloperSettings
import sap.commerce.toolset.hac.exec.settings.HacExecProjectSettings
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

@Service(Service.Level.PROJECT)
class HacExecConnectionService(project: Project) : ExecConnectionService<HacConnectionSettingsState>(project) {

    private val lock = Any()

    override var activeConnection: HacConnectionSettingsState
        get() = findActiveConnection()
        set(value) {
            HacExecDeveloperSettings.getInstance(project).activeConnectionUUID = value.uuid

            onActivate(value)
        }

    override val connections: List<HacConnectionSettingsState>
        get() = persistedConnections()
            ?: synchronized(lock) {
                persistedConnections()
                    ?: listOf(default()).also {
                        val defaultSettings = it.first()

                        HacExecDeveloperSettings.getInstance(project).connections = it
                        activeConnection = defaultSettings
                    }
            }

    override val listener: HacConnectionSettingsListener
        get() = project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC)

    override fun create(settings: Pair<HacConnectionSettingsState, Credentials>, notify: Boolean) = when (settings.first.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(HacExecDeveloperSettings.getInstance(project)) {
            connections = connections + settings.first

            onCreate(settings, notify)
        }

        ExecConnectionScope.PROJECT -> with(HacExecProjectSettings.getInstance(project)) {
            connections = connections + settings.first

            onCreate(settings, notify)
        }
    }

    override fun delete(settings: HacConnectionSettingsState, notify: Boolean) {
        val developerSettings = HacExecDeveloperSettings.getInstance(project)
        val projectSettings = HacExecProjectSettings.getInstance(project)
        developerSettings.connections = developerSettings.connections
            .filterNot { it.uuid == settings.uuid }
        projectSettings.connections = projectSettings.connections
            .filterNot { it.uuid == settings.uuid }

        onDelete(settings, notify)
    }

    override fun save(settings: Map<HacConnectionSettingsState, Credentials>) {
        val groupedSettings = settings.keys.groupBy { it.scope }
        val projectSettings = HacExecProjectSettings.getInstance(project)
        val developerSettings = HacExecDeveloperSettings.getInstance(project)

        // remove persisted credentials for previous connections
        projectSettings.connections.forEach { removeCredentials(it) }
        developerSettings.connections.forEach { removeCredentials(it) }

        projectSettings.connections = groupedSettings.getOrElse(ExecConnectionScope.PROJECT) { emptyList() }
        developerSettings.connections = groupedSettings.getOrElse(ExecConnectionScope.PROJECT_PERSONAL) { emptyList() }

        onSave(settings)
    }

    override fun default() = HacConnectionSettingsState(
        port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_TOMCAT_SSL_PORT, "9002"),
        webroot = getPropertyOrDefault(project, HybrisConstants.PROPERTY_HAC_WEBROOT, ""),
    )

    override fun defaultCredentials(settings: HacConnectionSettingsState) = Credentials(
        "admin",
        getPropertyOrDefault(project, HybrisConstants.PROPERTY_ADMIN_INITIAL_PASSWORD, "nimda")
    )

    private fun persistedConnections() = buildList {
        addAll(HacExecDeveloperSettings.getInstance(project).connections)
        addAll(HacExecProjectSettings.getInstance(project).connections)
    }
        .takeIf { it.isNotEmpty() }

    private fun findActiveConnection() = HacExecDeveloperSettings.getInstance(project).activeConnectionUUID
        ?.let { uuid -> connections.find { it.uuid == uuid } }
        ?: connections.first()

    companion object {
        fun getInstance(project: Project): HacExecConnectionService = project.service()
    }

}