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
package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.TransactionMode
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisGroovyConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.util.asSafely
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

class GroovyExecuteAction : AbstractExecuteAction(
    GroovyLanguage,
    HybrisConstants.CONSOLE_TITLE_GROOVY,
    "Execute Groovy Script",
    "Execute Groovy Script on a remote SAP Commerce instance",
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun doExecute(e: AnActionEvent, consoleService: HybrisConsoleService) {
        val project = e.project ?: return

        consoleService.getActiveConsole()
            ?.asSafely<HybrisGroovyConsole>()
            ?.also { console ->
                val commitMode = DeveloperSettingsComponent.getInstance(project).state.groovySettings.txMode == TransactionMode.COMMIT
                console.updateCommitMode(commitMode)

                val replicaContexts = HybrisHacHttpClient.getInstance(project).connectionContext.replicaContexts

                if (replicaContexts.isNotEmpty()) {
                    replicaContexts
                        .map {
                            it.content = e.dataContext.asSafely<UserDataHolder>()
                                ?.getUserData(HybrisConstants.KEY_REMOTE_EXECUTION_CONTENT)
                                ?: ""

                            SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .add(HybrisConstants.DATA_KEY_REPLICA_CONTEXT, it)
                                .build()
                        }
                        .map { AnActionEvent.createEvent(it, e.presentation, e.place, e.uiKind, e.inputEvent) }
                        .forEach { super.doExecute(it, consoleService) }
                } else {
                    super.doExecute(e, consoleService)
                }
            }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val project = e.project ?: return

        when (DeveloperSettingsComponent.getInstance(project).state.groovySettings.txMode) {
            TransactionMode.ROLLBACK -> {
                e.presentation.icon = HybrisIcons.Console.Actions.EXECUTE_ROLLBACK
                e.presentation.text = "Execute Groovy Script<br/>Commit Mode <strong><font color='#C75450'>OFF</font></strong>"
            }

            TransactionMode.COMMIT -> {
                e.presentation.icon = HybrisIcons.Console.Actions.EXECUTE
                e.presentation.text = "Execute Groovy Script<br/>Commit Mode <strong><font color='#57965C'>ON</font></strong>"
            }
        }
    }

    override fun processContent(
        e: AnActionEvent,
        content: String,
        editor: Editor,
        project: Project
    ): String {
        val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext) ?: return content

        val selectionModel = editor.selectionModel

        var processedContent = content

        if (selectionModel.hasSelection() && psiFile is GroovyFile && !psiFile.importStatements.isEmpty()) {

            val document = editor.document
            val selectionStartLine = document.getLineNumber(selectionModel.selectionStart)
            val selectionEndLine = document.getLineNumber(selectionModel.selectionEnd)

            val missingImports = psiFile.importStatements.filter { import ->
                val importLine = document.getLineNumber(import.textOffset)
                importLine < selectionStartLine || importLine > selectionEndLine
            }

            if (!missingImports.isEmpty()) {
                val importStatements = missingImports.map { it.text }
                val importBlock = importStatements.joinToString(separator = "\n")
                processedContent = "$importBlock\n\n$processedContent"
            }

        }

        processedContent = "/* ${psiFile.name} */\n$processedContent"

        return processedContent
    }
}