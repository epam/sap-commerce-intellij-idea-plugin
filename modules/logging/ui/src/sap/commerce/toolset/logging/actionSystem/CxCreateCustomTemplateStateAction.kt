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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.AnimatedIcon
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.custom.CxCustomLogTemplateService
import sap.commerce.toolset.logging.custom.settings.CxCustomLogTemplatesSettings
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.logging.selectedNodes
import sap.commerce.toolset.logging.ui.CxAdvancedCustomLogTemplateDialog
import sap.commerce.toolset.logging.ui.CxCustomLogTemplateDialog
import sap.commerce.toolset.logging.ui.LogTemplateDialogContext
import sap.commerce.toolset.logging.ui.tree.nodes.CxBundledLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxRemoteLogStateNode

class CxCreateCustomTemplateStateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return

        val customLogTemplateService = CxCustomLogTemplateService.getInstance(project)

        val selectedNodes = e.selectedNodes() ?: return
        if (selectedNodes.groupBy { it.javaClass }.keys.size > 1) {
            Messages.showErrorDialog("A custom template cannot be created from the current selection.", "Incompatible Nodes Selected")
            return
        }

        val dialogTitle = getDialogTitle(selectedNodes)
        val templateName = templateName(project, selectedNodes)
        val loggers = loggers(project, selectedNodes)
        val uniqueLoggers = loggers
            .fold(linkedMapOf<String, CxLoggerPresentation>()) { mergedLoggers, log ->
                mergedLoggers[log.name] = log
                mergedLoggers
            }
            .values
            .toList()

        val customTemplate = customLogTemplateService.createTemplateFromLoggers(templateName, uniqueLoggers).mutable()
        val dialogContext = LogTemplateDialogContext(
            project = project,
            mutable = customTemplate,
            title = dialogTitle,
            duplicatedSourceTemplates = loggers.size != uniqueLoggers.size,
        )
        val dialog = when (selectedNodes.first()) {
            is CxRemoteLogStateNode -> CxCustomLogTemplateDialog(dialogContext)
            is CxBundledLogTemplateItemNode -> CxCustomLogTemplateDialog(dialogContext)
            is CxCustomLogTemplateItemNode -> CxAdvancedCustomLogTemplateDialog(dialogContext)
            else -> null
        } ?: return

        if (dialog.showAndGet()) {
            customLogTemplateService.addTemplate(customTemplate.immutable())

            if (dialogContext.removeSourceTemplates.get()) {
                val customTemplateUids = selectedNodes
                    .mapNotNull { it.asSafely<CxCustomLogTemplateItemNode>() }
                    .map { it.uuid }
                customLogTemplateService.deleteTemplates(customTemplateUids)
            }
        }
    }

    private fun getDialogTitle(selectedNodes: List<Any>) = when (selectedNodes.first()) {
        is CxRemoteLogStateNode -> "Create a Log Template"
        is CxBundledLogTemplateItemNode -> if (selectedNodes.size > 1) "Merge Templates" else "Clone Template"
        is CxCustomLogTemplateItemNode -> if (selectedNodes.size > 1) "Merge Templates" else "Clone Template"
        else -> "Create a Log Template"
    }

    private fun templateNameFromConnection(project: Project, connectionName: String): String {
        val templateName = "Remote '$connectionName' | template"
        val count = CxCustomLogTemplatesSettings.getInstance(project).templates.count { it.name.startsWith(templateName) }

        return if (count >= 1) "$templateName ($count)"
        else templateName
    }

    private fun loggers(project: Project, selectedNodes: List<Any>) = selectedNodes
        .mapNotNull {
            when (it) {
                is CxRemoteLogStateNode -> CxRemoteLogStateService.getInstance(project).state(it.connection.uuid).get()?.values ?: emptyList()
                is CxBundledLogTemplateItemNode -> it.loggers
                is CxCustomLogTemplateItemNode -> it.loggers
                else -> null
            }
        }
        .flatten()

    private fun templateName(project: Project, selectedNodes: List<Any>) = when (val firstNode = selectedNodes.first()) {
        is CxRemoteLogStateNode -> templateNameFromConnection(project, firstNode.connection.shortenConnectionName)
        is CxBundledLogTemplateItemNode -> if (selectedNodes.size > 1) "Merged templates" else "Copy of '${firstNode.name}'"
        is CxCustomLogTemplateItemNode -> if (selectedNodes.size > 1) "Merged templates" else "Copy of '${firstNode.name}'"
        else -> ""
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        val selectedNodes = e.selectedNodes() ?: return
        val project = e.project ?: return

        e.presentation.isEnabled = isActionEnabled(project, selectedNodes)

        if (!e.presentation.isVisible) return

        e.presentation.text = actionLabel(selectedNodes)
        e.presentation.icon = HybrisIcons.Log.Action.SAVE_AS_TEMPLATE
        e.presentation.disabledIcon = disabledIcon(project, selectedNodes)
    }

    private fun isActionEnabled(project: Project, selectedNodes: List<Any>): Boolean {
        //if incompatible nodes selected => disable the action
        if (selectedNodes.groupBy { it.javaClass }.keys.size > 1) return false

        val loggerAccess = CxRemoteLogStateService.getInstance(project)

        return when (val firstNode = selectedNodes.first()) {
            //if remote state is fetched ==> enable the action
            is CxRemoteLogStateNode -> loggerAccess.ready
                && loggerAccess.stateInitialized
                && loggerAccess.state(firstNode.connection.uuid).get()?.isNotEmpty() ?: false

            is CxBundledLogTemplateItemNode -> true
            is CxCustomLogTemplateItemNode -> true
            else -> false
        }
    }

    private fun disabledIcon(project: Project, selectedNodes: List<Any>) = when (selectedNodes.first()) {
        //if remote state is not fetched ==> disabled icon
        is CxRemoteLogStateNode -> if (CxRemoteLogStateService.getInstance(project).ready) null else AnimatedIcon.Default.INSTANCE
        else -> null
    }

    private fun actionLabel(selectedNodes: List<Any>) = when (selectedNodes.first()) {
        is CxRemoteLogStateNode -> if (selectedNodes.size > 1) "Merge States & Save as Template" else "Save as Template"
        is CxBundledLogTemplateItemNode -> if (selectedNodes.size > 1) "Merge Templates" else "Clone Template"
        is CxCustomLogTemplateItemNode -> if (selectedNodes.size > 1) "Merge Templates" else "Clone Template"
        else -> "Create a Log Template"
    }
}
