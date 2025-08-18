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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SettingsState
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.toolwindow.CCv2View
import javax.swing.Icon

abstract class CCv2ShowWithStatusAction<T : Enum<T>>(
    private val tab: CCv2Tab,
    protected val status: T,
    text: String,
    icon: Icon
) : ToggleAction(text, null, icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun isSelected(e: AnActionEvent) = getCCv2Settings(e)
        ?.let { getStatuses(it) }
        ?.contains(status)
        ?: false

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        super.update(e)

        e.presentation.isVisible = e.project
            ?.let { CCv2View.getActiveTab(it) == tab }
            ?: false
    }

    protected abstract fun getStatuses(settings: CCv2SettingsState): Set<T>?

    private fun getCCv2Settings(e: AnActionEvent) = e.project
        ?.let { CCv2DeveloperSettings.getInstance(it).ccv2Settings }
}
