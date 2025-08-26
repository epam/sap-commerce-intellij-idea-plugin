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

package sap.commerce.toolset.hac.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.ui.HacConnectionSettingsDialog
import java.awt.Component
import java.awt.event.InputEvent

class EditActiveHacConnectionAction : HacConnectionAction("Edit active connection", HybrisIcons.Connection.EDIT) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val inputEvent: InputEvent? = e.inputEvent
        val eventSource = inputEvent?.source
        val component = (eventSource as? Component)
            ?: return

        val execService = HacExecConnectionService.getInstance(project)
        val mutableSettings = execService.activeConnection.mutable()
        if (HacConnectionSettingsDialog(project, component, mutableSettings).showAndGet()) {
            execService.save(mutableSettings.immutable())
        }
    }
}