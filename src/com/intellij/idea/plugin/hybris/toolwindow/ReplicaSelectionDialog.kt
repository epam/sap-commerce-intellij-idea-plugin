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
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.Replica
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
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

    private val currentReplica: Replica? = project.getUserData(AbstractHybrisHacHttpClient.REPLICA_KEY)

    private val autoReplicaSettings = AtomicBooleanProperty(currentReplica == null || currentReplica.type == ReplicaType.AUTO)
    private val manualReplicaSettings = AtomicBooleanProperty(currentReplica?.type == ReplicaType.MANUAL)
    private val ccv2ReplicaSettings = AtomicBooleanProperty(currentReplica?.type == ReplicaType.CCV2)

    private val ccv2EnvironmentEnabled = AtomicBooleanProperty(currentReplica?.type == ReplicaType.CCV2)
    private val ccv2ServiceEnabled = AtomicBooleanProperty(currentReplica?.type == ReplicaType.CCV2)
    private val ccv2ReplicaEnabled = AtomicBooleanProperty(currentReplica?.type == ReplicaType.CCV2)

    private val replicaType = AtomicProperty<ReplicaType?>(ReplicaType.AUTO).also {
        it.afterChange { selectedReplica ->
            autoReplicaSettings.set(selectedReplica == ReplicaType.AUTO)
            manualReplicaSettings.set(selectedReplica == ReplicaType.MANUAL)
            ccv2ReplicaSettings.set(selectedReplica == ReplicaType.CCV2)
        }
    }
    private val ccv2EnvironmentComboBoxModel = DefaultComboBoxModel<CCv2EnvironmentDto>()
    private val ccv2ServiceComboBoxModel = DefaultComboBoxModel<CCv2ServiceDto>()
    private val ccv2ReplicaComboBoxModel = DefaultComboBoxModel<CCv2ServiceReplicaDto>()

    init {
        title = "Replica Selection"
        isResizable = false
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

    private var isLoading: Boolean
        get() = jbLoadingPanel.isLoading
        set(value) =
            if (value) {
                centerPanel.isVisible = false
                jbLoadingPanel.startLoading()
            } else {
                jbLoadingPanel.stopLoading()
                centerPanel.isVisible = true
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
                    .also { it.selectedItem = ReplicaType.AUTO }
            }.layout(RowLayout.PARENT_GRID)

            autoSettings().visibleIf(autoReplicaSettings)
            manualSettings().visibleIf(manualReplicaSettings)
            ccv2Settings().visibleIf(ccv2ReplicaSettings)
        }
            .also {
                it.border = JBUI.Borders.empty(16)
                it.preferredSize = Dimension(400, 300)
            }


        jbLoadingPanel = JBLoadingPanel(BorderLayout(), this).also {
            it.add(centerPanel, BorderLayout.CENTER)
        }

        return jbLoadingPanel
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

            else -> null
        }

        project.putUserData(HybrisHacHttpClient.REPLICA_KEY, replica)
    }

    private fun Panel.autoSettings() = panel {
        row {
            text("In the auto-discovery mode, the Plugin will automatically pick-up all related cookies during login and rely on load balancer replica selection.")
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
                CCv2SubscriptionsComboBoxModelFactory.create(project),
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

                            isLoading = true
                        },
                        onCompleteCallback = { response ->
                            response[subscription]?.let { environments ->
                                ccv2EnvironmentComboBoxModel.addAll(environments)

                                ccv2EnvironmentEnabled.set(true)

                                isLoading = false
                            }
                        }
                    )
                }
                .component
                .also { it.selectedItem = currentReplica?.subscription }
        }
            .layout(RowLayout.PARENT_GRID)

        row {
            ccv2EnvironmentComboBox = comboBox(
                ccv2EnvironmentComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.name }
            )
                .label("Environment:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .onChanged {
                    val subscription = ccv2SubscriptionComboBox.selectedItem as CCv2Subscription
                    val environment = it.selectedItem as CCv2EnvironmentDto
                    CCv2Service.getInstance(project).fetchEnvironmentServices(
                        subscription,
                        environment,
                        onStartCallback = {
                            ccv2ServiceComboBoxModel.removeAllElements()
                            ccv2ReplicaComboBoxModel.removeAllElements()

                            ccv2ServiceEnabled.set(false)
                            ccv2ReplicaEnabled.set(false)

                            isLoading = true
                        },
                        onCompleteCallback = { response ->
                            ccv2ServiceComboBoxModel.removeAllElements()
                            response?.let { services ->
                                ccv2ServiceComboBoxModel.addAll(services)

                                ccv2ServiceEnabled.set(true)
                                ccv2ReplicaEnabled.set(true)

                                isLoading = false
                            }

                        }
                    )
                }
                .component
                .also { it.selectedItem = currentReplica?.environment }
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2EnvironmentEnabled)

        row {
            ccv2ServiceComboBox = comboBox(
                ccv2ServiceComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.name }
            )
                .label("Service:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .onChanged {
                    val service = it.selectedItem as CCv2ServiceDto
                    ccv2ReplicaComboBoxModel.removeAllElements()
                    ccv2ReplicaComboBoxModel.addAll(service.replicas)
                }
                .component
                .also { it.selectedItem = currentReplica?.service }
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
                .gap(RightGap.SMALL)
                .component
                .also { it.selectedItem = currentReplica?.replica }
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2ReplicaEnabled)
    }
}