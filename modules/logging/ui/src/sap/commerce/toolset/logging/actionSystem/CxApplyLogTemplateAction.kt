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

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.logging.selectedNode
import sap.commerce.toolset.logging.selectedNodes
import sap.commerce.toolset.logging.ui.tree.nodes.CxBundledLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateItemNode

class CxApplyLogTemplateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val selectedNodes = e.selectedNodes() ?: return
        val selectedNode = e.selectedNode() ?: return

        var loggers = if (selectedNodes.size == 1) {
            when (selectedNode) {
                is CxBundledLogTemplateItemNode -> selectedNode.loggers
                is CxCustomLogTemplateItemNode -> selectedNode.loggers
                else -> return
            }
        } else {
            selectedNodes
                .mapNotNull {
                    return@mapNotNull when (it) {
                        is CxBundledLogTemplateItemNode -> it.loggers
                        is CxCustomLogTemplateItemNode -> it.loggers
                        else -> null
                    }
                }
                .flatten()
        }

        val hasDuplicates = selectedNodes.size > 1 && loggers.groupBy { it.name }.filter { it.value.size > 1 }.isNotEmpty()

        if (hasDuplicates) {
            if (Messages.showYesNoDialog(
                    project,
                    """
                                    Some loggers are defined more than once.
                                    For each duplicated logger, only the logger from the last template will be kept and earlier ones will be overwritten.
                                    
                                    Do you want to continue?
                                """.trimIndent(),
                    "Confirm Applying Templates",
                    HybrisIcons.Log.Template.EXECUTE
                ) != Messages.YES
            ) return

            //remove duplicates
            loggers = loggers
                .fold(linkedMapOf<String, CxLoggerPresentation>()) { acc, log ->
                    acc[log.name] = log
                    acc
                }
                .values
                .toList()
        }

        CxRemoteLogStateService.getInstance(project).setLoggers(loggers)
    }

    override fun update(e: AnActionEvent) {
        val selectedNodes = e.selectedNodes() ?: return

        e.presentation.text = if (selectedNodes.size == 1) "Apply Template" else "Apply Templates"
        e.presentation.icon = HybrisIcons.Log.Template.EXECUTE
    }
}