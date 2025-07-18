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
package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaSelectionMode
import com.intellij.idea.plugin.hybris.ui.ActionButtonWithTextAndDescription
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil

class GroovyExecutionModeActionGroup : DefaultActionGroup() {

    init {
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
        templatePresentation.putClientProperty(ActionUtil.COMPONENT_PROVIDER, ActionButtonWithTextAndDescription(this))
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val connectionContext = GroovyExecutionClient.getInstance(project).connectionContext
        val text = when (connectionContext.replicaSelectionMode) {
            ReplicaSelectionMode.AUTO -> "Auto-Discover Replica"
            else -> "Execute on ${connectionContext.replicaContexts.size} replica(s)"
        }

        e.presentation.icon = connectionContext.replicaSelectionMode.icon
        e.presentation.text = text
        e.presentation.description = connectionContext.description
    }
}
