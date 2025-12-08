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

package sap.commerce.toolset.beanSystem.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import sap.commerce.toolset.beanSystem.settings.state.ChangeType
import sap.commerce.toolset.i18n

class BSShowOnlyDeprecatedAction : ToggleAction(
    i18n("hybris.toolwindow.bs.action.only_deprecated.text"),
    i18n("hybris.toolwindow.bs.action.only_deprecated.description"),
    null
) {

    override fun isSelected(e: AnActionEvent): Boolean = e.bsViewSettings
        ?.showDeprecatedOnly
        ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val settings = e.bsViewSettings ?: return

        settings.showDeprecatedOnly = state
        settings.fireSettingsChanged(ChangeType.FULL)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}