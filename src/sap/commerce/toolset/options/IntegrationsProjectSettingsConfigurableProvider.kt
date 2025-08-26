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

package sap.commerce.toolset.options

import com.intellij.openapi.Disposable
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
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.ui.HacConnectionSettingsListPanel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import javax.swing.DefaultComboBoxModel

class IntegrationsProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "Integrations", "hybris.project.integrations.settings"
    ) {

        @Volatile
        private var isReset = false
        private val currentActiveHybrisConnection = HacExecConnectionService.getInstance(project).activeConnection

        private val activeHacServerModel = DefaultComboBoxModel<HacConnectionSettingsState>()
        private val hacInstances = HacConnectionSettingsListPanel(project) { _, connections ->
            if (!isReset) {
                HacExecConnectionService.getInstance(project).save(connections)

                updateModel(activeHacServerModel, activeHacServerModel.selectedItem as HacConnectionSettingsState?, connections)
            }
        }

        override fun createPanel() = panel {
            row {
                icon(HybrisIcons.Y.REMOTE_GREEN)
                comboBox(
                    activeHacServerModel,
                    renderer = SimpleListCellRenderer.create("?") { it.presentationName }
                )
                    .label(i18n("hybris.settings.project.remote_instances.hac.active.title"))
                    .onApply {
                        (activeHacServerModel.selectedItem as HacConnectionSettingsState?)
                            ?.let { settings -> HacExecConnectionService.getInstance(project).activeConnection = settings }
                    }
                    .onIsModified {
                        (activeHacServerModel.selectedItem as HacConnectionSettingsState?)
                            ?.let { it.uuid != HacExecConnectionService.getInstance(project).activeConnection.uuid }
                            ?: false
                    }
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            group(i18n("hybris.settings.project.remote_instances.hac.title"), false) {
                row {
                    cell(hacInstances)
                        .align(AlignX.FILL)
                }
            }
        }

        override fun reset() {
            isReset = true

            hacInstances.setData(HacExecConnectionService.getInstance(project).connections)

            updateModel(activeHacServerModel, currentActiveHybrisConnection, hacInstances.data)

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

    override fun dispose() = Unit
}
