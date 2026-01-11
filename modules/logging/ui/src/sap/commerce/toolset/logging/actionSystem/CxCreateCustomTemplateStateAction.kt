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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.AnimatedIcon
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.custom.CxCustomLogTemplateService
import sap.commerce.toolset.logging.custom.settings.CxCustomLogTemplatesSettings
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.logging.selectedNodes
import sap.commerce.toolset.logging.ui.CxCustomLogTemplateDialog
import sap.commerce.toolset.logging.ui.LogTemplateDialogContext
import sap.commerce.toolset.logging.ui.tree.nodes.*
import kotlin.reflect.KClass

class CxCreateCustomTemplateStateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val selectedNodes = e.selectedNodes() ?: return@ifNotFromSearchPopup

        if (selectedNodes.groupBy { it.javaClass }.keys.size > 1) {
            Messages.showErrorDialog("A custom template cannot be created from the current selection.", "Incompatible Nodes Selected")
            return@ifNotFromSearchPopup
        }

        val node = selectedNodes.first()
        val multiChoice = selectedNodes.size > 1
        val templateName = templateName(project, node, multiChoice) ?: return@ifNotFromSearchPopup
        val dialogTitle = getDialogTitle(node, multiChoice)
        val loggers = loggers(project, selectedNodes)
        val uniqueLoggers = loggers
            .fold(linkedMapOf<String, CxLoggerPresentation>()) { mergedLoggers, log ->
                mergedLoggers[log.name] = log
                mergedLoggers
            }
            .values
            .toList()

        val customLogTemplateService = CxCustomLogTemplateService.getInstance(project)
        val customTemplate = customLogTemplateService.createTemplateFromLoggers(templateName, uniqueLoggers).mutable()
        val dialogContext = LogTemplateDialogContext(
            project = project,
            mutable = customTemplate,
            title = dialogTitle,
            duplicatedSourceTemplates = loggers.size != uniqueLoggers.size,
            showRemoveSourceTemplates = node is CxCustomLogTemplateItemNode
        )
        val dialog = when (node) {
            is CxRemoteLogStateNode -> CxCustomLogTemplateDialog(dialogContext)
            is CxBundledLogTemplateItemNode -> CxCustomLogTemplateDialog(dialogContext)
            is CxCustomLogTemplateItemNode -> CxCustomLogTemplateDialog(dialogContext)
            else -> null
        } ?: return@ifNotFromSearchPopup

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

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val selectedNodes = e.selectedNodes() ?: return@ifNotFromSearchPopup
        val project = e.project ?: return@ifNotFromSearchPopup
        val selectedNodeType = selectedNodeType(project, selectedNodes)

        e.presentation.isVisible = selectedNodeType != null

        if (selectedNodeType == null) return@ifNotFromSearchPopup

        val multiChoice = selectedNodes.size > 1

        e.presentation.icon = HybrisIcons.Log.Action.SAVE_AS_TEMPLATE
        e.presentation.text = actionLabel(selectedNodeType, multiChoice)
        e.presentation.disabledIcon = disabledIcon(project, selectedNodeType)
    }

    private fun getDialogTitle(selectedNode: CxLoggersNode, multiChoice: Boolean) = when (selectedNode) {
        is CxRemoteLogStateNode -> "Create a Log Template"
        is CxBundledLogTemplateItemNode, is CxCustomLogTemplateItemNode -> if (multiChoice) "Merge Templates" else "Clone Template"
        else -> "Create a Log Template"
    }

    private fun loggers(project: Project, selectedNodes: Collection<CxLoggersNode>) = selectedNodes
        .mapNotNull {
            when (it) {
                is CxRemoteLogStateNode -> CxRemoteLogStateService.getInstance(project).state(it.connection.uuid).get()?.values
                is CxBundledLogTemplateItemNode -> it.loggers
                is CxCustomLogTemplateItemNode -> it.loggers
                else -> null
            }
        }
        .flatten()

    private fun templateName(project: Project, node: CxLoggersNode, multiChoice: Boolean) = when (node) {
        is CxRemoteLogStateNode -> templateNameFromConnection(project, node.connection.shortenConnectionName)
        is CxBundledLogTemplateItemNode, is CxCustomLogTemplateItemNode -> if (multiChoice) "Merged templates" else "Copy of '${node.name}'"
        is CxCustomLogTemplateGroupNode -> ""
        else -> null
    }

    private fun templateNameFromConnection(project: Project, connectionName: String): String {
        val templateName = "Remote '$connectionName' | template"
        val count = CxCustomLogTemplatesSettings.getInstance(project).templates.count { it.name.startsWith(templateName) }

        return if (count >= 1) "$templateName ($count)"
        else templateName
    }

    private fun selectedNodeType(project: Project, selectedNodes: List<CxLoggersNode>) = selectedNodes
        //if multiple remote nodes selected => disable the action
        .takeUnless { nodes -> nodes.count { it is CxRemoteLogStateNode } > 1 }
        //if incompatible nodes selected => disable the action
        ?.takeUnless { nodes -> nodes.groupBy { it.javaClass }.keys.size > 1 }
        ?.firstOrNull()
        ?.takeIf { node ->
            when (node) {
                is CxRemoteLogStateNode -> CxRemoteLogStateService.getInstance(project)
                    .takeIf { state -> state.ready && state.stateInitialized }
                    ?.state(node.connection.uuid)
                    ?.get()
                    ?.isNotEmpty()
                    ?: false

                is CxBundledLogTemplateItemNode,
                is CxCustomLogTemplateItemNode -> true

                else -> false
            }
        }
        ?.let { it::class }

    private fun disabledIcon(project: Project, nodeType: KClass<out CxLoggersNode>) = when (nodeType) {
        //if remote state is not fetched ==> disabled icon
        CxRemoteLogStateNode::class -> if (CxRemoteLogStateService.getInstance(project).ready) null else AnimatedIcon.Default.INSTANCE
        else -> null
    }

    private fun actionLabel(nodeType: KClass<out CxLoggersNode>, multiChoice: Boolean) = when (nodeType) {
        CxRemoteLogStateNode::class -> if (multiChoice) "Merge States & Save as Template" else "Save as Template"
        CxBundledLogTemplateItemNode::class, CxCustomLogTemplateItemNode::class -> if (multiChoice) "Merge Templates" else "Clone Template"
        CxCustomLogTemplateGroupNode::class -> "Create a Log Template"
        else -> null
    }
}
