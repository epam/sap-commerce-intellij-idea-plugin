/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.HybrisTypeSystemViewSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ShowModulesAction(val settings: HybrisTypeSystemViewSettings) :
    ToggleAction("Show modules", null, AllIcons.Actions.GroupByModule) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowModules()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowModules(state)
    }

}