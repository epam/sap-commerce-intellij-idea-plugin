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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.ccv2.dto.CCv2Dto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ui.scrollPanel

abstract class CCv2DataView<T : CCv2Dto> {

    abstract val tab: CCv2Tab
    abstract fun dataPanel(project: Project, data: Map<CCv2Subscription, Collection<T>>): DialogPanel

    fun fetchingInProgressPanel(subscriptions: Collection<CCv2Subscription>): DialogPanel = panel {
        subscriptions.forEach {
            collapsibleGroup(it.presentableName) {
                row {
                    label("Fetching ${tab.title} data, Please wait...")
                        .align(Align.CENTER)
                        .resizableColumn()
                }.resizableRow()
            }
                .expanded = true
        }
    }
        .let { scrollPanel(it) }

    fun noDataPanel(): DialogPanel = panel {
        noData()
    }

    protected fun Panel.noData() {
        row {
            cell(
                InlineBanner(
                    "No ${tab.title} data available. Try re-fetching remote data...",
                    EditorNotificationPanel.Status.Warning
                ).showCloseButton(false)
            )
                .align(Align.CENTER)
                .resizableColumn()
        }
            .resizableRow()
            .topGap(TopGap.MEDIUM)
    }
}