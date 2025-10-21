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

import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.dsl.builder.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.dto.CCv2DeploymentStatusEnum
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.toolwindow.CCv2ViewUtil
import sap.commerce.toolset.ccv2.ui.dynatrace
import sap.commerce.toolset.ui.actionButton
import sap.commerce.toolset.ui.scrollPanel
import java.util.*

object CCv2EnvironmentsDataView : CCv2DataView<CCv2EnvironmentDto>() {

    override val tab: CCv2Tab
        get() = CCv2Tab.ENVIRONMENTS

    override fun dataPanel(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = panel(project, data)

    fun dataPanelWithBuilds(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = panel(project, data, true)

    private fun panel(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>, showBuilds: Boolean = false): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, environments) ->
            collapsibleGroup(subscription.presentableName) {
                if (environments.isEmpty()) {
                    noData()
                } else {
                    environments
                        .sortedWith(compareBy({ it.type }, { it.name }))
                        .forEach { environment(project, subscription, it, showBuilds) }
                }
            }
                .expanded = showBuilds
        }
    }
        .let { scrollPanel(it) }

    private fun Panel.environment(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, showBuilds: Boolean = false) {
        row {
            panel {
                row {
                    actionButton(
                        ActionManager.getInstance().getAction("ccv2.environment.showDetails.action"),
                        ActionPlaces.TOOLWINDOW_CONTENT
                    ) {
                        it[CCv2UiConstants.DataKeys.Subscription] = subscription
                        it[CCv2UiConstants.DataKeys.Environment] = environment
                    }
                }
            }.gap(RightGap.SMALL)

            panel {
                row {
                    val environmentName = environment.link
                        ?.let { browserLink(environment.name, it) }
                        ?: label(environment.name)
                    environmentName.comment(environment.code)
                        .bold()
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.type.icon)
                        .gap(RightGap.SMALL)
                    label(environment.type.title)
                        .comment("Type")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.status.icon)
                        .gap(RightGap.SMALL)
                    label(environment.status.title)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(environment.deploymentStatus.icon)
                        .gap(RightGap.SMALL)
                    label(environment.deploymentStatus.title)
                        .comment("Deployment status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    dynatrace(environment)
                }
            }.gap(RightGap.SMALL)

            panel {
                row {
                    icon(HybrisIcons.CCv2.OPENSEARCH)
                        .gap(RightGap.SMALL)
                    browserLink("OpenSearch", environment.loggingLink ?: "")
                        .enabled(environment.loggingLink != null)
                        .comment("&nbsp;")
                }
            }.gap(RightGap.COLUMNS)

            if (showBuilds) {
                buildPanel(project, subscription, environment)
            }
        }.layout(RowLayout.PARENT_GRID)
    }

    private fun Row.buildPanel(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto) {
        val deployedBuild = environment.deployedBuild
        if (deployedBuild != null) {
            panel {
                row {
                    icon(HybrisIcons.CCv2.BUILDS)
                        .gap(RightGap.SMALL)
                    link(deployedBuild.name) {
                        CCv2ViewUtil.showBuildDetailsTab(project, subscription, deployedBuild)
                    }
                        .bold()
                        .comment("Build name")
                        .applyToComponent {
                            HelpTooltip()
                                .setTitle("Show build details")
                                .installOn(this)
                        }
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