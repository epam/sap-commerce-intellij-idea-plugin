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
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.custom.CxCustomPropertyTemplateService
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.selectedNode
import sap.commerce.toolset.properties.ui.CxCustomPropertyTemplateDialog
import sap.commerce.toolset.properties.ui.PropertyTemplateDialogContext
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode

class CxCreateCustomPropertyTemplateAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val selectedNode = e.selectedNode() ?: return@ifNotFromSearchPopup
        val properties = sourceProperties(project, selectedNode) ?: return@ifNotFromSearchPopup
        val mutable = CxCustomPropertyTemplateService.getInstance(project)
            .createTemplateFromProperties(templateName(selectedNode), properties)
            .mutable()

        val context = PropertyTemplateDialogContext(
            project = project,
            mutable = mutable,
            title = if (selectedNode is CxRemotePropertyStateNode) "Create a Property Template" else "Clone Template",
            showRemoveSourceTemplates = selectedNode is CxCustomPropertyTemplateItemNode,
        )

        if (CxCustomPropertyTemplateDialog(context).showAndGet()) {
            CxCustomPropertyTemplateService.getInstance(project).addTemplate(mutable.immutable())
            if (context.removeSourceTemplates.get() && selectedNode is CxCustomPropertyTemplateItemNode) {
                CxCustomPropertyTemplateService.getInstance(project).deleteTemplates(listOf(selectedNode.uuid))
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()
        e.presentation.isEnabledAndVisible = selectedNode is CxRemotePropertyStateNode || selectedNode is CxCustomPropertyTemplateItemNode
        e.presentation.text = if (selectedNode is CxCustomPropertyTemplateItemNode) "Clone Template" else "Save as Template"
        e.presentation.icon = HybrisIcons.Log.Action.SAVE_AS_TEMPLATE
    }

    private fun sourceProperties(project: com.intellij.openapi.project.Project, node: Any): Collection<CxPropertyPresentation>? = when (node) {
        is CxRemotePropertyStateNode -> CxRemotePropertyStateService.getInstance(project).state(node.connection.uuid).get()?.properties?.values
        is CxCustomPropertyTemplateItemNode -> node.properties
        else -> null
    }

    private fun templateName(node: Any): String = when (node) {
        is CxRemotePropertyStateNode -> "Remote '${node.connection.shortenConnectionName}' | template"
        is CxCustomPropertyTemplateItemNode -> "Copy of '${node.name}'"
        else -> ""
    }
}
