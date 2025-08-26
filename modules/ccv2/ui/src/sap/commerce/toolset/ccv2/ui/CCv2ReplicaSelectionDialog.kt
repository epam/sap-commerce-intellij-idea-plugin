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

package sap.commerce.toolset.ccv2.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2ExecConstants
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.ccv2.ui.tree.CCv2TreeTable
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.exec.context.GroovyReplicaAwareContext
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JComponent

class CCv2ReplicaSelectionDialog(
    private val project: Project,
    private val editor: Editor,
    private val currentSettings: GroovyExecContext.Settings,
    parentComponent: Component,
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE), Disposable {

    private val replicaContext = currentSettings.replicaContext
        .takeIf { it.replicaSelectionMode == CCv2ExecConstants.ccv2 }
        ?: GroovyReplicaAwareContext(CCv2ExecConstants.ccv2)
    private val previousReplicaIds = replicaContext.replicaContexts.map { it.replicaId }
    private val selectedReplicaIds = mutableSetOf<String>()
    private val editable = AtomicBooleanProperty(true)
    private val ccv2TreeTable by lazy {
        CCv2TreeTable(previousReplicaIds, selectedReplicaIds)
            .apply {
                Disposer.register(this@CCv2ReplicaSelectionDialog, this)

                loadingState.afterChange {
                    if (it != null) startLoading("Loading replicas for ${it.name}, please wait...")
                    else stopLoading()
                }
            }
    }

    private val ccv2SubscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, null)

    init {
        title = "CCv2 Replica Selection"
        isResizable = false

        super.init()
    }

    private lateinit var jbLoadingPanel: JBLoadingPanel
    private lateinit var ccv2SubscriptionComboBox: ComboBox<CCv2Subscription>

    override fun createCenterPanel(): JComponent {
        val centerPanel = panel {
            ccv2Settings()
        }
            .apply {
                border = JBUI.Borders.empty(16)
                preferredSize = JBUI.DialogSizes.large()
            }

        return JBLoadingPanel(BorderLayout(), this)
            .apply {
                add(centerPanel, BorderLayout.CENTER)
                jbLoadingPanel = this
            }
            .also {
                ccv2SubscriptionComboBox.selectedItem.asSafely<CCv2Subscription>()
                    ?.let {
                        ccv2TreeTable.refresh(project, it)
                    }
            }
    }

    private fun Panel.ccv2Settings() {
        row {
            cell(
                InlineBanner(
                    "Each selected replica will result into a standalone request to hAC associated with <strong>ROUTE</strong> cookie value of the replica id.",
                    EditorNotificationPanel.Status.Info
                )
                    .showCloseButton(false)
            )
                .customize(UnscaledGaps(12, 12, 12, 12))
                .align(Align.CENTER)
        }

        row {
            ccv2SubscriptionComboBox = comboBox(
                ccv2SubscriptionsComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = HybrisIcons.Module.CCV2
                        label.text = value.presentableName
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
                    ccv2TreeTable.resetTree()
                    CCv2Service.getInstance(project).resetCache()
                }
            })
                .enabledIf(editable)
        }

        row {
            scrollCell(ccv2TreeTable)
                .align(Align.FILL)
        }
            .resizableRow()
    }

    override fun applyFields() {
        super.applyFields()

        val context = selectedReplicaIds
            .takeIf { it.isNotEmpty() }
            ?.let { replicas -> GroovyReplicaAwareContext(CCv2ExecConstants.ccv2, replicas.map { ReplicaContext(it) }) }
            ?: GroovyReplicaAwareContext.auto()

        editor.groovyExecContextSettings = currentSettings.copy(replicaContext = context)
    }

    override fun dispose() {
        super.dispose()
    }

    private fun startLoading(text: String = "Loading...") {
        editable.set(false)
        jbLoadingPanel.setLoadingText(text)
        jbLoadingPanel.startLoading()
    }

    private fun stopLoading() {
        editable.set(true)
        jbLoadingPanel.stopLoading()
    }
}