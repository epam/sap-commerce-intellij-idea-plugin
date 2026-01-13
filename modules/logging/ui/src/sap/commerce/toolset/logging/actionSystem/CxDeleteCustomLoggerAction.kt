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

package sap.commerce.toolset.logging.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.logging.CxLogUiConstants
import sap.commerce.toolset.logging.custom.CxCustomLogTemplateService

class CxDeleteCustomLoggerAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup

        val templateUUID = e.getData(CxLogUiConstants.DataKeys.TemplateUUID) ?: return@ifNotFromSearchPopup
        val loggerName = e.getData(CxLogUiConstants.DataKeys.LoggerName) ?: return@ifNotFromSearchPopup

        if (Messages.showYesNoDialog(
                project,
                "Delete logger \"${loggerName}\"?",
                "Confirm Logger Deletion",
                HybrisIcons.Log.Action.DELETE
            ) != Messages.YES
        ) return@ifNotFromSearchPopup

        CxCustomLogTemplateService.getInstance(project).deleteLogger(templateUUID, loggerName)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.icon = HybrisIcons.Log.Template.DELETE_LOGGER
    }
}