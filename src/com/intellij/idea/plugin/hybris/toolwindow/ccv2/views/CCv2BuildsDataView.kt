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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2.views

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.actions.*
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildDto
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.copyLink
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.date
import com.intellij.idea.plugin.hybris.tools.ccv2.ui.sUser
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

object CCv2BuildsDataView : AbstractCCv2DataView<CCv2BuildDto>() {

    override val tab: CCv2Tab
        get() = CCv2Tab.BUILDS

    override fun dataPanel(project: Project, data: Map<CCv2Subscription, Collection<CCv2BuildDto>>): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, builds) ->
            collapsibleGroup(subscription.toString()) {
                if (builds.isEmpty()) {
                    noData()
                } else {
                    builds.forEach { build(project, subscription, it) }
                }
            }
                .expanded = true
        }
    }
        .let { Dsl.scrollPanel(it) }

    private fun Panel.build(project: Project, subscription: CCv2Subscription, build: CCv2BuildDto) {
        row {
            panel {
                row {
                    actionsButton(
                        actions = listOfNotNull(
                            if (build.canTrack()) CCv2TrackBuildAction(subscription, build) else null,
                            CCv2ShowBuildDetailsAction(subscription, build),
                            CCv2RedoBuildAction(subscription, build),
                            if (build.canDeploy()) CCv2DeployBuildAction(subscription, build) else null,
                            if (build.canDelete()) CCv2DeleteBuildAction(subscription, build) else null,
                            if (build.canDownloadLogs()) CCv2DownloadBuildLogsAction(subscription, build) else null
                        ).toTypedArray(),
                        ActionPlaces.TOOLWINDOW_CONTENT
                    )
                }
            }.gap(RightGap.SMALL)

            panel {
                row {
                    val buildName = build.link
                        ?.let { browserLink(build.name, it) }
                        ?: label(build.name)
                    buildName
                        .comment(build.code)
                        .bold()
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(build.version)
                        .comment("Version")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(HybrisIcons.CCv2.Build.REVISION).gap(RightGap.SMALL)
                    copyLink(project, "Revision", build.revision, "Build Revision copied to clipboard")
                }
            }.gap(RightGap.SMALL)

            panel {
                row {
                    icon(HybrisIcons.CCv2.Build.BRANCH).gap(RightGap.SMALL)
                    copyLink(project, "Branch", build.branch, "Build Branch copied to clipboard")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(build.status.icon)
                        .gap(RightGap.SMALL)
                    label(build.status.title)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    sUser(project, build.createdBy, HybrisIcons.CCv2.Build.CREATED_BY)
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    date("Start time", build.startTime)
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    date("End time", build.endTime)
                }
            }
        }.layout(RowLayout.PARENT_GRID)
    }
}