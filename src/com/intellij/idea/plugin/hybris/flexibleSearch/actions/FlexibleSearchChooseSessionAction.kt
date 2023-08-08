/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class FlexibleSearchChooseSessionAction : ActionGroup() {

    init {
        isPopup = true
        templatePresentation.isPerformGroup = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun displayTextInToolbar() = true
    override fun getChildren(e: AnActionEvent?) = emptyArray<AnAction>()

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val presentation = e.presentation

        val devSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project)
        val hacSettings = devSettings.getActiveHacRemoteConnectionSettings(project)
        presentation.text = "Active connection: $hacSettings"
        presentation.isEnabled = false
        presentation.icon = HybrisIcons.HYBRIS_REMOTE
    }

}
