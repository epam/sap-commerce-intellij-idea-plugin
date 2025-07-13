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

package com.intellij.idea.plugin.hybris.actions

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.util.asSafely
import javax.swing.Icon

abstract class AbstractExecuteAction(
    internal val language: Language,
    internal val consoleName: String,
    internal val name: String,
    internal val description: String,
    internal val icon: Icon
) : AnAction(name, description, icon), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected open fun doExecute(e: AnActionEvent, consoleService: HybrisConsoleService) {
        consoleService.executeStatement(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext) ?: return
        val project = e.project ?: return
        val content = getExecutableContent(editor, e, project)

        actionPerformed(e, project, content)
    }

    open fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        with(HybrisToolWindowService.getInstance(project)) {
            activateToolWindow()
            activateToolWindowTab(HybrisToolWindowFactory.CONSOLES_ID)
        }

        val consoleService = HybrisConsoleService.getInstance(project)
        val console = consoleService.findConsole(consoleName)
        if (console == null) {
            LOG.warn("unable to find console $consoleName")
            return
        }

        e.dataContext.asSafely<UserDataHolder>()
            ?.putUserData(HybrisConstants.KEY_REMOTE_EXECUTION_CONTENT, content)

        consoleService.setActiveConsole(console)
        console.setInputText(content)

        invokeLater {
            doExecute(e, consoleService)
        }
    }

    open fun processContent(e: AnActionEvent, content: String, editor: Editor, project: Project) = content

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = this.language == e.dataContext.getData(CommonDataKeys.LANGUAGE)
    }

    private fun getExecutableContent(
        editor: Editor,
        e: AnActionEvent,
        project: Project
    ): String {
        val selectionModel = editor.selectionModel
        var content = selectionModel.selectedText
        if (content == null || content.trim { it <= ' ' }.isEmpty()) {
            content = editor.document.text
        }

        return processContent(e, content, editor, project)
    }

    companion object {
        private val LOG = Logger.getInstance(AbstractExecuteAction::class.java)
    }
}
