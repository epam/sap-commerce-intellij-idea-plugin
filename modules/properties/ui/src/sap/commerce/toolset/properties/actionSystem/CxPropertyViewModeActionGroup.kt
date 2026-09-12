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
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.cxPropertyViewSettings
import sap.commerce.toolset.properties.settings.state.CxPropertyViewMode

/**
 * View options of the Properties tool window, offered the way the Commit tool window offers its own: a single toolbar
 * button opening a popup of mutually exclusive modes.
 */
class CxPropertyViewModeActionGroup : DefaultActionGroup(), DumbAware {

    init {
        isPopup = true
        templatePresentation.text = "View Options"
        templatePresentation.description = "Choose what the properties list shows"
        templatePresentation.icon = HybrisIcons.Property.VIEW_OPTIONS

        CxPropertyViewMode.entries.forEach { add(CxPropertyViewModeToggleAction(it)) }
    }
}

private class CxPropertyViewModeToggleAction(private val viewMode: CxPropertyViewMode) : ToggleAction(
    viewMode.title,
    viewMode.description,
    null
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent) = e.cxPropertyViewSettings?.viewMode == viewMode

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (!state) return

        val settings = e.cxPropertyViewSettings ?: return
        settings.viewMode = viewMode
        settings.fireViewModeChanged(viewMode)
    }
}
