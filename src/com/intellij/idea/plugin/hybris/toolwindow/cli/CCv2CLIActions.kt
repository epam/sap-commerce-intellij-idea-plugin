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

package com.intellij.idea.plugin.hybris.toolwindow.cli

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.cli.CCv2CLIService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import javax.swing.Icon

abstract class FetchAction(
    text: String,
    val taskTitle: String,
    icon: Icon? = null
) : AnAction(text, null, icon) {

    private var fetching = false
    protected abstract fun fetch(project: Project, ccv2Subscriptions: List<CCv2Subscription>)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val task = object : Task.Backgroundable(project, taskTitle) {
            override fun run(indicator: ProgressIndicator) {
                fetching = true
                val ccv2Subscriptions = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(project).getActiveCCv2Subscription()
                    ?.let { listOf(it) }
                    ?: HybrisApplicationSettingsComponent.getInstance().state.ccv2Subscriptions
                        .sortedBy { it.toString() }

                fetch(project, ccv2Subscriptions)

                fetching = false
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !fetching && HybrisApplicationSettingsComponent.getInstance().state.ccv2Subscriptions.isNotEmpty()
    }
}

class RetrieveEnvironmentsAction() : FetchAction(
    "Fetch Environments",
    "Fetching CCv2 Environments...",
    HybrisIcons.CCV2_CLI_FETCH
) {

    override fun fetch(project: Project, ccv2Subscriptions: List<CCv2Subscription>) {
        CCv2CLIService.getInstance(project)
            .fetchEnvironments(ccv2Subscriptions)
    }
}