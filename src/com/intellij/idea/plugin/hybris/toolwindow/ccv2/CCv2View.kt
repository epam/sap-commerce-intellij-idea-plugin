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
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2EnvironmentListener
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
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
import com.intellij.util.asSafely
import java.io.Serial
import javax.swing.DefaultComboBoxModel

class CCv2View(val project: Project) : SimpleToolWindowPanel(false), Disposable {

    override fun dispose() = Unit

    private val tabsLoadedState = CCv2Tab.entries
        .associateWith { false }
        .toMutableMap()

    // TODO: add new TOPIC for CCv2Subscriptions
    private val ccv2SubscriptionsModel = DefaultComboBoxModel<CCv2Subscription>()
    private val tabbedPane = JBTabbedPane().also {
        CCv2Tab.entries.forEach { tab ->
            it.addTab(tab.title, tab.icon, tab.view.noDataPanel())
        }

        it.addChangeListener { event ->
            val selectedIndex = event.source.asSafely<JBTabbedPane>()
                ?.selectedIndex

            //TODO: show data loading text and get data right away
            println(selectedIndex)
        }
    }

    init {
        val ccv2Subscriptions = HybrisApplicationSettingsComponent.getInstance().state.ccv2Subscriptions
        ccv2SubscriptionsModel.addElement(null)
        ccv2SubscriptionsModel.addAll(ccv2Subscriptions)

        add(rootPanel())
        installToolbar()
        installDataListeners()
    }

    private fun rootPanel() = panel {
        indent {
            row {
                comboBox(
                    ccv2SubscriptionsModel,
                    renderer = SimpleListCellRenderer.create("-- all subscriptions --") { it.toString() }
                )
                    .label("Subscription:")
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
            add(RetrieveEnvironmentsAction())
            ActionManager.getInstance().createActionToolbar("SAP_CX_CCv2_View", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installDataListeners() {
        with(project.messageBus.connect(this)) {
            // Environments data listeners
            subscribe(CCv2Service.TOPIC_ENVIRONMENT, object : CCv2EnvironmentListener {
                override fun fetchingStarted() {
                    if (tabsLoadedState[CCv2Tab.ENVIRONMENTS]!!) return
                    tabsLoadedState[CCv2Tab.ENVIRONMENTS] = true

                    val index = getTabIndex(CCv2Tab.ENVIRONMENTS)

                    tabbedPane.setComponentAt(index, CCv2EnvironmentsDataView.fetchingInProgress())
                }

                override fun fetchingCompleted(environments: Map<CCv2Subscription, Collection<CCv2Environment>>) {
                    val index = getTabIndex(CCv2Tab.ENVIRONMENTS)

                    tabbedPane.setComponentAt(index, CCv2EnvironmentsDataView.dataPanel(environments))
                }
            })
        }
    }

    private fun getTabIndex(tab: CCv2Tab): Int = tabbedPane.indexOfTab(tab.title)

    companion object {
        @Serial
        private val serialVersionUID: Long = -3734294049693312978L
    }
}