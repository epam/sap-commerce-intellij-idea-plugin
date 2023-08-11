/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.action

import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesPanel
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware

abstract class AbstractExecuteAction : AnAction(), DumbAware {

    protected abstract val extension: String
    protected abstract val consoleName: String
    protected open fun doExecute(consolePanel: HybrisConsolesPanel) {
        consolePanel.execute()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val project = e.project ?: return

        val selectionModel = editor.selectionModel
        var content = selectionModel.selectedText
        if (content == null || content.trim { it <= ' ' }.isEmpty()) {
            content = editor.document.text
        }

        val toolWindowService = HybrisToolWindowService.getInstance(project)
        with(toolWindowService) {
            this.activateToolWindow()
            this.activateToolWindowTab(HybrisToolWindowFactory.CONSOLES_ID)
        }

        val consolesPanel = toolWindowService.consolesPanel
        val console = consolesPanel.findConsole(consoleName)
        if (console == null) {
            LOG.warn("unable to find console $consoleName")
            return
        }
        consolesPanel.setActiveConsole(console)
        consolesPanel.sendTextToConsole(console, content)
        toolWindowService.activateToolWindow()
        doExecute(consolesPanel)
    }

    override fun update(e: AnActionEvent) {
        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && file.name.endsWith(this.extension)
        e.presentation.isEnabledAndVisible = enabled
    }

    companion object {
        private val LOG = Logger.getInstance(AbstractExecuteAction::class.java)
    }
}
