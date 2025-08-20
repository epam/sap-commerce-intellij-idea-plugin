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

package sap.commerce.toolset.ccv2.ui.view

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.actionSystem.CCv2TrackDeploymentAction
import sap.commerce.toolset.ccv2.dto.CCv2DeploymentDto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.ui.date
import sap.commerce.toolset.ccv2.ui.sUser
import sap.commerce.toolset.ui.scrollPanel

object CCv2DeploymentsDataView : CCv2DataView<CCv2DeploymentDto>() {

    override val tab: CCv2Tab
        get() = CCv2Tab.DEPLOYMENTS

    override fun dataPanel(project: Project, data: Map<CCv2Subscription, Collection<CCv2DeploymentDto>>): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, builds) ->
            collapsibleGroup(subscription.toString()) {
                if (builds.isEmpty()) {
                    noData()
                } else {
                    builds.forEach { deployment(project, subscription, it) }
                }
            }
                .expanded = true
        }
    }
        .let { scrollPanel(it) }

    private fun Panel.deployment(project: Project, subscription: CCv2Subscription, deployment: CCv2DeploymentDto) {
        row {
            panel {
                row {
                    actionsButton(
                        actions = listOfNotNull(
                            CCv2TrackDeploymentAction(subscription, deployment)
                        ).toTypedArray(),
                        ActionPlaces.TOOLWINDOW_CONTENT
                    )
                }
            }.gap(RightGap.SMALL)
            panel {
                row {
                    val deploymentCode = deployment.link
                        ?.let { browserLink(deployment.code, it) }
                        ?: label(deployment.code)
                    deploymentCode
                        .comment(deployment.buildCode)
                        .bold()
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(deployment.envCode)
                        .comment("Environment")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(deployment.status.icon)
                        .gap(RightGap.SMALL)
                    label(deployment.status.title)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(deployment.strategy.icon)
                        .gap(RightGap.SMALL)
                    label(deployment.strategy.title)
                        .comment("Strategy")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(deployment.updateMode.icon)
                        .gap(RightGap.SMALL)
                    label(deployment.updateMode.title)
                        .comment("Mode")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    sUser(project, deployment.createdBy, HybrisIcons.CCv2.Deployment.CREATED_BY)
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    date("Created time", deployment.createdTime)
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    date("Deployed time", deployment.deployedTime)
                }
            }

        }.layout(RowLayout.PARENT_GRID)
    }
}