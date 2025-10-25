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
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.CCv2UiConstants

class CCv2DeleteEndpointAction : DumbAwareAction(
    "Delete Endpoint",
    null,
    HybrisIcons.CCv2.Endpoint.DELETE
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val endpoint = e.getData(CCv2UiConstants.DataKeys.Endpoint) ?: return

        e.presentation.isEnabled = endpoint.actionsAllowed
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.getData(CCv2UiConstants.DataKeys.Subscription) ?: return
        val environment = e.getData(CCv2UiConstants.DataKeys.Environment) ?: return
        val endpoint = e.getData(CCv2UiConstants.DataKeys.Endpoint) ?: return

        val modalResult = Messages.showYesNoDialog(
            project,
            "Delete the selected endpoint?",
            "Delete Endpoint",
            HybrisIcons.CCv2.Endpoint.DELETE
        )
        if (modalResult != Messages.YES) return

        CCv2Service.getInstance(project).deleteEndpoint(project, subscription, environment, endpoint)
    }
}