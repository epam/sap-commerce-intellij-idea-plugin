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
package com.intellij.idea.plugin.hybris.project.actions

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.toolwindow.ReplicaSelectionDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.asSafely
import java.awt.Component

class HacConfigureReplica : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val component = e.inputEvent?.source?.asSafely<Component>()
            ?: return
        ReplicaSelectionDialog(project, component).showAndGet()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val replica = HybrisHacHttpClient.getInstance(project).replica
        e.presentation.text = replica?.toString()
            ?: "Auto-discover replica"
        e.presentation.icon = replica?.let { AllIcons.Actions.Checked }
            ?: AllIcons.Actions.Lightning
        e.presentation.description = replica?.description
            ?: "If applicable, replica will be automatically applied during authentication."
    }
}
