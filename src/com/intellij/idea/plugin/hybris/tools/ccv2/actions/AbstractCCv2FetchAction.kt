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

package com.intellij.idea.plugin.hybris.tools.ccv2.actions

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DTO
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import java.util.*
import javax.swing.Icon

abstract class AbstractCCv2FetchAction<T : CCv2DTO>(
    tab: CCv2Tab,
    private val text: String,
    description: String? = null,
    private val icon: Icon,
    private val fetch: (Project, List<CCv2Subscription>, (SortedMap<CCv2Subscription, Collection<T>>) -> Unit) -> Unit
) : AbstractCCv2Action(tab, text, description, icon) {

    private var fetching = false

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val subscriptions = DeveloperSettingsComponent.getInstance(project).getActiveCCv2Subscription()
            ?.let { listOf(it) }
            ?: ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions
                .sortedBy { it.toString() }

        fetching = true

        fetch.invoke(
            project, subscriptions,
            onCompleteCallback(e)
        )
    }

    private fun onCompleteCallback(e: AnActionEvent): (SortedMap<CCv2Subscription, Collection<T>>) -> Unit = {
        fetching = false
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        e.presentation.text = if (fetching) "Fetching..." else text
        e.presentation.disabledIcon = if (fetching) AnimatedIcon.Default.INSTANCE else icon
    }

    override fun isEnabled() = !fetching && super.isEnabled()

}