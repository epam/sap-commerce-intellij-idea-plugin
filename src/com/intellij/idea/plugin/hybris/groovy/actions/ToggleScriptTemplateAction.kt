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

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ToggleScriptTemplateAction : ToggleAction(null, null, AllIcons.Actions.Minimap) {

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val groovySettings = DeveloperSettings.getInstance(project).groovySettings.mutable()
        e.presentation.text = if (!groovySettings.enableScriptTemplate) "Enable Script Template" else "Disable Script Template"
        return groovySettings.enableScriptTemplate
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        with(DeveloperSettings.getInstance(project)) {
            groovySettings = groovySettings.copy(enableScriptTemplate = state)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

}