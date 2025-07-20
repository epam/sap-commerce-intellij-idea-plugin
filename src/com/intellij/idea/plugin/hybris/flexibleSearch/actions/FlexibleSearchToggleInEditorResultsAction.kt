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
package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.flexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class FlexibleSearchToggleInEditorResultsAction : ToggleAction(
    message("hybris.actions.in_editor_results"),
    message("hybris.actions.in_editor_results.description"),
    HybrisIcons.Actions.TOGGLE_IN_EDITOR_RESULTS
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = e.flexibleSearchSplitEditor()?.inEditorResults
        ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.flexibleSearchSplitEditor()?.inEditorResults = state
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = e.presentation.isVisible && Plugin.GRID.isActive()
    }
}
