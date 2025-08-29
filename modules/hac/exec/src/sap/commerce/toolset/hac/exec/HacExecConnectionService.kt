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
            ?: synchronized(lock) {
                findActiveConnection()
                    ?: default().also {
                        HacExecDeveloperSettings.getInstance(project).activeConnectionUUID = it.uuid
                        add(it, false)
                        onActivate(it)
                    }
            }
        set(value) {
            HacExecDeveloperSettings.getInstance(project).activeConnectionUUID = value.uuid

            onActivate(value)
        }

    override val connections: List<HacConnectionSettingsState>
        get() = persistedConnections()
            ?: listOf(default())

    override val listener: HacConnectionSettingsListener
        get() = project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC)

    override fun add(settings: HacConnectionSettingsState, notify: Boolean) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(HacExecDeveloperSettings.getInstance(project)) {
            connections = connections + settings

            onAdd(settings, notify)
        }

        ExecConnectionScope.PROJECT -> with(HacExecProjectSettings.getInstance(project)) {
            connections = connections + settings

            onAdd(settings, notify)
        }
    }

    override fun remove(settings: HacConnectionSettingsState, scope: ExecConnectionScope, notify: Boolean) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(HacExecDeveloperSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            onRemove(settings, notify)
        }

        ExecConnectionScope.PROJECT -> with(HacExecProjectSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            onRemove(settings, notify)
        }
    }

    override fun save(settings: Map<ExecConnectionScope, List<HacConnectionSettingsState>>, notify: Boolean) {
        HacExecProjectSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT) { emptyList() }
        HacExecDeveloperSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT_PERSONAL) { emptyList() }

        onSave(settings, notify)
    }

    override fun default() = HacConnectionSettingsState(
        port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_TOMCAT_SSL_PORT, "9002"),
        webroot = getPropertyOrDefault(project, HybrisConstants.PROPERTY_HAC_WEBROOT, ""),
        credentials = Credentials(
            "admin",
            getPropertyOrDefault(project, HybrisConstants.PROPERTY_ADMIN_INITIAL_PASSWORD, "nimda")
        )
    )

    private fun persistedConnections() = buildList {
        addAll(HacExecDeveloperSettings.getInstance(project).connections)
        addAll(HacExecProjectSettings.getInstance(project).connections)
    }
        .takeIf { it.isNotEmpty() }

    private fun findActiveConnection() = HacExecDeveloperSettings.getInstance(project).activeConnectionUUID
        ?.let { uuid -> persistedConnections()?.find { it.uuid == uuid } }

    companion object {
        fun getInstance(project: Project): HacExecConnectionService = project.service()
    }

}