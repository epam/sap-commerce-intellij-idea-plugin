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
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.options.ApplicationCCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Deployment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.strategies.CCv2Strategy
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.messages.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.*

@Service(Service.Level.PROJECT)
class CCv2Service(val project: Project, val coroutineScope: CoroutineScope) {

    fun fetchEnvironments(subscriptions: Collection<CCv2Subscription>, onStartCallback: () -> Unit, onCompleteCallback: () -> Unit) {
        onStartCallback.invoke()
        project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var environments: SortedMap<CCv2Subscription, Collection<CCv2Environment>> = sortedMapOf()
                try {
                    environments = CCv2Strategy.getStrategy(project).fetchEnvironments(project, ccv2Token, subscriptions)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke()
                project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingCompleted(environments)
            }
        }
    }

    fun fetchBuilds(subscriptions: Collection<CCv2Subscription>, onStartCallback: () -> Unit, onCompleteCallback: () -> Unit) {
        onStartCallback.invoke()
        project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Builds...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var builds: SortedMap<CCv2Subscription, Collection<CCv2Build>> = sortedMapOf()
                try {
                    builds = CCv2Strategy.getStrategy(project).fetchBuilds(project, ccv2Token, subscriptions)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke()
                project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingCompleted(builds)
            }
        }
    }

    fun fetchDeployments(subscriptions: Collection<CCv2Subscription>, onStartCallback: () -> Unit, onCompleteCallback: () -> Unit) {
        onStartCallback.invoke()
        project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Deployments...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var deployments: SortedMap<CCv2Subscription, Collection<CCv2Deployment>> = sortedMapOf()
                try {
                    deployments = CCv2Strategy.getStrategy(project).fetchDeployments(project, ccv2Token, subscriptions)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke()
                project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingCompleted(deployments)
            }
        }
    }

    fun createBuild(subscription: CCv2Subscription, name: String, branch: String) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Creating new CCv2 Build...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildStarted()
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    return@withBackgroundProgress
                }

                try {
                    CCv2Strategy.getStrategy(project).createBuild(project, ccv2Token, subscription, name, branch)
                        .also {
                            if (it != null) {
                                Notifications.create(
                                    NotificationType.INFORMATION,
                                    "CCv2: New Build has been scheduled.",
                                    """
                                    Code: ${it}<br>
                                    Name: ${name}<br>
                                    Branch: ${branch}<br>
                                """.trimIndent()
                                )
                                    .hideAfter(10)
                                    .notify(project)
                            }
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }
            }
        }
    }

    fun deleteBuild(project: Project, subscription: CCv2Subscription, build: CCv2Build) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Deleting CCv2 Build - ${build.code}...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildRemovalStarted(subscription, build)

                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildRemovalRequested(subscription, build)
                    return@withBackgroundProgress
                }

                try {
                    CCv2Strategy.getStrategy(project)
                        .deleteBuild(project, ccv2Token, subscription, build)
                        .also {
                            Notifications.create(
                                NotificationType.INFORMATION,
                                "CCv2: Build has been deleted.",
                                """
                                    Code: ${build.code}<br>
                                    Subscription: $subscription<br>
                                """.trimIndent()
                            )
                                .hideAfter(10)
                                .notify(project)
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }
            }
        }
    }

    private fun getCCv2Token(): String? {
        val appSettings = ApplicationSettingsComponent.getInstance()
        val ccv2Token = appSettings.ccv2Token

        if (ccv2Token != null) return ccv2Token

        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: API Token is not set",
                "Please, specify CCv2 API token via corresponding application settings."
            )
            .addAction("Open Settings") { _, _ ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
            }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(10)
            .notify(project)
        return null
    }

    private fun notifyOnTimeout() {
        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Interrupted on timeout",
                "Exceeded current read timeout, it can be adjusted within CCv2 settings."
            )
            .addAction("Open Settings") { _, _ ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
            }
            .hideAfter(10)
            .notify(project)
    }

    private fun notifyOnException(e: RuntimeException) {
        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Unable to process request",
                e.message ?: ""
            )
            .addAction("Open Settings") { _, _ ->
                ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
            }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(15)
            .notify(project)
    }

    companion object {

        val TOPIC_CCV2_SETTINGS = Topic("HYBRIS_CCV2_SETTINGS", CCv2SettingsListener::class.java)
        val TOPIC_ENVIRONMENT = Topic("HYBRIS_CCV2_ENVIRONMENTS_LISTENER", CCv2EnvironmentsListener::class.java)
        val TOPIC_BUILDS = Topic("HYBRIS_CCV2_BUILDS_LISTENER", CCv2BuildsListener::class.java)
        val TOPIC_DEPLOYMENTS = Topic("HYBRIS_CCV2_DEPLOYMENTS_LISTENER", CCv2DeploymentsListener::class.java)
        fun getInstance(project: Project): CCv2Service = project.getService(CCv2Service::class.java)
    }
}