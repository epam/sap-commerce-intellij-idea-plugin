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

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.CCv2UiConstants
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings

class CCv2FetchEnvironmentAction : DumbAwareAction("Fetch Environment", null, HybrisIcons.CCv2.Actions.FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val subscription = e.getData(CCv2UiConstants.DataKeys.Subscription) ?: return
        val environment = e.getData(CCv2UiConstants.DataKeys.Environment) ?: return
        val environmentCallback = e.getData(CCv2UiConstants.DataKeys.EnvironmentCallback) ?: return

        fetching = true

        CCv2Service.Companion.getInstance(project).fetchEnvironments(
            listOf(subscription),
            { response ->
                fetching = false

                invokeLater {
                    val fetchedEnvironment = response[subscription]
                        ?.find { it.code == environment.code }

                    if (fetchedEnvironment != null) {
                        environmentCallback.invoke(fetchedEnvironment)
                    } else {
                        Notifications.Companion.create(
                            NotificationType.WARNING,
                            "Unable to fetch environment",
                            "Environment ${environment.code} is not found."
                        )
                            .hideAfter(10)
                            .notify(project)
                    }
                }
            },
            false
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        e.presentation.isEnabled = !fetching && CCv2ProjectSettings.getInstance().subscriptions.isNotEmpty()
        e.presentation.text = if (fetching) "Fetching..." else "Fetch Environment"
        e.presentation.disabledIcon = if (fetching) AnimatedIcon.Default.INSTANCE else HybrisIcons.CCv2.Actions.FETCH
    }
}