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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.ui.view.CCv2ServiceDetailsView

class CCv2ShowServiceDetailsAction : DumbAwareAction("Show Service Details", null, HybrisIcons.CCv2.Service.Actions.SHOW_DETAILS) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.getData(CCv2UiConstants.DataKeys.Subscription) ?: return
        val environment = e.getData(CCv2UiConstants.DataKeys.Environment) ?: return
        val service = e.getData(CCv2UiConstants.DataKeys.Service) ?: return
        val toolWindow = ToolWindowManager.Companion.getInstance(project)
            .getToolWindow(HybrisConstants.TOOLWINDOW_ID) ?: return
        val contentManager = toolWindow.contentManager
        val panel = CCv2ServiceDetailsView(project, subscription, environment, service)
        val content = contentManager.factory
            .createContent(panel, service.name, true)
            .also {
                it.isCloseable = true
                it.isPinnable = true
                it.icon = HybrisIcons.CCv2.Service.Actions.SHOW_DETAILS
                it.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            }

        Disposer.register(toolWindow.disposable, panel)

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return
    }

}