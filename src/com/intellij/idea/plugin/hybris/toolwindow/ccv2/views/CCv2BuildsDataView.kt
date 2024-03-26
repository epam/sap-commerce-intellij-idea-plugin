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
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CCv2BuildsDataView : AbstractCCv2DataView<CCv2Build>() {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss")
    private val localTimeZone = ZoneId.systemDefault()
    override val tab: CCv2Tab
        get() = CCv2Tab.BUILDS

    override fun dataPanel(data: Map<CCv2Subscription, Collection<CCv2Build>>): DialogPanel = if (data.isEmpty()) noDataPanel()
    else panel {
        data.forEach { (subscription, builds) ->
            collapsibleGroup(subscription.toString()) {
                if (builds.isEmpty()) {
                    noData()
                } else {
                    builds.forEach { build(it) }
                }
            }
                .expanded = true
        }
    }
        .let { scrollPanel(it) }

    private fun Panel.build(build: CCv2Build) {
        row {
            panel {
                row {
                    label(build.name)
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
                    icon(HybrisIcons.CCV2_BUILD_BRANCH)
                    label(build.branch)
                        .comment("Branch")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(build.status.icon)
                    label(build.status.title)
                        .comment("Status")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    icon(HybrisIcons.CCV2_BUILD_CREATED_BY)
                    label(build.createdBy)
                        .comment("Created by")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(build.startTimeFormatted)
                        .comment("Start time")
                }
            }.gap(RightGap.COLUMNS)

            panel {
                row {
                    label(build.endTimeFormatted)
                        .comment("End time")
                }
            }
        }.layout(RowLayout.PARENT_GRID)
    }
}