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
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.CCv2SubscriptionsComboBoxModelFactory
import com.intellij.idea.plugin.hybris.tools.remote.ReplicaType
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2TreeTable
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
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.util.*
import javax.swing.JComponent

class ReplicasSelectionDialog(
    private val project: Project,
    private val currentReplicaType: ReplicaType = ReplicaType.MANUAL,
    parentComponent: Component
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE), Disposable {

    private val editable = AtomicBooleanProperty(true)

    private val manualReplicaSettings = AtomicBooleanProperty(currentReplicaType == ReplicaType.MANUAL)
    private val ccv2ReplicaSettings = AtomicBooleanProperty(currentReplicaType == ReplicaType.CCV2)
    private val ccv2SettingsRefresh = AtomicBooleanProperty(true)
    private val ccv2TreeTable by lazy { CCv2TreeTable() }

    private val replicaType = AtomicProperty(currentReplicaType).apply {
        afterChange { selectedReplica ->
            manualReplicaSettings.set(selectedReplica == ReplicaType.MANUAL)
            ccv2ReplicaSettings.set(selectedReplica == ReplicaType.CCV2)
        }
    }
    private val ccv2SubscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, null)

    init {
        title = "Batch Replica Selection"
        isResizable = false

        super.init()
    }

    private lateinit var jbLoadingPanel: JBLoadingPanel
    private lateinit var centerPanel: DialogPanel
    private lateinit var ccv2SubscriptionComboBox: ComboBox<CCv2Subscription>

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

    override fun createCenterPanel(): JComponent? {
        centerPanel = panel {
            row {
                segmentedButton(EnumSet.of(ReplicaType.MANUAL, ReplicaType.CCV2), {
                    this.text = it.title
                    this.icon = it.icon
                })
                    .align(AlignX.FILL)
                    .gap(RightGap.SMALL)
                    .whenItemSelected { replicaType.set(it) }
                    .apply {
                        selectedItem = currentReplicaType
                        enabledIf(editable)
                    }
            }.layout(RowLayout.PARENT_GRID)

            ccv2Settings().visibleIf(ccv2ReplicaSettings)
        }
            .apply {
                border = JBUI.Borders.empty(16)
                preferredSize = JBUI.DialogSizes.medium()
            }

//        ccv2TableModel.addRow(listOf("1", "2", "3").toTypedArray())
//        ccv2TableModel.addRow(listOf("1", "2", "3").toTypedArray())
//        ccv2TableModel.addRow(listOf("1", "2", "3").toTypedArray())
//        ccv2TableModel.addRow(listOf("1", "2", "3").toTypedArray())

        return JBLoadingPanel(BorderLayout(), this).apply {
            add(centerPanel, BorderLayout.CENTER)
            jbLoadingPanel = this
        }
    }

    private fun Panel.ccv2Settings() = panel {
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

                    ccv2TreeTable.refresh(project, subscription)
                }
                .component

            actionButton(object : AnAction("Refresh", "", HybrisIcons.Actions.REFRESH) {
                override fun getActionUpdateThread() = ActionUpdateThread.BGT
                override fun actionPerformed(e: AnActionEvent) {
                    ccv2SubscriptionsComboBoxModel.refresh()
                }
            })
                .align(AlignX.RIGHT)
                .enabledIf(ccv2SettingsRefresh)
        }
            .layout(RowLayout.PARENT_GRID)

        row {
            cell(ccv2TreeTable)
                .align(Align.FILL)
        }
            .layout(RowLayout.PARENT_GRID)
    }

    override fun dispose() {
        super.dispose()
    }
}