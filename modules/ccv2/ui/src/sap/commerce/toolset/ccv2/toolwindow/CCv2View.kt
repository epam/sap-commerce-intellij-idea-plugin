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

package sap.commerce.toolset.ccv2.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2BuildDto
import sap.commerce.toolset.ccv2.dto.CCv2DeploymentDto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.event.CCv2BuildsListener
import sap.commerce.toolset.ccv2.event.CCv2DeploymentsListener
import sap.commerce.toolset.ccv2.event.CCv2EnvironmentsListener
import sap.commerce.toolset.ccv2.event.CCv2SettingsListener
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.ccv2.ui.view.CCv2BuildsDataView
import sap.commerce.toolset.ccv2.ui.view.CCv2DeploymentsDataView
import sap.commerce.toolset.ccv2.ui.view.CCv2EnvironmentsDataView
import java.io.Serial

class CCv2View(private val project: Project) : SimpleToolWindowPanel(false), Disposable {

    override fun dispose() = Unit

    private val ccv2SubscriptionsModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true, disposable = this) {
        if (it == null) {
            CCv2DeveloperSettings.getInstance(project).activeCCv2SubscriptionID = null
        }
    }

    private val tabbedPane = JBTabbedPane().also {
        CCv2Tab.entries.forEach { tab ->
            it.addTab(tab.title, tab.icon, tab.view.noDataPanel())
        }
    }

    init {
        add(rootPanel())
        installToolbar()
        installListeners()
    }

    fun getActiveTab() = CCv2Tab.entries
        .find { it.ordinal == tabbedPane.selectedIndex }

    private fun rootPanel() = panel {
        indent {
            row {
                comboBox(
                    ccv2SubscriptionsModel,
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        if (value != null) {
                            label.icon = HybrisIcons.Module.CCV2
                            label.text = value.presentableName
                        } else {
                            label.text = "-- all subscriptions --"
                        }
                    }
                )
                    .label("Subscription:")
                    .onChanged {
                        val devSettings = CCv2DeveloperSettings.getInstance(project)

                        when (val element = it.selectedItem) {
                            is CCv2Subscription -> devSettings.activeCCv2SubscriptionID = element.uuid
                            else -> devSettings.activeCCv2SubscriptionID = null
                        }
                    }
            }
                .topGap(TopGap.SMALL)
                .bottomGap(BottomGap.SMALL)
        }
        row {
            cell(tabbedPane)
                .align(Align.FILL)
        }.resizableRow()
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(actionManager.getAction("ccv2.toolbar.actions"))

            actionManager.createActionToolbar(PLACE, this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installListeners() {
        installDataListeners()
    }

    private fun installDataListeners() {
        with(project.messageBus.connect(this)) {
            // Environments data listeners
            subscribe(CCv2EnvironmentsListener.TOPIC, object : CCv2EnvironmentsListener {
                override fun onFetchingStarted(subscriptions: Collection<CCv2Subscription>) = onFetchingStarted(CCv2Tab.ENVIRONMENTS)
                { CCv2EnvironmentsDataView.fetchingInProgressPanel(subscriptions) }

                override fun onFetchingCompleted(data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = onFetchingCompleted(CCv2Tab.ENVIRONMENTS)
                {
                    val dataPanel = CCv2EnvironmentsDataView.dataPanel(project, data)
                    CCv2Service.getInstance(project).fetchEnvironmentsBuilds(data)

                    dataPanel
                }

                override fun onFetchingBuildDetailsStarted(data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = onFetchingCompleted(CCv2Tab.ENVIRONMENTS)
                { CCv2EnvironmentsDataView.dataPanelWithBuilds(project, data) }

                override fun onFetchingBuildDetailsCompleted(
                    data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>,
                ) = onFetchingCompleted(CCv2Tab.ENVIRONMENTS)
                { CCv2EnvironmentsDataView.dataPanelWithBuilds(project, data) }
            })

            // Builds data listeners
            subscribe(CCv2BuildsListener.TOPIC, object : CCv2BuildsListener {
                override fun onFetchingStarted(subscriptions: Collection<CCv2Subscription>) = onFetchingStarted(CCv2Tab.BUILDS)
                { CCv2BuildsDataView.fetchingInProgressPanel(subscriptions) }

                override fun onFetchingCompleted(data: Map<CCv2Subscription, Collection<CCv2BuildDto>>) = onFetchingCompleted(CCv2Tab.BUILDS)
                { CCv2BuildsDataView.dataPanel(project, data) }
            })

            // Deployments data listeners
            subscribe(CCv2DeploymentsListener.TOPIC, object : CCv2DeploymentsListener {
                override fun onFetchingStarted(subscriptions: Collection<CCv2Subscription>) = onFetchingStarted(CCv2Tab.DEPLOYMENTS)
                { CCv2DeploymentsDataView.fetchingInProgressPanel(subscriptions) }

                override fun onFetchingCompleted(data: Map<CCv2Subscription, Collection<CCv2DeploymentDto>>) = onFetchingCompleted(CCv2Tab.DEPLOYMENTS)
                { CCv2DeploymentsDataView.dataPanel(project, data) }
            })

            subscribe(CCv2SettingsListener.TOPIC, object : CCv2SettingsListener {
                override fun onActivation(subscription: CCv2Subscription?) {
                    ccv2SubscriptionsModel.selectedItem = subscription
                }
            })
        }
    }

    private fun onFetchingStarted(tab: CCv2Tab, createPanel: () -> DialogPanel) = tabbedPane.setComponentAt(
        getTabIndex(tab),
        createPanel.invoke()
    )

    private fun onFetchingCompleted(tab: CCv2Tab, createPanel: () -> DialogPanel) = invokeLater {
        tabbedPane.setComponentAt(
            getTabIndex(tab),
            createPanel.invoke()
        )
    }

    private fun getTabIndex(tab: CCv2Tab): Int = tabbedPane.indexOfTab(tab.title)

    companion object {
        @Serial
        private val serialVersionUID: Long = -3734294049693312978L
        const val TAB_NAME = "CCv2"
        const val PLACE = "SAP_CX_CCv2_View"

        fun getActiveTab(project: Project) = ToolWindowManager.getInstance(project)
            .getToolWindow(HybrisConstants.TOOLWINDOW_ID)
            ?.contentManager
            ?.findContent(TAB_NAME)
            ?.component
            ?.asSafely<CCv2View>()
            ?.getActiveTab()
    }
}