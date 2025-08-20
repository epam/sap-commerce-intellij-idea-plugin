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

package sap.commerce.toolset.ccv2.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.dto.CCv2BuildDto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.view.CCv2BuildDetailsView

object CCv2ViewUtil {

    fun showBuildDetailsTab(project: Project, subscription: CCv2Subscription, build: CCv2BuildDto) {
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(HybrisConstants.TOOLWINDOW_ID) ?: return
        val contentManager = toolWindow.contentManager
        val panel = CCv2BuildDetailsView(project, subscription, build)
        val content = contentManager.factory
            .createContent(panel, build.code, true)
            .also {
                it.isCloseable = true
                it.isPinnable = true
                it.icon = HybrisIcons.CCv2.Build.Actions.SHOW_DETAILS
                it.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            }

        Disposer.register(toolWindow.disposable, panel)

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

    fun noDataPanel(message: String) = panel {
        row {
            cell(
                InlineBanner(message, EditorNotificationPanel.Status.Info)
                    .showCloseButton(false)
            )
                .align(Align.CENTER)
                .resizableColumn()
        }
            .resizableRow()
            .topGap(TopGap.MEDIUM)
    }
}