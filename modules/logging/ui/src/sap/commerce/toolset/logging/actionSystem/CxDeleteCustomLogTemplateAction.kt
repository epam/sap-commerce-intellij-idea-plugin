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
import com.intellij.openapi.ui.Messages
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.logging.custom.CxCustomLogTemplateService
import sap.commerce.toolset.logging.selectedNodes
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateItemNode

class CxDeleteCustomLogTemplateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val selectedNodes = customLogTemplateItemNodes(e) ?: return@ifNotFromSearchPopup
        val message = if (selectedNodes.size == 1) "Delete template \"${selectedNodes.first().name}\"?"
        else "Delete ${selectedNodes.size} templates?"

        if (Messages.showYesNoDialog(
                project,
                message,
                "Confirm Deletion",
                HybrisIcons.Log.Action.DELETE
            ) != Messages.YES
        ) return@ifNotFromSearchPopup

        val templateIds = selectedNodes.map { it.uuid }

        CxCustomLogTemplateService.getInstance(project).deleteTemplates(templateIds)
    }

    override fun update(e: AnActionEvent) {
        val selectedNodes = customLogTemplateItemNodes(e) ?: return

        e.presentation.text = if (selectedNodes.size == 1) "Delete Template" else "Delete Templates"
        e.presentation.icon = HybrisIcons.Log.Template.DELETE_CUSTOM_TEMPLATE
    }

    private fun customLogTemplateItemNodes(e: AnActionEvent): List<CxCustomLogTemplateItemNode>? = e.selectedNodes()
        ?.mapNotNull { it.asSafely<CxCustomLogTemplateItemNode>() }
}
