/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.welcomescreen.actionSystem

import com.intellij.ide.RecentProjectsManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.i18n
import sap.commerce.toolset.welcomescreen.WelcomeScreenConstants

/**
 * Removes a SAP Commerce project from the IDE's recent projects list.
 *
 * Reads the project from [sap.commerce.toolset.welcomescreen.WelcomeScreenConstants.DATA_KEY_SAP_COMMERCE_PROJECT] supplied via the data context
 * — typically populated by the welcome screen list when it shows a context menu
 * or invokes the action from a row's overflow button.
 */
class RemoveSapCommerceProjectAction : AnAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.getData(WelcomeScreenConstants.DATA_KEY_SAP_COMMERCE_PROJECT) != null
        e.presentation.text = i18n("hybris.welcometab.button.remove.from.recent.projects")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(WelcomeScreenConstants.DATA_KEY_SAP_COMMERCE_PROJECT) ?: return

        val confirmed = MessageDialogBuilder
            .yesNo(
                title = i18n("hybris.welcometab.remove.popup.title"),
                message = i18n("hybris.welcometab.remove.popup.body", "'${project.displayName}'")
            )
            .yesText(i18n("hybris.welcometab.remove.popup.yes"))
            .noText(Messages.getCancelButton())
            .icon(Messages.getQuestionIcon())
            .guessWindowAndAsk()

        if (!confirmed) return

        RecentProjectsManager.getInstance().removePath(project.location)
    }

    companion object {
        const val ACTION_ID = "sap.developers.toolset.removeFromRecentProjects"
    }
}

