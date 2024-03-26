/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2BuildsListener
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2EnvironmentsListener
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.CCv2SubscriptionsComboBoxModelFactory
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2BuildsDataView
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2EnvironmentsDataView
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import java.io.Serial

class CCv2View(val project: Project) : SimpleToolWindowPanel(false), Disposable {

    override fun dispose() = Unit

    private val tabsLoadedState = CCv2Tab.entries
        .associateWith { false }
        .toMutableMap()

    // TODO: add new TOPIC for CCv2Subscriptions
    private val ccv2SubscriptionsModel = CCv2SubscriptionsComboBoxModelFactory.create(project, allowBlank = true) {
        if (it == null) {
            DeveloperSettingsComponent.getInstance(project).state.activeCCv2SubscriptionID = null
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
                    renderer = SimpleListCellRenderer.create("-- all subscriptions --") { it.toString() }
                )
                    .label("Subscription:")
                    .onChanged {
                        val devSettings = DeveloperSettingsComponent.getInstance(project).state

                        when (val element = it.selectedItem) {
                            is CCv2Subscription -> devSettings.activeCCv2SubscriptionID = element.id
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

            add(actionManager.getAction("ccv2.action.environment.fetch"))
            add(actionManager.getAction("ccv2.action.build.fetch"))
            add(actionManager.getAction("ccv2.action.build.create"))
            actionManager.createActionToolbar("SAP_CX_CCv2_View", this, false)
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
            subscribe(CCv2Service.TOPIC_ENVIRONMENT, object : CCv2EnvironmentsListener {
                override fun fetchingStarted() {
                    if (tabsLoadedState[CCv2Tab.ENVIRONMENTS]!!) return
                    tabsLoadedState[CCv2Tab.ENVIRONMENTS] = true

                    tabbedPane.setComponentAt(
                        getTabIndex(CCv2Tab.ENVIRONMENTS),
                        CCv2EnvironmentsDataView.fetchingInProgress()
                    )
                }

                override fun fetchingCompleted(data: Map<CCv2Subscription, Collection<CCv2Environment>>) = tabbedPane.setComponentAt(
                    getTabIndex(CCv2Tab.ENVIRONMENTS),
                    CCv2EnvironmentsDataView.dataPanel(project, data)
                )
            })

            // Builds data listeners
            subscribe(CCv2Service.TOPIC_BUILDS, object : CCv2BuildsListener {
                override fun fetchingStarted() {
                    if (tabsLoadedState[CCv2Tab.BUILDS]!!) return
                    tabsLoadedState[CCv2Tab.BUILDS] = true

                    tabbedPane.setComponentAt(
                        getTabIndex(CCv2Tab.BUILDS),
                        CCv2BuildsDataView.fetchingInProgress()
                    )
                }

                override fun fetchingCompleted(data: Map<CCv2Subscription, Collection<CCv2Build>>) = tabbedPane.setComponentAt(
                    getTabIndex(CCv2Tab.BUILDS),
                    CCv2BuildsDataView.dataPanel(project, data)
                )
            })
        }
    }

    private fun getTabIndex(tab: CCv2Tab): Int = tabbedPane.indexOfTab(tab.title)

    companion object {
        @Serial
        private val serialVersionUID: Long = -3734294049693312978L
    }
}