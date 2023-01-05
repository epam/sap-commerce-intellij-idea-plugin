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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.view

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ShowOnlyCustomAction(val settings: BSViewSettings) : ToggleAction(
    message("hybris.toolwindow.action.only_custom.text"),
    message("hybris.toolwindow.bs.action.only_custom.description"),
    null
) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowOnlyCustom()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowOnlyCustom(state)
        settings.fireSettingsChanged(BSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowOnlyDeprecatedAction(val settings: BSViewSettings) : ToggleAction(
    message("hybris.toolwindow.bs.action.only_deprecated.text"),
    message("hybris.toolwindow.bs.action.only_deprecated.description"),
    null
) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowOnlyDeprecated()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowOnlyDeprecated(state)
        settings.fireSettingsChanged(BSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaEnumValuesAction(val settings: BSViewSettings) : ToggleAction(
    message("hybris.toolwindow.bs.action.enum.values.text"), null, null
) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowEnumValues()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowEnumValues(state)
        settings.fireSettingsChanged(BSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ShowMetaBeanPropertiesAction(val settings: BSViewSettings) : ToggleAction(
    message("hybris.toolwindow.bs.action.bean.properties.text"), null, null
) {

    override fun isSelected(e: AnActionEvent): Boolean = settings.isShowBeanProperties()

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.setShowBeanProperties(state)
        settings.fireSettingsChanged(BSViewSettings.ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

