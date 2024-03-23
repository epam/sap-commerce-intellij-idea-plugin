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

package com.intellij.idea.plugin.hybris.toolwindow.cli

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.cli.CCv2CLIEnvironmentListener
import com.intellij.idea.plugin.hybris.tools.cli.CCv2CLIService
import com.intellij.idea.plugin.hybris.tools.cli.dto.CCv2Environment
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import java.io.Serial
import javax.swing.DefaultComboBoxModel

class CCv2CLIView(val project: Project) : SimpleToolWindowPanel(false), Disposable {

    override fun dispose() = Unit

    private val tabsLoadedState = CCv2CLITab.entries
        .associateWith { false }
        .toMutableMap()

    // TODO: add new TOPIC for CCv2Subscriptions
    private val ccv2SubscriptionsModel = DefaultComboBoxModel<CCv2Subscription>()
    private var tabbedPane = JBTabbedPane().also {
        CCv2CLITab.entries.forEach { tab ->
            it.addTab(tab.title, tab.icon, noDataPanel(tab))
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
        }
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(RetrieveEnvironmentsAction())
            ActionManager.getInstance().createActionToolbar("SAP_CX_CCv2_CLI_View", this, false)
        }
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun installDataListeners() {
        with(project.messageBus.connect(this)) {
            // Environments data listeners
            subscribe(CCv2CLIService.TOPIC_ENVIRONMENT, object : CCv2CLIEnvironmentListener {
                override fun fetchingCompleted(environments: Map<CCv2Subscription, Collection<CCv2Environment>>) {
                    val index = getTabIndex(CCv2CLITab.ENVIRONMENTS)
                    tabbedPane.setComponentAt(index, environmentsDataPanel(environments))
                }

                override fun fetchingStarted() {
                    if (tabsLoadedState[CCv2CLITab.ENVIRONMENTS]!!) return
                    tabsLoadedState[CCv2CLITab.ENVIRONMENTS] = true

                    val index = getTabIndex(CCv2CLITab.ENVIRONMENTS)

                    tabbedPane.setComponentAt(index, fetchingInProgress(CCv2CLITab.ENVIRONMENTS))
                }
            })
        }
    }

    private fun getTabIndex(tab: CCv2CLITab): Int = tabbedPane.indexOfTab(tab.title)

    private fun environmentsDataPanel(environments: Map<CCv2Subscription, Collection<CCv2Environment>>) = panel {
        environments.forEach { (subscription, environments) ->
            collapsibleGroup(subscription.toString()) {
                environments.forEach {
                    row {
                        panel {
                            row {
                                label(it.name)
                                    .comment(it.code)
                                    .bold()
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                label(it.type.title)
                                    .comment("Type")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                label(it.status)
                                    .comment("Status")
                            }
                        }.gap(RightGap.COLUMNS)

                        panel {
                            row {
                                label(it.deploymentStatus)
                                    .comment("Deployment status")
                            }
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
                .expanded = true
        }
    }

    private fun fetchingInProgress(tab: CCv2CLITab): DialogPanel {
        return panel {
            row {
                label("Fetching ${tab.title} data, Please wait...")
                    .align(Align.FILL)
            }
        }
    }

    private fun noDataPanel(tab: CCv2CLITab): DialogPanel {
        val noDataPanel = panel {
            row {
                label("No ${tab.title} data available...")
                    .align(Align.FILL)
            }
        }
        return noDataPanel
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -3734294049693312978L
    }
}