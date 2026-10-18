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
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.properties.cxPropertyViewSettings
import sap.commerce.toolset.properties.settings.state.CxPropertySourceMode

/**
 * How the Source Code branch of the tree is arranged — the same one-button popup of mutually exclusive modes the
 * Commit tool window uses for its grouping.
 */
class CxPropertySourceModeActionGroup : DefaultActionGroup(), DumbAware {

    init {
        isPopup = true
        templatePresentation.text = "Group Source Code By"
        templatePresentation.description = "Choose how the Source Code properties are arranged"
        templatePresentation.icon = HybrisIcons.Property.SOURCE_MODE

        CxPropertySourceMode.entries.forEach { add(CxPropertySourceModeToggleAction(it)) }
    }
}

private class CxPropertySourceModeToggleAction(private val sourceMode: CxPropertySourceMode) : ToggleAction(
    sourceMode.title,
    sourceMode.description,
    null
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabledAndVisible = e.isHybrisProject
    }

    override fun isSelected(e: AnActionEvent) = e.cxPropertyViewSettings?.sourceMode == sourceMode

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (!state) return

        e.cxPropertyViewSettings?.sourceMode = sourceMode
    }
}
