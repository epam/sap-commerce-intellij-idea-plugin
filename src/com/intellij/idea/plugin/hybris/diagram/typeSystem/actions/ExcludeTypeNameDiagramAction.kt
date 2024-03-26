/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.diagram.typeSystem.actions

import com.intellij.diagram.DiagramAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSDiagramNode
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent

class ExcludeTypeNameDiagramAction : DiagramAction(
    message("hybris.diagram.ts.provider.actions.exclude_type_name"),
    null,
    HybrisIcons.ACTION_REMOVE
) {
    override fun perform(event: AnActionEvent) {
        val project = event.project ?: return

        val excludedTypeNames = getSelectedNodesExceptNotes(event)
            .filterIsInstance<TSDiagramNode>()
            .map { it.graphNode.name }

        if (excludedTypeNames.isNotEmpty()) {
            DeveloperSettingsComponent.getInstance(project).state
                .typeSystemDiagramSettings
                .excludedTypeNames
                .addAll(excludedTypeNames)

            ActionManager.getInstance().getAction("Diagram.RefreshDataModelManually").actionPerformed(event)
        }
    }

    override fun getActionName() = message("hybris.diagram.ts.provider.actions.exclude_type_name")
}
