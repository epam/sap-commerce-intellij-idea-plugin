/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.kotlin.equalsIgnoreOrder
import com.intellij.idea.plugin.hybris.ui.RemoteHacInstancesListPanel
import com.intellij.idea.plugin.hybris.ui.RemoteSolrInstancesListPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.framework.base.deepClonePolymorphic
import javax.swing.DefaultComboBoxModel

class HybrisProjectRemoteInstancesSettingsConfigurableProvider(val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable(): Configurable {
        val settingsConfigurable = SettingsConfigurable(project)

        project.messageBus
            .connect(this)
            .subscribe<HybrisDeveloperSpecificProjectSettingsListener>(HybrisDeveloperSpecificProjectSettingsListener.TOPIC,
                object : HybrisDeveloperSpecificProjectSettingsListener {
                    override fun hacConnectionSettingsChanged() = settingsConfigurable.updateHacModel()
                    override fun solrConnectionSettingsChanged() = settingsConfigurable.updateSolrModel()
                }
            )
        return settingsConfigurable
    }

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.remote_instances.title"), "hybris.project.remote_instances.settings"
    ) {

        private val settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        private val activeHacServerModel = DefaultComboBoxModel<HybrisRemoteConnectionSettings>()
        private val activeSolrServerModel = DefaultComboBoxModel<HybrisRemoteConnectionSettings>()
        private val hacInstances = RemoteHacInstancesListPanel(project)
        private val solrInstances = RemoteSolrInstancesListPanel(project)

        private var initialHacInstances = settings.hacRemoteConnectionSettings.deepClonePolymorphic()
        private var initialSolrInstances = settings.solrRemoteConnectionSettings.deepClonePolymorphic()

        override fun createPanel() = panel {
            row {
                icon(HybrisIcons.Y_REMOTE_GREEN)
                comboBox(
                    activeHacServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.toString() }
                )
                    .label(message("hybris.settings.project.remote_instances.hac.active.title"))
                    .onReset { updateHacModel() }
                    .onApply { settings.setActiveHacRemoteConnectionSettings(activeHacServerModel.selectedItem as HybrisRemoteConnectionSettings) }
                    .onIsModified { activeHacServerModel.selectedItem != settings.getActiveHacRemoteConnectionSettings(project) }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            row {
                icon(HybrisIcons.CONSOLE_SOLR)
                comboBox(
                    activeSolrServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.toString() }
                )
                    .label(message("hybris.settings.project.remote_instances.solr.active.title"))
                    .onReset { updateSolrModel() }
                    .onApply { settings.setActiveSolrRemoteConnectionSettings(activeSolrServerModel.selectedItem as HybrisRemoteConnectionSettings) }
                    .onIsModified { activeSolrServerModel.selectedItem != settings.getActiveSolrRemoteConnectionSettings(project) }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            group(message("hybris.settings.project.remote_instances.hac.title"), false) {
                row {
                    cell(hacInstances)
                        .onReset { hacInstances.setInitialList(initialHacInstances) }
                        .onApply { initialHacInstances = hacInstances.data.deepClonePolymorphic() }
                        .onIsModified { hacInstances.data.equalsIgnoreOrder(initialHacInstances).not() }
                        .align(AlignX.FILL)
                }
            }

            group(message("hybris.settings.project.remote_instances.solr.title"), false) {
                row {
                    cell(solrInstances)
                        .onReset { solrInstances.setInitialList(initialSolrInstances) }
                        .onApply { initialSolrInstances = solrInstances.data.deepClonePolymorphic() }
                        .onIsModified { solrInstances.data.equalsIgnoreOrder(initialSolrInstances).not() }
                        .align(AlignX.FILL)
                }
            }
        }

        fun updateHacModel() {
            updateRemoteInstancesModel(
                activeHacServerModel,
                settings.getActiveHacRemoteConnectionSettings(project),
                settings.hacRemoteConnectionSettings,
            )
        }

        fun updateSolrModel() {
            updateRemoteInstancesModel(
                activeSolrServerModel,
                settings.getActiveSolrRemoteConnectionSettings(project),
                settings.solrRemoteConnectionSettings,
            )
        }

        private fun updateRemoteInstancesModel(
            model: DefaultComboBoxModel<HybrisRemoteConnectionSettings>,
            activeRemoteConnection: HybrisRemoteConnectionSettings,
            remoteConnectionSettingsList: List<HybrisRemoteConnectionSettings>
        ) {
            model.removeAllElements()
            remoteConnectionSettingsList.forEach { model.addElement(it) }
            model.selectedItem = activeRemoteConnection
        }
    }

    override fun dispose() {
    }
}
