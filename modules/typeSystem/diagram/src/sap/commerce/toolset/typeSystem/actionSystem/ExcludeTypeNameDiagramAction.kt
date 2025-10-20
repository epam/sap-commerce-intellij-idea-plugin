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
package sap.commerce.toolset.typeSystem.actionSystem

import com.intellij.diagram.DiagramAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.typeSystem.diagram.node.TSDiagramNode

class ExcludeTypeNameDiagramAction : DiagramAction(
    i18n("hybris.diagram.ts.provider.actions.exclude_type_name"),
    null,
    HybrisIcons.Actions.REMOVE
) {
    override fun perform(event: AnActionEvent) {
        val project = event.project ?: return

        val excludedTypeNames = getSelectedNodesExceptNotes(event)
            .filterIsInstance<TSDiagramNode>()
            .map { it.graphNode.name }

        if (excludedTypeNames.isNotEmpty()) {
            with(project.yDeveloperSettings) {
                val newExcludedTypeNames = typeSystemDiagramSettings.excludedTypeNames.toMutableSet()
                    .apply { addAll(excludedTypeNames) }
                typeSystemDiagramSettings = typeSystemDiagramSettings.copy(excludedTypeNames = newExcludedTypeNames)
            }

            val action = ActionManager.getInstance().getAction("Diagram.RefreshDataModelManually")
            ActionUtil.performAction(action, event)
        }
    }

    override fun getActionName() = i18n("hybris.diagram.ts.provider.actions.exclude_type_name")
}
