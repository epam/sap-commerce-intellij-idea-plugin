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
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.properties.selectedNodes
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode

class CxApplyPropertyTemplateAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val project = e.project ?: return@ifNotFromSearchPopup
        val selectedNodes = e.selectedNodes()
            ?.mapNotNull { it as? CxCustomPropertyTemplateItemNode }
            ?: return@ifNotFromSearchPopup

        val merged = linkedMapOf<String, CxPropertyPresentation>()
        selectedNodes.flatMap { it.properties }.forEach { merged[it.key] = it }
        CxRemotePropertyStateService.getInstance(project).applyProperties(merged.values.toList())
    }

    override fun update(e: AnActionEvent) {
        val count = e.selectedNodes()?.size ?: return
        e.presentation.text = if (count == 1) "Apply Template" else "Apply Templates"
        e.presentation.icon = HybrisIcons.Log.Template.EXECUTE
    }
}
