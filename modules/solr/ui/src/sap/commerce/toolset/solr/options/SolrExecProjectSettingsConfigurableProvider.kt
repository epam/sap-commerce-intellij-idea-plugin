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
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.presentationName
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import sap.commerce.toolset.solr.ui.SolrConnectionSettingsListPanel
import javax.swing.DefaultComboBoxModel

class SolrExecProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "Solr", "sap.commerce.toolset.solr.exec.settings"
    ) {

        @Volatile
        private var isReset = false
        private val currentActiveConnection = SolrExecConnectionService.getInstance(project).activeConnection

        private val activeServerModel = DefaultComboBoxModel<SolrConnectionSettingsState>()

        private val servers = SolrConnectionSettingsListPanel(project) { _, connections ->
            if (!isReset) {
                SolrExecConnectionService.getInstance(project).save(connections)

                updateModel(activeServerModel, activeServerModel.selectedItem as SolrConnectionSettingsState?, connections)
            }
        }

        override fun createPanel() = panel {
            row {
                icon(HybrisIcons.Console.SOLR)
                comboBox(
                    activeServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.presentationName }
                )
                    .label(i18n("hybris.settings.project.remote_instances.solr.active.title"))
                    .onApply {
                        (activeServerModel.selectedItem as SolrConnectionSettingsState?)
                            ?.let { settings -> SolrExecConnectionService.getInstance(project).activeConnection = settings }
                    }
                    .onIsModified {
                        (activeServerModel.selectedItem as SolrConnectionSettingsState?)
                            ?.let { it.uuid != SolrExecConnectionService.getInstance(project).activeConnection.uuid }
                            ?: false
                    }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            group(i18n("hybris.settings.project.remote_instances.solr.title"), false) {
                row {
                    cell(servers)
                        .align(AlignX.FILL)
                }
            }
        }

        override fun reset() {
            isReset = true

            servers.setData(SolrExecConnectionService.getInstance(project).connections)

            updateModel(activeServerModel, currentActiveConnection, servers.data)

            isReset = false
        }

        private fun <T : ExecConnectionSettingsState> updateModel(
            model: DefaultComboBoxModel<T>,
            activeConnection: T?,
            connectionSettings: Collection<T>
        ) {
            model.removeAllElements()
            model.addAll(connectionSettings)

            model.selectedItem = if (model.getIndexOf(activeConnection) != -1) model.getElementAt(model.getIndexOf(activeConnection))
            else model.getElementAt(0)
        }
    }
}
