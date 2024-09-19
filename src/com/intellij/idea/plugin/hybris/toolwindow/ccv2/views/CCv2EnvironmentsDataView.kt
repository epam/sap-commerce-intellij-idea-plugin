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

import com.intellij.ide.HelpTooltip
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.CCv2ShowEnvironmentDetailsAction
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentStatusEnum
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2ViewUtil
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.dsl.builder.*
import java.util.*

object CCv2EnvironmentsDataView : AbstractCCv2DataView<CCv2EnvironmentDto>() {

    override val tab: CCv2Tab
        get() = CCv2Tab.ENVIRONMENTS

    override fun dataPanel(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = panel(project, data)

    fun dataPanelWithBuilds(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) = panel(project, data, true)

    private fun panel(project: Project, data: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>, showBuilds: Boolean = false): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, environments) ->
            collapsibleGroup(subscription.toString()) {
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
        .let { Dsl.scrollPanel(it) }

    private fun Panel.environment(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, showBuilds: Boolean = false) {
        row {
            panel {
                row {
                    actionButton(
                        CCv2ShowEnvironmentDetailsAction(subscription, environment),
                        ActionPlaces.TOOLWINDOW_CONTENT
                    )
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
                    icon(HybrisIcons.CCv2.DYNATRACE)
                        .gap(RightGap.SMALL)
                    browserLink("Dynatrace", environment.dynatraceLink ?: "")
                        .enabled(environment.dynatraceLink != null)
                        .comment(environment.problems
                            ?.let { "problems: <strong>$it</strong>" } ?: "&nbsp;")
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
                                .installOn(this);
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
