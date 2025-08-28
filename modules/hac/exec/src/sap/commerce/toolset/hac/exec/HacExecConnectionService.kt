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
class HacExecConnectionService(private val project: Project) : ExecConnectionService<HacConnectionSettingsState>() {

    override var activeConnection: HacConnectionSettingsState
        get() = HacExecDeveloperSettings.getInstance(project).activeConnectionUUID
            ?.let { uuid -> connections.find { it.uuid == uuid } }
            ?: default().also {
                HacExecDeveloperSettings.getInstance(project).activeConnectionUUID = it.uuid
                add(it)

                project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onRemoved(it)
            }
        set(value) {
            HacExecDeveloperSettings.getInstance(project).activeConnectionUUID = value.uuid

            project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onRemoved(value)
        }

    override val connections: List<HacConnectionSettingsState>
        get() = buildList {
            addAll(HacExecDeveloperSettings.getInstance(project).connections)
            addAll(HacExecProjectSettings.getInstance(project).connections)
        }
            .takeIf { it.isNotEmpty() }
            ?: listOf(default())

    override fun add(settings: HacConnectionSettingsState) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(HacExecDeveloperSettings.getInstance(project)) {
            connections = connections + settings

            project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onAdded(settings)
        }

        ExecConnectionScope.PROJECT -> with(HacExecProjectSettings.getInstance(project)) {
            connections = connections + settings

            project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onAdded(settings)
        }
    }

    override fun remove(settings: HacConnectionSettingsState, scope: ExecConnectionScope) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(HacExecDeveloperSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onRemoved(settings)
        }

        ExecConnectionScope.PROJECT -> with(HacExecProjectSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onRemoved(settings)
        }
    }

    override fun save(settings: Map<ExecConnectionScope, List<HacConnectionSettingsState>>) {
        HacExecProjectSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT) { emptyList() }
        HacExecDeveloperSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT_PERSONAL) { emptyList() }

        project.messageBus.syncPublisher(HacConnectionSettingsListener.TOPIC).onSave(settings)
    }

    override fun default(): HacConnectionSettingsState {
        val connectionSettings = HacConnectionSettingsState(
            port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_TOMCAT_SSL_PORT, "9002"),
            webroot = getPropertyOrDefault(project, HybrisConstants.PROPERTY_HAC_WEBROOT, ""),
            credentials = Credentials(
                "admin",
                getPropertyOrDefault(project, HybrisConstants.PROPERTY_ADMIN_INITIAL_PASSWORD, "nimda")
            )
        )
        return connectionSettings
    }

    companion object {
        fun getInstance(project: Project): HacExecConnectionService = project.service()
    }

}