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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2Settings
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.views.CCv2EnvironmentDetailsView
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager

class CCv2FetchEnvironmentsAction : AbstractCCv2FetchAction<CCv2EnvironmentDto>(
    tab = CCv2Tab.ENVIRONMENTS,
    text = "Fetch Environments",
    icon = HybrisIcons.CCv2.Actions.FETCH,
    fetch = { project, subscriptions, onStartCallback, onCompleteCallback ->
        CCv2Service.getInstance(project).fetchEnvironments(subscriptions, onStartCallback, onCompleteCallback)
    }
)

class CCv2FetchEnvironmentAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    private val onStartCallback: () -> Unit,
    private val onCompleteCallback: (CCv2EnvironmentDto) -> Unit
) : DumbAwareAction("Fetch Environment", null, HybrisIcons.CCv2.Actions.FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CCv2Service.getInstance(project).fetchEnvironments(
            listOf(subscription),
            {
                fetching = true
                e.presentation.text = "Fetching..."

                onStartCallback.invoke()
            },
            { response ->
                invokeLater {
                    fetching = false
                    e.presentation.text = "Fetch Environment"

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
        e.presentation.isEnabled = !fetching && ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions.isNotEmpty()
    }
}

class CCv2FetchEnvironmentServiceAction(
    private val subscription: CCv2Subscription,
    private val environment: CCv2EnvironmentDto,
    private val service: CCv2ServiceDto,
    private val onStartCallback: () -> Unit,
    private val onCompleteCallback: (CCv2ServiceDto) -> Unit
) : DumbAwareAction("Fetch Service", null, HybrisIcons.CCv2.Actions.FETCH) {

    private var fetching = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CCv2Service.getInstance(project).fetchEnvironmentServices(
            subscription,
            environment,
            {
                fetching = true
                e.presentation.text = "Fetching..."

                onStartCallback.invoke()
            },
            { response ->
                invokeLater {
                    fetching = false
                    e.presentation.text = "Fetch Environment"

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
        e.presentation.isEnabled = !fetching && ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions.isNotEmpty()
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
            .getToolWindow(HybrisToolWindowFactory.ID) ?: return
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

abstract class AbstractCCv2ShowEnvironmentWithStatusAction(status: CCv2EnvironmentStatus) : AbstractCCv2ShowWithStatusAction<CCv2EnvironmentStatus>(
    CCv2Tab.ENVIRONMENTS,
    status,
    status.title,
    status.icon
) {

    override fun getStatuses(settings: CCv2Settings) = settings.showEnvironmentStatuses
}

class CCv2ShowProvisioningEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.PROVISIONING)
class CCv2ShowAvailableEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.AVAILABLE)
class CCv2ShowTerminatingEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATING)
class CCv2ShowTerminatedEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATED)