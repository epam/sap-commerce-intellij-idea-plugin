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
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.event.CCv2SettingsListener
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.presentationName
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.ui.HacConnectionSettingsListPanel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import javax.swing.DefaultComboBoxModel

class ProjectIntegrationsSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider(), Disposable {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        "Integrations", "hybris.project.integrations.settings"
    ) {

        private val ccv2DeveloperSettings = CCv2DeveloperSettings.getInstance(project)

        @Volatile
        private var isReset = false
        private val currentActiveHybrisConnection = HacExecConnectionService.getInstance(project).activeConnection

        private lateinit var activeCCv2SubscriptionComboBox: ComboBox<CCv2Subscription>
        private val activeHacServerModel = DefaultComboBoxModel<HacConnectionSettingsState>()
        private val hacInstances = HacConnectionSettingsListPanel(project) { _, connections ->
            if (!isReset) {
                HacExecConnectionService.getInstance(project).save(connections)

                updateModel(activeHacServerModel, activeHacServerModel.selectedItem as HacConnectionSettingsState?, connections)
            }
        }

        private val ccv2SubscriptionsModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true)

        override fun createPanel() = panel {
            group("CCv2 Integration", true) {
                row {
                    icon(HybrisIcons.Module.CCV2)
                    activeCCv2SubscriptionComboBox = comboBox(
                        ccv2SubscriptionsModel,
                        renderer = SimpleListCellRenderer.create { label, value, _ ->
                            if (value != null) {
                                label.icon = HybrisIcons.Module.CCV2
                                label.text = value.toString()
                            } else {
                                label.text = "-- all subscriptions --"
                            }
                        }
                    )
                        .label("Subscription:")
                        .comment("Subscriptions are IntelliJ IDEA application-aware and can be changes via corresponding settings: [y] SAP CX > CCv2.")
                        .onApply {
                            val activeSubscription = activeCCv2SubscriptionComboBox.selectedItem as? CCv2Subscription
                            when (activeSubscription) {
                                is CCv2Subscription -> ccv2DeveloperSettings.activeCCv2SubscriptionID = activeSubscription.uuid
                                else -> ccv2DeveloperSettings.activeCCv2SubscriptionID = null
                            }

                            project.messageBus
                                .syncPublisher(CCv2SettingsListener.TOPIC)
                                .onActiveSubscriptionChanged(activeSubscription)
                        }
                        .onIsModified { activeCCv2SubscriptionComboBox.selectedItem != ccv2DeveloperSettings.getActiveCCv2Subscription() }
                        .component
                        .also { it.selectedItem = ccv2DeveloperSettings.getActiveCCv2Subscription() }
                }.layout(RowLayout.PARENT_GRID)
            }

            group("Remote Instances", true) {
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
        }

        override fun reset() {
            isReset = true

            activeCCv2SubscriptionComboBox.selectedItem = ccv2DeveloperSettings.getActiveCCv2Subscription()

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
