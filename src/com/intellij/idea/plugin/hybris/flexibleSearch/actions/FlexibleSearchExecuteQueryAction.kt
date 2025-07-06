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
package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.flexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.idea.plugin.hybris.tools.remote.HybrisRemoteExecutionService
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.AnimatedIcon

class FlexibleSearchExecuteQueryAction : AbstractExecuteAction(
    FlexibleSearchFileType.defaultExtension,
    HybrisConstants.CONSOLE_TITLE_FLEXIBLE_SEARCH,
    message("hybris.fxs.actions.execute_query"),
    message("hybris.fxs.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {

    companion object {
        private val KEY_QUERY_EXECUTING = Key.create<Boolean>("fxs.query.execution.state")
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val queryExecuting = e.flexibleSearchSplitEditor()
            ?.getUserData(KEY_QUERY_EXECUTING)
            ?: false

        e.presentation.isEnabledAndVisible = e.presentation.isEnabledAndVisible && Plugin.GRID.isActive()
        e.presentation.isEnabled = e.presentation.isEnabledAndVisible && !queryExecuting
        e.presentation.disabledIcon = if (queryExecuting) AnimatedIcon.Default.INSTANCE
        else HybrisIcons.Console.Actions.EXECUTE
    }

    override fun processContent(e: AnActionEvent, content: String, editor: Editor, project: Project): String = e.flexibleSearchSplitEditor()
        ?.query
        ?: content

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = e.flexibleSearchSplitEditor()
        if (fileEditor?.inEditorResults ?: false) {
            HybrisConsoleService.getInstance(project).findConsole(consoleName)
                ?.let { console ->
                    fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
                    fileEditor.beforeExecution()

                    project.service<HybrisRemoteExecutionService>()
                        .execute(console, content)
                        {
                            fileEditor.renderExecutionResult(it)
                            fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

                            this.update(e)
                        }
                }
                ?: super.actionPerformed(e, project, content)
        } else {
            super.actionPerformed(e, project, content)
        }
    }
}
