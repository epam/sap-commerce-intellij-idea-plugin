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
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.util.asSafely

class FlexibleSearchToggleParametersPanelAction : AnAction(
    message("hybris.fxs.actions.show_parameters"),
    message("hybris.fxs.actions.show_parameters.description"),
    HybrisIcons.FlexibleSearch.SHOW_PARAMETERS_PANEL
), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val visible = e.getData(PlatformDataKeys.FILE_EDITOR)
            ?.asSafely<FlexibleSearchSplitEditor>()
            ?.isParametersPanelVisible() ?: return

        if (visible) {
            e.presentation.text = message("hybris.fxs.actions.hide_parameters")
            e.presentation.description = message("hybris.fxs.actions.hide_parameters.description")
            e.presentation.icon = HybrisIcons.FlexibleSearch.HIDE_PARAMETERS_PANEL
        } else {
            e.presentation.text = message("hybris.fxs.actions.show_parameters")
            e.presentation.description = message("hybris.fxs.actions.show_parameters.description")
            e.presentation.icon = HybrisIcons.FlexibleSearch.SHOW_PARAMETERS_PANEL
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(PlatformDataKeys.FILE_EDITOR)
            ?.asSafely<FlexibleSearchSplitEditor>()
            ?.toggleLayout()
    }
}