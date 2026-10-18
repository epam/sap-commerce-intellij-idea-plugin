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
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.selectedNodes
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode

class CxApplyPropertyTemplateAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val templates = templateNodes(e).takeIf { it.isNotEmpty() } ?: return@ifNotFromSearchPopup
        // A later template wins for a key several of them set, the same as applying them one after another.
        val properties = templates.flatMap { it.properties }.associateBy { it.key }.values.toList()
        val server = HacExecConnectionService.getInstance(project).activeConnection

        val confirmed = MessageDialogBuilder
            .yesNo(
                if (templates.size == 1) "Apply Template" else "Apply Templates",
                "Apply ${properties.size} propert${if (properties.size == 1) "y" else "ies"} to '${server.shortenConnectionName}'?",
            )
            .ask(project)
        if (!confirmed) return@ifNotFromSearchPopup

        CxRemotePropertyStateService.getInstance(project).applyProperties(properties)
    }

    override fun update(e: AnActionEvent) {
        val count = templateNodes(e).size
        e.presentation.isEnabledAndVisible = e.isHybrisProject && count > 0
        e.presentation.text = if (count == 1) "Apply Template" else "Apply Templates"
        e.presentation.icon = HybrisIcons.Log.Template.EXECUTE
    }

    private fun templateNodes(e: AnActionEvent) = e.selectedNodes()
        ?.mapNotNull { it.asSafely<CxCustomPropertyTemplateItemNode>() }
        .orEmpty()
}
