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

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab

class CCv2DeleteBuildAction : CCv2Action(
    tab = CCv2Tab.BUILDS,
    text = "Delete Build",
    icon = HybrisIcons.CCv2.Build.Actions.DELETE
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.getData(CCv2UiConstants.DataKeys.Subscription) ?: return
        val build = e.getData(CCv2UiConstants.DataKeys.Build) ?: return

        if (Messages.showYesNoDialog(
                project,
                """
                    Are you certain that you want to delete build '${build.code}' within the '${subscription.presentableName}' subscription?<br>
                    The build will be deleted permanently in 14 day(s).<br>
                    During this period you can request a restore via ticket to your system administrator.
                """.trimIndent(),
                "Delete CCv2 Build",
                HybrisIcons.CCv2.Build.Actions.DELETE
            ) != Messages.YES
        ) return

        CCv2Service.getInstance(project).deleteBuild(project, subscription, build)
    }

    override fun isEnabled(e: AnActionEvent) = super.isEnabled(e)
        && (e.getData(CCv2UiConstants.DataKeys.Build)?.canDelete() ?: false)
}