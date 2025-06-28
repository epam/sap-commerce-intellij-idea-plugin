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
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import java.awt.Component
import javax.swing.DefaultComboBoxModel

class ReplicaSelectionDialog(
    private val project: Project,
    parentComponent: Component
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE) {

    private val customReplicaSettings = AtomicBooleanProperty(true)
    private val ccv2ReplicaSettings = AtomicBooleanProperty(false)
    private val ccv2EnvironmentEnabled = AtomicBooleanProperty(false)
    private val ccv2ServiceEnabled = AtomicBooleanProperty(false)
    private val ccv2ReplicaEnabled = AtomicBooleanProperty(false)

    private val replicaType = AtomicProperty<ReplicaType?>(ReplicaType.CUSTOM).also {
        it.afterChange { selectedReplica ->
            customReplicaSettings.set(selectedReplica == ReplicaType.CUSTOM)
            ccv2ReplicaSettings.set(selectedReplica == ReplicaType.CCV2)
        }
    }
    private val ccv2EnvironmentComboBoxModel = DefaultComboBoxModel<CCv2EnvironmentDto>()
    private val ccv2ServiceComboBoxModel = DefaultComboBoxModel<CCv2ServiceDto>()
    private val ccv2ReplicaComboBoxModel = DefaultComboBoxModel<CCv2ServiceReplicaDto>()

    init {
        title = "Replica Selection"
        super.init()
    }

    private lateinit var customCookieName: JBTextField
    private lateinit var customReplicaId: JBTextField
    private lateinit var ccv2SubscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var ccv2EnvironmentComboBox: ComboBox<CCv2EnvironmentDto>
    private lateinit var ccv2ServiceComboBox: ComboBox<CCv2ServiceDto>
    private lateinit var ccv2ReplicaComboBox: ComboBox<CCv2ServiceReplicaDto>

    override fun createCenterPanel() = panel {
        row {
            comboBox(
                EnumComboBoxModel(ReplicaType::class.java),
                renderer = SimpleListCellRenderer.create("") { it.title }
            )
                .label("Replica type:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .onChanged { replicaType.set(it.selectedItem as ReplicaType) }
        }.layout(RowLayout.PARENT_GRID)

        row {
            customReplicaId = textField()
                .label("Replica id:")
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .visibleIf(customReplicaSettings)

        row {
            customCookieName = textField()
                .label("Cookie name:")
                .align(AlignX.FILL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .visibleIf(customReplicaSettings)

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
                        },
                        onCompleteCallback = { response ->
                            response[subscription]?.let { environments ->
                                ccv2EnvironmentComboBoxModel.addAll(environments)

                                ccv2EnvironmentEnabled.set(true)
                            }
                        }
                    )
                }
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .visibleIf(ccv2ReplicaSettings)

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
                        },
                        onCompleteCallback = { response ->
                            ccv2ServiceComboBoxModel.removeAllElements()
                            response?.let { services ->
                                ccv2ServiceComboBoxModel.addAll(services)

                                ccv2ServiceEnabled.set(true)
                                ccv2ReplicaEnabled.set(true)
                            }

                        }
                    )
                }
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2EnvironmentEnabled)
            .visibleIf(ccv2ReplicaSettings)

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
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2ServiceEnabled)
            .visibleIf(ccv2ReplicaSettings)

        row {
            ccv2ReplicaComboBox = comboBox(
                ccv2ReplicaComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.name }
            )
                .label("Replica:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .component
        }
            .layout(RowLayout.PARENT_GRID)
            .enabledIf(ccv2ReplicaEnabled)
            .visibleIf(ccv2ReplicaSettings)
    }
        .also {
            it.border = JBUI.Borders.empty(16)
        }

    override fun applyFields() {
        super.applyFields()

        val replica = when (replicaType.get()) {
            ReplicaType.CUSTOM -> customReplicaId.text
                .takeIf { it.isNotBlank() }
                ?.let {
                    Replica(
                        id = it,
                        cookieName = customCookieName.text
                    )
                }

            ReplicaType.CCV2 -> ccv2ReplicaComboBox.selectedItem?.asSafely<CCv2ServiceReplicaDto>()
                ?.let {
                    Replica(
                        id = it.name,
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
}