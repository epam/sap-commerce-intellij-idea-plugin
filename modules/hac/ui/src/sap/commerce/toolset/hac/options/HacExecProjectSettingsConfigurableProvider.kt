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

package sap.commerce.toolset.hac.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.ConnectionComboBoxModel
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.ui.HacConnectionSettingsListPanel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject

class HacExecProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "HAC", "sap.commerce.toolset.hac.exec.settings"
    ) {

        private lateinit var connectionsList: HacConnectionSettingsListPanel
        private lateinit var activeServerComboBox: ComboBox<HacConnectionSettingsState>
        private lateinit var activeServerModel: ConnectionComboBoxModel<HacConnectionSettingsState>

        private var originalConnections = HacExecConnectionService.getInstance(project).connections.map { it.mutable() }
        private var originalActiveConnection = HacExecConnectionService.getInstance(project).activeConnection

        override fun createPanel(): DialogPanel {
            connectionsList = HacConnectionSettingsListPanel(
                project, disposable,
                activeConnection = { activeServerComboBox.selectedItem as? HacConnectionSettingsState }
            ) { refreshActiveServerComboBox() }

            activeServerModel = ConnectionComboBoxModel() { connectionsList.repaint() }

            return panel {
                row {
                    icon(HybrisIcons.Y.REMOTE_GREEN)
                    activeServerComboBox = comboBox(
                        activeServerModel,
                        renderer = textListCellRenderer(" -- auto-create -- ") { it.presentationName }
                    )
                        .label(i18n("hybris.settings.project.remote_instances.hac.active.title"))
                        .onIsModified { originalActiveConnection.uuid != activeServerComboBox.selectedItem?.asSafely<HacConnectionSettingsState>()?.uuid }
                        .align(AlignX.FILL)
                        .component
                }.layout(RowLayout.PARENT_GRID)

                row {
                    cell(connectionsList)
                        .onIsModified { connectionsList.modified }
                        .align(Align.FILL)
                }
            }
        }

        override fun reset() {
            connectionsList.reset(originalConnections.map { it.copy() })
            refreshActiveServerComboBox()
            activeServerComboBox.selectedItem = originalActiveConnection
        }

        override fun apply() {
            super.apply()

            val connectionService = HacExecConnectionService.getInstance(project)
            val snapshots = connectionsList.elements.map { it.snapshot() }

            connectionService.save(snapshots)

            if (snapshots.isEmpty()) {
                originalConnections = connectionService.connections.map { it.mutable() }
                originalActiveConnection = connectionService.activeConnection
            } else {
                originalConnections = snapshots.map { it.state.mutable() }
                originalActiveConnection = activeServerComboBox.selectedItem as HacConnectionSettingsState

                connectionService.activeConnection = originalActiveConnection
            }

            reset()
        }

        private fun refreshActiveServerComboBox() {
            val previousSelectedItem = activeServerModel.selectedItem?.asSafely<HacConnectionSettingsState>()?.uuid
            val modifiedConnections = connectionsList.elements.map { it.snapshot() }
            activeServerModel.refresh(modifiedConnections.map { it.state })
            activeServerModel.selectedItem = modifiedConnections.find { it.state.uuid == previousSelectedItem }
                ?.state
                ?: modifiedConnections.firstOrNull()?.state
            activeServerComboBox.repaint()
        }
    }
}
