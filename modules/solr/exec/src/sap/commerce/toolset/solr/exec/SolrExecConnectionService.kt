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

package sap.commerce.toolset.solr.exec

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exec.ExecConnectionService
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.solr.exec.settings.SolrExecDeveloperSettings
import sap.commerce.toolset.solr.exec.settings.SolrExecProjectSettings
import sap.commerce.toolset.solr.exec.settings.event.SolrConnectionSettingsListener
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState

@Service(Service.Level.PROJECT)
class SolrExecConnectionService(private val project: Project) : ExecConnectionService<SolrConnectionSettingsState>() {

    override var activeConnection: SolrConnectionSettingsState
        get() = SolrExecDeveloperSettings.getInstance(project).activeConnectionUUID
            ?.let { uuid -> connections.find { it.uuid == uuid } }
            ?: default().also {
                SolrExecDeveloperSettings.getInstance(project).activeConnectionUUID = it.uuid
                add(it)

                project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onActiveConnectionChanged(it)
            }
        set(value) {
            SolrExecDeveloperSettings.getInstance(project).activeConnectionUUID = value.uuid

            project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onActiveConnectionChanged(value)
        }

    override val connections: List<SolrConnectionSettingsState>
        get() = buildList {
            addAll(SolrExecDeveloperSettings.getInstance(project).connections)
            addAll(SolrExecProjectSettings.getInstance(project).connections)
        }
            .takeIf { it.isNotEmpty() }
            ?: listOf(default())

    override fun add(settings: SolrConnectionSettingsState) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(SolrExecDeveloperSettings.getInstance(project)) {
            connections = connections + settings

            project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onAdded(settings)
        }

        ExecConnectionScope.PROJECT -> with(SolrExecProjectSettings.getInstance(project)) {
            connections = connections + settings

            project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onAdded(settings)
        }
    }

    override fun remove(settings: SolrConnectionSettingsState, scope: ExecConnectionScope) = when (settings.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(SolrExecDeveloperSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onRemoved(settings)
        }

        ExecConnectionScope.PROJECT -> with(SolrExecProjectSettings.getInstance(project)) {
            connections = connections
                .filterNot { it.uuid == settings.uuid }

            project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onRemoved(settings)
        }
    }

    override fun save(settings: Map<ExecConnectionScope, List<SolrConnectionSettingsState>>) {
        SolrExecProjectSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT) { emptyList() }
        SolrExecDeveloperSettings.getInstance(project).connections = settings.getOrElse(ExecConnectionScope.PROJECT_PERSONAL) { emptyList() }

        project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC).onSave(settings)
    }

    override fun default(): SolrConnectionSettingsState {
        val connectionSettings = SolrConnectionSettingsState(
            port = getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_PORT, "8983"),
            credentials = Credentials(
                getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_USER, "solrserver"),
                getPropertyOrDefault(project, HybrisConstants.PROPERTY_SOLR_DEFAULT_PASSWORD, "server123")
            )
        )
        return connectionSettings
    }

    companion object {
        fun getInstance(project: Project): SolrExecConnectionService = project.service()
    }

}