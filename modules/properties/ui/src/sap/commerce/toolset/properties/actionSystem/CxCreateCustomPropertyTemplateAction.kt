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

package sap.commerce.toolset.properties.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.custom.CxCustomPropertyTemplateService
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.selectedNode
import sap.commerce.toolset.properties.ui.CxCustomPropertyTemplateDialog
import sap.commerce.toolset.properties.ui.PropertyTemplateDialogContext
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode

class CxCreateCustomPropertyTemplateAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup

        when (val selectedNode = e.selectedNode()) {
            // The accumulated state holds only the pages scrolled into view, so the instance is loaded in full first.
            is CxRemotePropertyStateNode -> CxRemotePropertyStateService.getInstance(project)
                .fetchAll(selectedNode.connection) { page ->
                    invokeLater { createTemplate(project, selectedNode, page.properties) }
                }

            is CxCustomPropertyTemplateItemNode -> createTemplate(project, selectedNode, selectedNode.properties)

            else -> Unit
        }
    }

    private fun createTemplate(project: Project, selectedNode: Any, properties: Collection<CxPropertyPresentation>) {
        if (properties.isEmpty()) return

        val templateService = CxCustomPropertyTemplateService.getInstance(project)
        val mutable = templateService
            .createTemplateFromProperties(templateName(selectedNode), properties)
            .mutable()

        val context = PropertyTemplateDialogContext(
            project = project,
            mutable = mutable,
            title = if (selectedNode is CxRemotePropertyStateNode) "Create a Property Template" else "Clone Template",
            showRemoveSourceTemplates = selectedNode is CxCustomPropertyTemplateItemNode,
            selectableProperties = properties.sortedBy { it.key },
        )
        val dialog = CxCustomPropertyTemplateDialog(context)

        if (dialog.showAndGet()) {
            dialog.applySelection()

            templateService.addTemplate(mutable.immutable())
            if (context.removeSourceTemplates.get() && selectedNode is CxCustomPropertyTemplateItemNode) {
                templateService.deleteTemplates(listOf(selectedNode.uuid))
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()
        e.presentation.isEnabledAndVisible = e.isHybrisProject
            && (selectedNode is CxRemotePropertyStateNode || selectedNode is CxCustomPropertyTemplateItemNode)
        e.presentation.text = if (selectedNode is CxCustomPropertyTemplateItemNode) "Clone Template" else "Save as Template"
        e.presentation.icon = HybrisIcons.Log.Action.SAVE_AS_TEMPLATE
    }

    private fun templateName(node: Any): String = when (node) {
        is CxRemotePropertyStateNode -> "Remote '${node.connection.shortenConnectionName}' | template"
        is CxCustomPropertyTemplateItemNode -> "Copy of '${node.name}'"
        else -> ""
    }
}
