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

package com.intellij.idea.plugin.hybris.settings.options

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
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

class ProjectRemoteInstancesSettingsConfigurableProvider(val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = ProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.remote_instances.title"), "hybris.project.remote_instances.settings"
    ) {

        @Volatile
        private var isReset = false
        private val currentActiveHybrisConnection = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
        private val currentActiveSolrConnection = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.SOLR)

        private val activeHacServerModel = DefaultComboBoxModel<RemoteConnectionSettings>()
        private val activeSolrServerModel = DefaultComboBoxModel<RemoteConnectionSettings>()
        private val hacInstances = RemoteHacInstancesListPanel(project) { _, data ->
            if (!isReset) updateModel(activeHacServerModel, activeHacServerModel.selectedItem as RemoteConnectionSettings?, data)
        }

        private val solrInstances = RemoteSolrInstancesListPanel(project) { _, data ->
            if (!isReset) updateModel(activeSolrServerModel, activeSolrServerModel.selectedItem as RemoteConnectionSettings?, data)
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
                        (activeHacServerModel.selectedItem as RemoteConnectionSettings?)
                            ?.let { settings -> RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, settings) }
                    }
                    .onIsModified {
                        (activeHacServerModel.selectedItem as RemoteConnectionSettings?)
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
                        (activeSolrServerModel.selectedItem as RemoteConnectionSettings?)
                            ?.let { settings -> RemoteConnectionUtil.setActiveRemoteConnectionSettings(project, settings) }
                    }
                    .onIsModified {
                        (activeSolrServerModel.selectedItem as RemoteConnectionSettings?)
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
            model: DefaultComboBoxModel<RemoteConnectionSettings>,
            activeConnection: RemoteConnectionSettings?,
            connectionSettings: Collection<RemoteConnectionSettings>
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
