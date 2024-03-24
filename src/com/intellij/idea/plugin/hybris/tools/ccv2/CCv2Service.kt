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

package com.intellij.idea.plugin.hybris.tools.ccv2

import com.intellij.ide.BrowserUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.strategies.CCv2Strategy
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

@Service(Service.Level.PROJECT)
class CCv2Service(val project: Project) {

    private val messageBus = project.messageBus

    fun fetchEnvironments(subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Environment>> {
        messageBus.syncPublisher(TOPIC_ENVIRONMENT).fetchingStarted()

        val appSettings = HybrisApplicationSettingsComponent.getInstance()
        val ccv2Token = appSettings.ccv2Token

        if (ccv2Token == null) {
            Notifications
                .create(
                    NotificationType.WARNING,
                    "CCv2: API Token is not set",
                    "Please, specify CCv2 API token via corresponding application settings."
                )
                .addAction("Open Settings") { _, _ ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, CCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
                .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
                .hideAfter(10)
                .notify(project)

            messageBus.syncPublisher(TOPIC_ENVIRONMENT).fetchingCompleted(emptyMap())
            return emptyMap()
        }

        return CCv2Strategy.getSAPCCMCCv2Strategy().fetchEnvironments(project, ccv2Token, subscriptions)
            .also { messageBus.syncPublisher(TOPIC_ENVIRONMENT).fetchingCompleted(it) }
    }

    companion object {

        val TOPIC_ENVIRONMENT = Topic("HYBRIS_CCV2_ENVIRONMENT_LISTENER", CCv2EnvironmentListener::class.java)
        fun getInstance(project: Project): CCv2Service = project.getService(CCv2Service::class.java)
    }
}