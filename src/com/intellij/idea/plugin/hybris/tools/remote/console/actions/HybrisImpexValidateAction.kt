/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.HybrisConsoleExecuteValidateActionHandler
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class HybrisImpexValidateAction(
    private val executeValidationActionHandler: HybrisConsoleExecuteValidateActionHandler
) : AnAction(
    message("action.console.hybris.impex.validate.message.text"),
    message("action.console.hybris.impex.validate.message.title"),
    HybrisIcons.IMX_VALIDATE
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        executeValidationActionHandler.runExecuteAction()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val activeConsole = HybrisConsoleService.getInstance(project).getActiveConsole() ?: return

        val editor = activeConsole.consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isEnabled = !executeValidationActionHandler.isProcessRunning &&
            (lookup == null || !lookup.isCompletion) && activeConsole is HybrisImpexConsole

        e.presentation.isVisible = activeConsole is HybrisImpexConsole
    }

}