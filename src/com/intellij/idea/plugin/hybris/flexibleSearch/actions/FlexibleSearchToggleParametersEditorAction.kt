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
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.intellij.util.asSafely

class FlexibleSearchToggleParametersEditorAction : ToggleAction(
    message("hybris.fxs.actions.show_parameters"),
    message("hybris.fxs.actions.show_parameters.description"),
    HybrisIcons.FlexibleSearch.TOGGLE_PARAMETERS_EDITOR
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val state = e.getData(PlatformDataKeys.FILE_EDITOR)
            ?.asSafely<FlexibleSearchSplitEditor>()
            ?.isParametersPanelVisible() ?: false

        with(e.presentation) {
            if (state) {
                text = message("hybris.fxs.actions.hide_parameters")
                description = message("hybris.fxs.actions.hide_parameters.description")
            } else {
                text = message("hybris.fxs.actions.show_parameters")
                description = message("hybris.fxs.actions.show_parameters.description")
            }
        }

        return state
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.getData(PlatformDataKeys.FILE_EDITOR)
            ?.asSafely<FlexibleSearchSplitEditor>()
            ?.apply {
                if (state) showParametersPanel()
                else hideParametersPanel()
            }
    }
}