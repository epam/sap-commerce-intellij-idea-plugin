/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.ui.RemoteHacInstancesListPanel
import com.intellij.idea.plugin.hybris.ui.RemoteSolrInstancesListPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel

class HybrisProjectRemoteInstancesSettingsConfigurableProvider(val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.remote_instances.title"), "hybris.project.remote_instances.settings"
    ) {

        @Volatile
        private var isReset = false
        private val currentActiveHybrisConnection = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        private val currentActiveSolrConnection = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.SOLR)

        private val activeHacServerModel = DefaultComboBoxModel<HybrisRemoteConnectionSettings>()
        private val activeSolrServerModel = DefaultComboBoxModel<HybrisRemoteConnectionSettings>()
        private val hacInstances = RemoteHacInstancesListPanel(project) { _, data ->
            if (!isReset) updateModel(activeHacServerModel, activeHacServerModel.selectedItem as HybrisRemoteConnectionSettings?, data)
        }

        private val solrInstances = RemoteSolrInstancesListPanel(project) { _, data ->
            if (!isReset) updateModel(activeSolrServerModel, activeSolrServerModel.selectedItem as HybrisRemoteConnectionSettings?, data)
        }

        override fun createPanel() = panel {
            row {
                icon(HybrisIcons.Y_REMOTE_GREEN)
                comboBox(
                    activeHacServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.toString() }
                )
                    .label(message("hybris.settings.project.remote_instances.hac.active.title"))
                    .onApply {
                        (activeHacServerModel.selectedItem as HybrisRemoteConnectionSettings?)
                            ?.let { settings -> RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, settings) }
                    }
                    .onIsModified {
                        (activeHacServerModel.selectedItem as HybrisRemoteConnectionSettings?)
                            ?.let { it.uuid != RemoteConnectionUtil.getActiveRemoteConnectionId(project, it.type) }
                            ?: false
                    }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            row {
                icon(HybrisIcons.CONSOLE_SOLR)
                comboBox(
                    activeSolrServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.toString() }
                )
                    .label(message("hybris.settings.project.remote_instances.solr.active.title"))
                    .onApply {
                        (activeSolrServerModel.selectedItem as HybrisRemoteConnectionSettings?)
                            ?.let { settings -> RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, settings) }
                    }
                    .onIsModified {
                        (activeSolrServerModel.selectedItem as HybrisRemoteConnectionSettings?)
                            ?.let { it.uuid != RemoteConnectionUtil.getActiveRemoteConnectionId(project, it.type) }
                            ?: false
                    }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            group(message("hybris.settings.project.remote_instances.hac.title"), false) {
                row {
                    cell(hacInstances)
                        .align(AlignX.FILL)
                }
            }

            group(message("hybris.settings.project.remote_instances.solr.title"), false) {
                row {
                    cell(solrInstances)
                        .align(AlignX.FILL)
                }
            }
        }

        override fun reset() {
            isReset = true

            hacInstances.setData(RemoteConnectionUtil.getRemoteConnections(project, RemoteConnectionType.Hybris))
            solrInstances.setData(RemoteConnectionUtil.getRemoteConnections(project, RemoteConnectionType.SOLR))

            updateModel(activeHacServerModel, currentActiveHybrisConnection, hacInstances.data)
            updateModel(activeSolrServerModel, currentActiveSolrConnection, solrInstances.data)

            isReset = false
        }

        private fun updateModel(
            model: DefaultComboBoxModel<HybrisRemoteConnectionSettings>,
            activeConnection: HybrisRemoteConnectionSettings?,
            connectionSettings: Collection<HybrisRemoteConnectionSettings>
        ) {
            model.removeAllElements()
            model.addAll(connectionSettings)

            model.selectedItem = if (model.getIndexOf(activeConnection) != -1) model.getElementAt(model.getIndexOf(activeConnection))
            else model.getElementAt(0)
        }
    }

    override fun dispose() {
    }
}
