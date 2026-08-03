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
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.properties.custom.CxCustomPropertyTemplateService
import sap.commerce.toolset.properties.selectedNode
import sap.commerce.toolset.properties.selectedNodes
import sap.commerce.toolset.properties.ui.CxCustomPropertyTemplateDialog
import sap.commerce.toolset.properties.ui.PropertyTemplateDialogContext
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode

class CxEditCustomPropertyTemplateAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val item = e.selectedNode()?.asSafely<CxCustomPropertyTemplateItemNode>() ?: return@ifNotFromSearchPopup
        val mutable = CxCustomPropertyTemplateService.getInstance(project).findTemplate(item.uuid)?.mutable() ?: return@ifNotFromSearchPopup

        val context = PropertyTemplateDialogContext(project, mutable, "Update a Property Template")
        if (CxCustomPropertyTemplateDialog(context).showAndGet()) {
            CxCustomPropertyTemplateService.getInstance(project).updateTemplate(mutable.immutable())
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = "Edit Template"
        e.presentation.icon = HybrisIcons.Log.Template.EDIT_CUSTOM_TEMPLATE
        e.presentation.isEnabled = e.selectedNodes()?.size == 1
    }
}
