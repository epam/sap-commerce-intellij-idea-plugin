/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import sap.commerce.toolset.exec.ExecConnectionService
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.settings.state.Mutation
import sap.commerce.toolset.solr.SolrConstants
import sap.commerce.toolset.solr.exec.settings.SolrExecDeveloperSettings
import sap.commerce.toolset.solr.exec.settings.SolrExecProjectSettings
import sap.commerce.toolset.solr.exec.settings.event.SolrConnectionSettingsListener
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState

@Service(Service.Level.PROJECT)
class SolrExecConnectionService(project: Project) : ExecConnectionService<SolrConnectionSettingsState, SolrConnectionSettingsState.Snapshot>(project) {

    private val lock = Any()

    override var activeConnection: SolrConnectionSettingsState
        get() = findActiveConnection()
        set(value) {
            SolrExecDeveloperSettings.getInstance(project).activeConnectionUUID = value.uuid

            onActivate(value)
        }

    override val connections: List<SolrConnectionSettingsState>
        get() = persistedConnections()
            ?: synchronized(lock) {
                persistedConnections()
                    ?: listOf(default()).also {
                        val defaultSettings = it.first()

                        SolrExecDeveloperSettings.getInstance(project).connections = it
                        activeConnection = defaultSettings
                    }
            }

    override val listener: SolrConnectionSettingsListener
        get() = project.messageBus.syncPublisher(SolrConnectionSettingsListener.TOPIC)

    override fun create(snapshot: SolrConnectionSettingsState.Snapshot, notify: Boolean) = when (snapshot.state.scope) {
        ExecConnectionScope.PROJECT_PERSONAL -> with(SolrExecDeveloperSettings.getInstance(project)) {
            connections = connections + snapshot.state

            onCreate(snapshot, notify)
        }

        ExecConnectionScope.PROJECT -> with(SolrExecProjectSettings.getInstance(project)) {
            connections = connections + snapshot.state

            onCreate(snapshot, notify)
        }
    }

    override fun delete(state: SolrConnectionSettingsState) {
        val developerSettings = SolrExecDeveloperSettings.getInstance(project)
        val projectSettings = SolrExecProjectSettings.getInstance(project)
        developerSettings.connections = developerSettings.connections
            .filterNot { it.uuid == state.uuid }
        projectSettings.connections = projectSettings.connections
            .filterNot { it.uuid == state.uuid }
    }

    override fun default() = SolrConnectionSettingsState(
        port = getPropertyOrDefault(project, SolrConstants.PROPERTY_SOLR_DEFAULT_PORT, "8983"),
    )

    override fun save(snapshots: Collection<SolrConnectionSettingsState.Snapshot>) {
        val groupedSettings = snapshots
            .filter { it.mutation == Mutation.SAVE }
            .map { it.state }
            .groupBy { it.scope }
            .mapValues { (_, v) -> v.toList() }
        val projectSettings = SolrExecProjectSettings.getInstance(project)
        val developerSettings = SolrExecDeveloperSettings.getInstance(project)

        // remove persisted credentials only f or the connections which are gone
        val statesToSave = snapshots.map { it.state.uuid }

        (projectSettings.connections + developerSettings.connections)
            .filterNot { statesToSave.contains(it.uuid) }
            .forEach { removeCredentials(it) }

        projectSettings.connections = groupedSettings.getOrElse(ExecConnectionScope.PROJECT) { emptyList() }
        developerSettings.connections = groupedSettings.getOrElse(ExecConnectionScope.PROJECT_PERSONAL) { emptyList() }

        onSave(snapshots)
    }

    override fun defaultCredentials() = Credentials(
        getPropertyOrDefault(project, SolrConstants.PROPERTY_SOLR_DEFAULT_USER, "solrserver"),
        getPropertyOrDefault(project, SolrConstants.PROPERTY_SOLR_DEFAULT_PASSWORD, "server123")
    )

    private fun persistedConnections() = buildList {
        addAll(SolrExecDeveloperSettings.getInstance(project).connections)
        addAll(SolrExecProjectSettings.getInstance(project).connections)
    }
        .takeIf { it.isNotEmpty() }

    private fun findActiveConnection() = SolrExecDeveloperSettings.getInstance(project).activeConnectionUUID
        ?.let { uuid -> connections.find { it.uuid == uuid } }
        ?: connections.first()

    companion object {
        fun getInstance(project: Project): SolrExecConnectionService = project.service()
    }

}