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

package com.intellij.idea.plugin.hybris.tools.ccv2.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2ServiceDetailsView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager

class CCv2ShowServiceDetailsAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    private val service: CCv2ServiceDto,
) : DumbAwareAction("Show Service Details", null, HybrisIcons.CCV2_SERVICE) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(HybrisToolWindowFactory.ID) ?: return
        val contentManager = toolWindow.contentManager
        val panel = CCv2ServiceDetailsView(project, subscription, environment, service)
        val content = contentManager.factory
            .createContent(panel, service.name, true)
            .also {
                it.isCloseable = true
                it.isPinnable = true
                it.icon = HybrisIcons.CCV2_SERVICE
                it.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            }

        Disposer.register(toolWindow.disposable, panel)

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

}
