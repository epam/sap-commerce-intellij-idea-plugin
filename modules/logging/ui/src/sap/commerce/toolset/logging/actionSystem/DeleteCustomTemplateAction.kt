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
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.selectedNode
import sap.commerce.toolset.logging.template.CxLoggersTemplatesService
import sap.commerce.toolset.logging.ui.tree.nodes.CustomLoggersTemplateItemNode

class DeleteCustomTemplateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return

        val templateNode = e.selectedNode()
            ?.asSafely<CustomLoggersTemplateItemNode>()
            ?: return

        if (Messages.showYesNoDialog(
                project,
                "Delete template $ { templateNode.name }?",
                "Delete Custom Template",
                HybrisIcons.Log.Action.DELETE
            ) != Messages.YES
        ) return

        CxLoggersTemplatesService.getInstance(project).deleteCustomTemplate(templateNode.uuid)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = "Delete Template"
        e.presentation.icon = HybrisIcons.Log.Template.DELETE_CUSTOM_TEMPLATE
    }
}
