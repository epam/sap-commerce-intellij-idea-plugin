/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.project.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil.SHOW_TEXT_IN_TOOLBAR
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.project.ProjectRefreshService
import sap.commerce.toolset.settings.WorkspaceSettings

class ProjectRefreshAction : DumbAwareAction(
    "Refresh SAP Commerce Project",
    "Re-imports the current hybris project with default values",
    HybrisIcons.Y.LOGO_BLUE
) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            ProjectRefreshService.getInstance(project).refresh()
        } catch (ex: ConfigurationException) {
            Messages.showErrorDialog(
                project,
                ex.getMessageHtml().toString(),
                message("hybris.project.import.error.unable.to.proceed")
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return

        with(e.presentation) {
            putClientProperty(SHOW_TEXT_IN_TOOLBAR, true)
            setVisible(WorkspaceSettings.getInstance(project).hybrisProject)
        }
    }
}
