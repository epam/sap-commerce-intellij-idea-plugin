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
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.toolwindow.CCv2ViewUtil

class CCv2ShowBuildDetailsAction : DumbAwareAction(
    "Show Build Details",
    null,
    HybrisIcons.CCv2.Build.Actions.SHOW_DETAILS
) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.getData(CCv2UiConstants.DataKeys.Subscription) ?: return
        val build = e.getData(CCv2UiConstants.DataKeys.Build) ?: return

        CCv2ViewUtil.showBuildDetailsTab(project, subscription, build)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return
    }

}