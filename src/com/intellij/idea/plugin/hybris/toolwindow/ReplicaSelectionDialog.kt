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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.CCv2SubscriptionsComboBoxModelFactory
import com.intellij.idea.plugin.hybris.tools.remote.ReplicaType
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.Replica
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class ReplicaSelectionDialog(
    private val project: Project,
    parentComponent: Component
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE), Disposable {

    private val currentReplica: Replica? = HybrisHacHttpClient.getInstance(project).replica

    private val editable = AtomicBooleanProperty(true)
    private val autoReplicaSettings = AtomicBooleanProperty(currentReplica == null || currentReplica.type == ReplicaType.AUTO)
    private val manualReplicaSettings = AtomicBooleanProperty(currentReplica?.type == ReplicaType.MANUAL)
    private val ccv2ReplicaSettings = AtomicBooleanProperty(currentReplica?.type == ReplicaType.CCV2)

    private val ccv2EnvironmentEnabled = AtomicBooleanProperty(currentReplica?.environment != null)
    private val ccv2ServiceEnabled = AtomicBooleanProperty(currentReplica?.service != null)
    private val ccv2ReplicaEnabled = AtomicBooleanProperty(currentReplica?.replica != null)
    private val ccv2SettingsRefresh = AtomicBooleanProperty(true)

    private val replicaType = AtomicProperty(currentReplica?.type ?: ReplicaType.AUTO).apply {
        afterChange { selectedReplica ->
            autoReplicaSettings.set(selectedReplica == ReplicaType.AUTO)
            manualReplicaSettings.set(selectedReplica == ReplicaType.MANUAL)
            ccv2ReplicaSettings.set(selectedReplica == ReplicaType.CCV2)
        }
    }
    private val ccv2SubscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, currentReplica?.subscription)
    private val ccv2EnvironmentComboBoxModel = DefaultComboBoxModel<CCv2EnvironmentDto>()
    private val ccv2ServiceComboBoxModel = DefaultComboBoxModel<CCv2ServiceDto>()
    private val ccv2ReplicaComboBoxModel = DefaultComboBoxModel<CCv2ServiceReplicaDto>()

    init {
        title = "Replica Selection"
        isResizable = false

        currentReplica?.apply {
            environment?.let {
                ccv2EnvironmentComboBoxModel.addElement(it)
                ccv2EnvironmentComboBoxModel.selectedItem = it
            }
            service?.let {
                ccv2ServiceComboBoxModel.addElement(it)
                ccv2ServiceComboBoxModel.selectedItem = it
            }
            replica?.let {
                ccv2ReplicaComboBoxModel.addElement(it)
                ccv2ReplicaComboBoxModel.selectedItem = it
            }
        }

        super.init()
    }

    override fun dispose() {
        super.dispose()
    }

    private lateinit var manualCookieName: JBTextField
    private lateinit var manualReplicaId: JBTextField
    private lateinit var ccv2SubscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var ccv2EnvironmentComboBox: ComboBox<CCv2EnvironmentDto>
    private lateinit var ccv2ServiceComboBox: ComboBox<CCv2ServiceDto>
    private lateinit var ccv2ReplicaComboBox: ComboBox<CCv2ServiceReplicaDto>
    private lateinit var jbLoadingPanel: JBLoadingPanel
    private lateinit var centerPanel: DialogPanel

    private fun startLoading(text: String = "Loading...") {
        editable.set(false)
        ccv2SettingsRefresh.set(false)
        jbLoadingPanel.setLoadingText(text)
        jbLoadingPanel.startLoading()
    }

    private fun stopLoading() {
        editable.set(true)
        ccv2SettingsRefresh.set(true)
        jbLoadingPanel.stopLoading()
    }

    override fun createCenterPanel(): JComponent {
        centerPanel = panel {
            row {
                segmentedButton(ReplicaType.entries, {
                    this.text = it.shortTitle
                    this.icon = it.icon
                    this.toolTipText = it.title
                })
                    .align(AlignX.FILL)
                    .gap(RightGap.SMALL)
                    .whenItemSelected { replicaType.set(it) }
                    .apply {
                        selectedItem = (currentReplica?.type ?: ReplicaType.AUTO)
                        enabledIf(editable)
                    }
            }.layout(RowLayout.PARENT_GRID)

            autoSettings().visibleIf(autoReplicaSettings)
            manualSettings().visibleIf(manualReplicaSettings)
            ccv2Settings().visibleIf(ccv2ReplicaSettings)
        }
            .apply {
                border = JBUI.Borders.empty(16)
                preferredSize = Dimension(400, 300)
            }

        return JBLoadingPanel(BorderLayout(), this).apply {
            add(centerPanel, BorderLayout.CENTER)
            jbLoadingPanel = this
        }
    }

    override fun applyFields() {
        super.applyFields()

        val replica = when (replicaType.get()) {
            ReplicaType.AUTO -> null

            ReplicaType.MANUAL -> manualReplicaId.text
                .takeIf { it.isNotBlank() }
                ?.let {
                    Replica(
                        type = ReplicaType.MANUAL,
                        id = it,
                        cookieName = manualCookieName.text
                    )
                }

            ReplicaType.CCV2 -> ccv2ReplicaComboBox.selectedItem?.asSafely<CCv2ServiceReplicaDto>()
                ?.let {
                    Replica(
                        type = ReplicaType.CCV2,
                        id = if (it.name.startsWith(".")) it.name else ".${it.name}",
                        subscription = ccv2SubscriptionComboBox.selectedItem as? CCv2Subscription,
                        environment = ccv2EnvironmentComboBox.selectedItem as? CCv2EnvironmentDto,
                        service = ccv2ServiceComboBox.selectedItem as? CCv2ServiceDto,
                        replica = it
                    )
                }
        }

        HybrisHacHttpClient.getInstance(project).setReplica(replica)
    }

    private fun Panel.autoSettings() = panel {
        row {
            cell(
                InlineBanner(
                    """
                        In the auto-discovery mode, the Plugin will automatically pick-up all related cookies during login and rely on load balancer replica selection.
                """.trimIndent(),
                    EditorNotificationPanel.Status.Info
                ).showCloseButton(false)
            )
        }.topGap(TopGap.MEDIUM)
    }

    private fun Panel.manualSettings() = group("Manual Settings") {
        row {
            manualReplicaId = textField()
                .label("Replica id:")
                .text(currentReplica?.id ?: "")
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)

        row {
            manualCookieName = textField()
                .label("Cookie name:")
                .text(currentReplica?.cookieName ?: "")
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)
    }

    private fun Panel.ccv2Settings() = group("CCv2 Settings") {
        row {
            ccv2SubscriptionComboBox = comboBox(
                ccv2SubscriptionsComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = HybrisIcons.Module.CCV2
                        label.text = value.toString()
                    }
                }
            )
                .label("Subscription:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .enabledIf(editable)
                .onChanged {
                    val subscription = it.selectedItem as CCv2Subscription
                    CCv2Service.getInstance(project).fetchEnvironments(
                        listOf(subscription),
                        onStartCallback = {
                            ccv2EnvironmentComboBoxModel.removeAllElements()
                            ccv2ServiceComboBoxModel.removeAllElements()
                            ccv2ReplicaComboBoxModel.removeAllElements()

                            ccv2EnvironmentEnabled.set(false)
                            ccv2ServiceEnabled.set(false)
                            ccv2ReplicaEnabled.set(false)

                            startLoading("Fetching environments...")
                        },
                        onCompleteCallback = { response ->
                            response[subscription]
                                ?.filter { environment -> environment.accessible  }
                                ?.let { environments ->
                                    ccv2EnvironmentComboBoxModel.addAll(environments)

                                    ccv2EnvironmentEnabled.set(true)

                                    stopLoading()
                                }
                        },
                        sendEvents = false
                    )
                }
                .component

            actionButton(object : AnAction("Refresh", "", HybrisIcons.Actions.REFRESH) {
                override fun getActionUpdateThread() = ActionUpdateThread.BGT
                override fun actionPerformed(e: AnActionEvent) {
                    ccv2EnvironmentEnabled.set(false)
                    ccv2ServiceEnabled.set(false)
                    ccv2ReplicaEnabled.set(false)

                    ccv2SubscriptionsComboBoxModel.refresh()
                    ccv2EnvironmentComboBoxModel.removeAllElements()
                    ccv2ServiceComboBoxModel.removeAllElements()
                    ccv2ReplicaComboBoxModel.removeAllElements()
                }
            })
                .align(AlignX.RIGHT)
                .enabledIf(ccv2SettingsRefresh)
        }
            .layout(RowLayout.PARENT_GRID)

        row {
            ccv2EnvironmentComboBox = comboBox(
                ccv2EnvironmentComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    label.text = value?.name
                    label.icon = value?.type?.icon
                }
            )
                .label("Environment:")
                .align(AlignX.FILL)
                .onChanged {
                    val subscription = ccv2SubscriptionComboBox.selectedItem as CCv2Subscription
                    val environment = it.selectedItem as CCv2EnvironmentDto
                    CCv2Service.getInstance(project).fetchEnvironmentServices(
                        subscription,
                        environment,
                        onStartCallback = {
                            ccv2ServiceComboBoxModel.removeAllElements()
                            ccv2ReplicaComboBoxModel.removeAllElements()

                            ccv2EnvironmentEnabled.set(false)
                            ccv2ServiceEnabled.set(false)
                            ccv2ReplicaEnabled.set(false)

                            startLoading("Fetching services & replicas...")
                        },
                        onCompleteCallback = { response ->
                            ccv2ServiceComboBoxModel.removeAllElements()
                            response?.let { services ->
                                ccv2ServiceComboBoxModel.addAll(services)

                                ccv2EnvironmentEnabled.set(true)
                                ccv2ServiceEnabled.set(true)
                                ccv2ReplicaEnabled.set(true)

                                stopLoading()
                            }
                        }
                    )
                }
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2EnvironmentEnabled)

        row {
            ccv2ServiceComboBox = comboBox(
                ccv2ServiceComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.text = value.name
                        label.icon = HybrisIcons.CCv2.Service.ICON
                    }
                }
            )
                .label("Service:")
                .align(AlignX.FILL)
                .onChanged {
                    val service = it.selectedItem as CCv2ServiceDto
                    ccv2ReplicaComboBoxModel.removeAllElements()
                    ccv2ReplicaComboBoxModel.addAll(service.replicas)
                }
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2ServiceEnabled)

        row {
            ccv2ReplicaComboBox = comboBox(
                ccv2ReplicaComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.name }
            )
                .label("Replica:")
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2ReplicaEnabled)
    }
}
