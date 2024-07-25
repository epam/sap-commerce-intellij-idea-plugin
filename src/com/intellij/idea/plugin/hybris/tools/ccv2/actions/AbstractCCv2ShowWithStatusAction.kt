/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.ccv2.actions

import com.intellij.idea.plugin.hybris.settings.CCv2Settings
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2View
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import java.util.*
import javax.swing.Icon

abstract class AbstractCCv2ShowWithStatusAction<T : Enum<T>>(
    private val tab: CCv2Tab,
    private val status: T,
    text: String,
    icon: Icon
) : ToggleAction(text, null, icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun isSelected(e: AnActionEvent) = getCCv2Settings(e)
        ?.let { getStatuses(it) }
        ?.contains(status)
        ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        getCCv2Settings(e)
            ?.let { getStatuses(it) }
            ?.let {
                if (state) it.add(status)
                else it.remove(status)
            }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val isRightPlace = "SAP_CX_CCv2_View" == e.place
        e.presentation.isVisible = isRightPlace && e.project
            ?.let { CCv2View.getActiveTab(it) == tab }
            ?: false
    }

    protected abstract fun getStatuses(settings: CCv2Settings): EnumSet<T>?

    private fun getCCv2Settings(e: AnActionEvent) = e.project
        ?.let { DeveloperSettingsComponent.getInstance(it).state.ccv2Settings }
}