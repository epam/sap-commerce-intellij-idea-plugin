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
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentStatus
import sap.commerce.toolset.ccv2.dto.CCv2ServiceDto
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SettingsState
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab
import sap.commerce.toolset.ccv2.ui.view.CCv2EnvironmentDetailsView

class CCv2FetchEnvironmentsAction : CCv2FetchAction<CCv2EnvironmentDto>(
    tab = CCv2Tab.ENVIRONMENTS,
    text = "Fetch Environments",
    icon = HybrisIcons.CCv2.Actions.FETCH,
    fetch = { project, subscriptions, onCompleteCallback ->
        CCv2Service.getInstance(project).fetchEnvironments(subscriptions, onCompleteCallback)
    }
)

class CCv2FetchEnvironmentAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    private val onCompleteCallback: (CCv2EnvironmentDto) -> Unit
) : DumbAwareAction("Fetch Environment", null, HybrisIcons.CCv2.Actions.FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        fetching = true

        CCv2Service.getInstance(project).fetchEnvironments(
            listOf(subscription),
            { response ->
                fetching = false

                invokeLater {
                    val fetchedEnvironment = response[subscription]
                        ?.find { it.code == environment.code }

                    if (fetchedEnvironment != null) {
                        onCompleteCallback.invoke(fetchedEnvironment)
                    } else {
                        Notifications.create(
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
        e.presentation.isEnabled = !fetching && CCv2ProjectSettings.getInstance().ccv2Subscriptions.isNotEmpty()
        e.presentation.text = if (fetching) "Fetching..." else "Fetch Environment"
        e.presentation.disabledIcon = if (fetching) AnimatedIcon.Default.INSTANCE else HybrisIcons.CCv2.Actions.FETCH
    }
}

class CCv2FetchEnvironmentServiceAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    private val service: CCv2ServiceDto,
    private val onCompleteCallback: (CCv2ServiceDto) -> Unit
) : DumbAwareAction("Fetch Service", null, HybrisIcons.CCv2.Actions.FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        fetching = true

        CCv2Service.getInstance(project).fetchEnvironmentServices(
            subscription,
            environment,
            { response ->
                fetching = false

                invokeLater {
                    val fetchedService = response
                        ?.find { it.code == service.code }

                    if (fetchedService != null) {
                        onCompleteCallback.invoke(fetchedService)
                    } else {
                        Notifications.create(
                            NotificationType.WARNING,
                            "Unable to fetch service",
                            "Service ${service.code} is not found."
                        )
                            .hideAfter(10)
                            .notify(project)
                    }
                }
            }
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !fetching && CCv2ProjectSettings.getInstance().ccv2Subscriptions.isNotEmpty()
        e.presentation.text = if (fetching) "Fetching..." else "Fetch Service"
        e.presentation.disabledIcon = if (fetching) AnimatedIcon.Default.INSTANCE else HybrisIcons.CCv2.Actions.FETCH
    }
}

class CCv2ShowEnvironmentDetailsAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto
) : DumbAwareAction("Show Environment Details", null, HybrisIcons.CCv2.Environment.Actions.SHOW_DETAILS) {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(HybrisConstants.TOOLWINDOW_ID) ?: return
        val contentManager = toolWindow.contentManager
        val panel = CCv2EnvironmentDetailsView(project, subscription, environment)
        val content = contentManager.factory
            .createContent(panel, environment.name, true)
            .also {
                it.isCloseable = true
                it.isPinnable = true
                it.icon = HybrisIcons.CCv2.Environment.Actions.SHOW_DETAILS
                it.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            }

        Disposer.register(toolWindow.disposable, panel)

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = environment.accessible
    }
}

abstract class CCv2ShowEnvironmentWithStatusAction(status: CCv2EnvironmentStatus) : CCv2ShowWithStatusAction<CCv2EnvironmentStatus>(
    CCv2Tab.ENVIRONMENTS,
    status,
    status.title,
    status.icon
) {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val developerSettings = CCv2DeveloperSettings.getInstance(project)
        val mutableSettings = developerSettings.ccv2Settings.mutable()
        if (state) mutableSettings.showEnvironmentStatuses.add(status)
        else mutableSettings.showEnvironmentStatuses.remove(status)

        developerSettings.ccv2Settings = mutableSettings.immutable()
    }

    override fun getStatuses(settings: CCv2SettingsState) = settings.showEnvironmentStatuses
}

class CCv2ShowProvisioningEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.PROVISIONING)
class CCv2ShowAvailableEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.AVAILABLE)
class CCv2ShowTerminatingEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATING)
class CCv2ShowTerminatedEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATED)