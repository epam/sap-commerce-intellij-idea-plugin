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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2.views

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBEmptyBorder

object CCv2EnvironmentsDataView : CCv2DataView() {

    override val tab: CCv2Tab
        get() = CCv2Tab.ENVIRONMENTS

    fun dataPanel(environments: Map<CCv2Subscription, Collection<CCv2Environment>>): DialogPanel {
        if (environments.isEmpty()) return noDataPanel()

        val content = panel {
            environments.forEach { (subscription, environments) ->
                collapsibleGroup(subscription.toString()) {
                    if (environments.isEmpty()) {
                        noData()
                    } else {
                        environments.forEach { it ->
                            environment(it)
                        }
                    }
                }
                    .expanded = true
            }
        }

        return panel {
            row {
                scrollCell(content)
                    .align(Align.FILL)
                    .resizableColumn()
                    .applyToComponent {
                        (this.parent.parent as? JBScrollPane)
                            ?.border = JBEmptyBorder(0)
                    }

            }.resizableRow()
        }
    }

    private fun Panel.environment(environment: CCv2Environment) {
        row {
            panel {
                row {
                    label(environment.name)
                        .comment(environment.code)
                        .bold()
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(environment.type.title)
                        .comment("Type")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(environment.status)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(environment.deploymentStatus)
                        .comment("Deployment status")
                }
            }
        }.layout(RowLayout.PARENT_GRID)
    }
}