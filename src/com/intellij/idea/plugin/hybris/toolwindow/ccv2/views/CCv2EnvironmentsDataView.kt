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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentStatusEnum
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.dsl.builder.*
import java.util.*

object CCv2EnvironmentsDataView : AbstractCCv2DataView<CCv2Environment>() {

    override val tab: CCv2Tab
        get() = CCv2Tab.ENVIRONMENTS

    override fun dataPanel(data: Map<CCv2Subscription, Collection<CCv2Environment>>) = panel(data)

    fun dataPanelWithBuilds(data: Map<CCv2Subscription, Collection<CCv2Environment>>) = panel(data, true)

    private fun panel(data: Map<CCv2Subscription, Collection<CCv2Environment>>, showBuilds: Boolean = false): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, environments) ->
            collapsibleGroup(subscription.toString()) {
                if (environments.isEmpty()) {
                    noData()
                } else {
                    environments.forEach { environment(it, showBuilds) }
                }
            }
                .expanded = showBuilds
        }
    }
        .let { scrollPanel(it) }

    private fun Panel.environment(environment: CCv2Environment, showBuilds: Boolean = false) {
        row {
            val deployedBuild = environment.deployedBuild

            panel {
                row {
                    label(environment.name)
                        .comment(environment.code)
                        .bold()
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.type.icon)
                    label(environment.type.title)
                        .comment("Type")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.status.icon)
                    label(environment.status.title)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.deploymentStatus.icon)
                    label(environment.deploymentStatus.title)
                        .comment("Deployment status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(HybrisIcons.DYNATRACE)
                    browserLink("Dynatrace", environment.dynatraceLink ?: "")
                        .enabled(environment.dynatraceLink != null)
                        .comment("&nbsp;")
                }
            }.gap(RightGap.COLUMNS)

            if (showBuilds) {
                buildPanel(environment, deployedBuild)
            }
        }.layout(RowLayout.PARENT_GRID)
    }

    private fun Row.buildPanel(environment: CCv2Environment, deployedBuild: CCv2Build?) {
        if (deployedBuild != null) {
            panel {
                row {
                    label(deployedBuild.name)
                        .bold()
                        .comment("Build name")
                }
            }.gap(RightGap.COLUMNS)
            panel {
                row {
                    label(deployedBuild.code)
                        .comment("Build code")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(deployedBuild.branch)
                        .comment("Build branch")
                }
            }
        } else if (!EnumSet.of(CCv2DeploymentStatusEnum.UNDEPLOYED, CCv2DeploymentStatusEnum.UNKNOWN).contains(environment.deploymentStatus)) {
            panel {
                row {
                    icon(AnimatedIcon.Default.INSTANCE)
                        .comment("Build details")
                }
            }
        }
    }
}
