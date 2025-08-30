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

package sap.commerce.toolset.solr.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.ConnectionComboBoxModel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import sap.commerce.toolset.solr.ui.SolrConnectionSettingsListPanel

class SolrExecProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "Solr", "sap.commerce.toolset.solr.exec.settings"
    ) {

        private lateinit var connectionsListPanel: SolrConnectionSettingsListPanel
        private lateinit var activeServerComboBox: ComboBox<SolrConnectionSettingsState>
        private lateinit var activeServerModel: ConnectionComboBoxModel<SolrConnectionSettingsState>

        private var originalConnections = SolrExecConnectionService.getInstance(project).connections.map { it.mutable() }
        private var originalActiveConnection = SolrExecConnectionService.getInstance(project).activeConnection

        override fun createPanel(): DialogPanel {
            activeServerModel = ConnectionComboBoxModel()
            connectionsListPanel = SolrConnectionSettingsListPanel(project, disposable) {
                val previousSelectedItem = activeServerModel.selectedItem?.asSafely<SolrConnectionSettingsState>()?.uuid
                val modifiedConnections = connectionsListPanel.data.map { it.immutable() }
                activeServerModel.refresh(modifiedConnections.map { it.first })
                activeServerModel.selectedItem = modifiedConnections.find { it.first.uuid == previousSelectedItem }
                    ?.first
                    ?: modifiedConnections.firstOrNull()?.first
                activeServerComboBox.repaint()
            }

            return panel {
                row {
                    icon(HybrisIcons.Console.SOLR)
                    activeServerComboBox = comboBox(
                        activeServerModel,
                        renderer = SimpleListCellRenderer.create(" -- auto-create -- ") { it.presentationName }
                    )
                        .label(i18n("hybris.settings.project.remote_instances.solr.active.title"))
                        .onIsModified { originalActiveConnection.uuid != activeServerComboBox.selectedItem?.asSafely<SolrConnectionSettingsState>()?.uuid }
                        .align(AlignX.FILL)
                        .component
                }.layout(RowLayout.PARENT_GRID)

                group(i18n("hybris.settings.project.remote_instances.solr.title"), false) {
                    row {
                        cell(connectionsListPanel)
                            .onIsModified { connectionsListPanel.data != originalConnections }
                            .align(Align.FILL)
                    }
                }
            }
        }

        override fun reset() {
            connectionsListPanel.data = originalConnections.map { it.copy() }
            activeServerComboBox.selectedItem = originalActiveConnection
        }

        override fun apply() {
            super.apply()

            val connectionService = SolrExecConnectionService.getInstance(project)
            val newSettings = connectionsListPanel.data.map { it.immutable() }

            connectionService.save(newSettings.associate { it.first to it.second })

            if (newSettings.isEmpty()) {
                originalConnections = connectionService.connections.map { it.mutable() }
                originalActiveConnection = connectionService.activeConnection
            } else {
                originalConnections = newSettings.map { it.first.mutable() }
                originalActiveConnection = activeServerComboBox.selectedItem as SolrConnectionSettingsState

                connectionService.activeConnection = originalActiveConnection
            }

            reset()
        }

    }
}
