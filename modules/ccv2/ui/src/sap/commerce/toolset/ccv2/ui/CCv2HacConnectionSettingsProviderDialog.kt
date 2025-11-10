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

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2EndpointDto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentStatus
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.awt.BorderLayout
import java.util.*

/**
 * TODO: Respect webcontext for /hac specified in the manifest.json
 * TODO: no subscriptions mode
 */
class CCv2HacConnectionSettingsProviderDialog(
    private val project: Project,
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    private lateinit var subscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var environmentComboBox: ComboBox<CCv2EnvironmentDto>
    private lateinit var endpointComboBox: ComboBox<CCv2EndpointDto>
    private lateinit var environments: SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>
    private lateinit var jbLoadingPanel: JBLoadingPanel

    private val settings = HacConnectionSettingsState().mutable()
    private val name = AtomicProperty("")
    private val host = AtomicProperty("")

    private val editable = AtomicBooleanProperty(true)
    private val subscriptionsComboBoxModel = CCv2SubscriptionsComboBoxModelFactory.create(project, null)
    private val endpointModel by lazy { CollectionComboBoxModel<CCv2EndpointDto>() }
    private val environmentModel by lazy { CollectionComboBoxModel<CCv2EnvironmentDto>() }

    fun showAndRetrieve() = if (showAndGet()) settings
    else null

    init {
        title = "CCv2 Endpoint Selection"
        isResizable = false

        super.init()

        fetchEndpoints()
    }

    override fun createCenterPanel() = panel {
        row {
            subscriptionComboBox = comboBox(
                subscriptionsComboBoxModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = HybrisIcons.Module.CCV2
                        label.text = value.presentableName
                    }
                }
            )
                .label("Subscription:")
                .enabledIf(editable)
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .addValidationRule("Please select the subscription.") { it.selectedItem == null }
                .onChanged {
                    it.selectedItem
                        ?.asSafely<CCv2Subscription>()
                        ?.let { subscription -> updateEnvironments(subscription) }
                }
                .component

            actionButton(object : AnAction("Refresh", "", HybrisIcons.Actions.REFRESH) {
                override fun getActionUpdateThread() = ActionUpdateThread.BGT
                override fun actionPerformed(e: AnActionEvent) {
                    fetchEndpoints(true)
                }
            })
                .enabledIf(editable)
        }.layout(RowLayout.PARENT_GRID)

        row {
            environmentComboBox = comboBox(
                environmentModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = value.type.icon
                        label.text = value.name
                    }
                }
            )
                .label("Environment:")
                .enabledIf(editable)
                .align(AlignX.FILL)
                .addValidationRule("Please select the environment.") { it.selectedItem == null }
                .onChanged {
                    it.selectedItem
                        ?.asSafely<CCv2EnvironmentDto>()
                        ?.let { environment -> updateEndpoints(environment) }
                }
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            endpointComboBox = comboBox(
                endpointModel,
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = HybrisIcons.CCv2.ENDPOINTS
                        label.text = value.name
                    }
                }
            )
                .label("Endpoint:")
                .enabledIf(editable)
                .align(AlignX.FILL)
                .addValidationRule("Please select the endpoint.") { it.selectedItem == null }
                .onChanged {
                    val environment = endpointComboBox.selectedItem.asSafely<CCv2EnvironmentDto>()
                        ?: return@onChanged
                    it.selectedItem
                        ?.asSafely<CCv2EndpointDto>()
                        ?.let { endpoint ->
                            val fullName = "${environment.code} | ${endpoint.name}"
                            settings.name.set(fullName)
                            settings.host.set(endpoint.url)

                            name.set(StringUtil.first(fullName, 40, true))
                            host.set(StringUtil.first(endpoint.url, 40, true))
                        }
                }
                .component
        }.layout(RowLayout.PARENT_GRID)

        group("Connection Settings") {
            row {
                text("")
                    .label("Name:")
                    .bindText(name)
                    .enabled(false)
            }.layout(RowLayout.PARENT_GRID)

            row {
                text("")
                    .label("Host:")
                    .bindText(host)
                    .enabled(false)
            }.layout(RowLayout.PARENT_GRID)
        }
    }
        .let {
            jbLoadingPanel = JBLoadingPanel(BorderLayout(), disposable)
            jbLoadingPanel.add(it, BorderLayout.CENTER)

            return@let jbLoadingPanel
        }

    private fun updateEnvironments(subscription: CCv2Subscription) {
        environmentModel.removeAll()
        environments[subscription]
            ?.toList()
            ?.let { environmentModel.addAll(0, it) }
        environmentModel.selectedItem = null
    }

    private fun updateEndpoints(environment: CCv2EnvironmentDto) {
        endpointModel.removeAll()
        environment.endpoints
            ?.toList()
            ?.let { endpointModel.addAll(0, it) }
        endpointModel.selectedItem = null
    }

    private fun fetchEndpoints(resetCache: Boolean = false) {
        isOKActionEnabled = false

        startLoading("Loading endpoints, please wait...")

        val ccv2Service = CCv2Service.getInstance(project)

        if (resetCache) ccv2Service.resetCache()

        ccv2Service.fetchEnvironments(
            subscriptions = CCv2ProjectSettings.getInstance().subscriptions,
            statuses = EnumSet.of(CCv2EnvironmentStatus.AVAILABLE),
            requestEndpoints = true,
            onCompleteCallback = {
                environments = it
                updateEnvironments(subscriptionComboBox.selectedItem as CCv2Subscription)
                endpointModel.removeAll()

                isOKActionEnabled = true

                stopLoading()
            },
            sendEvents = false
        )
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