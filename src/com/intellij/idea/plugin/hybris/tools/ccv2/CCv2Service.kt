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
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.settings.options.ApplicationCCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.tools.ccv2.api.CCv1Api
import com.intellij.idea.plugin.hybris.tools.ccv2.api.CCv2Api
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.io.ZipUtil
import com.intellij.util.messages.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

@Service(Service.Level.PROJECT)
class CCv2Service(val project: Project, private val coroutineScope: CoroutineScope) {

    fun fetchEnvironments(
        subscriptions: Collection<CCv2Subscription>,
        onStartCallback: () -> Unit,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>) -> Unit,
        sendEvents: Boolean = true,
    ) {
        onStartCallback.invoke()
        if (sendEvents) project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingStarted(subscriptions)

        val ccv2Settings = DeveloperSettingsComponent.getInstance(project).state.ccv2Settings
        val statuses = ccv2Settings.showEnvironmentStatuses
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    if (sendEvents) project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var environments = sortedMapOf<CCv2Subscription, Collection<CCv2EnvironmentDto>>()
                try {
                    environments = CCv2Api.getInstance().fetchEnvironments(ccv2Token, subscriptions, statuses)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(environments)
                if (sendEvents) project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingCompleted(environments)
            }
        }
    }

    fun fetchEnvironmentsBuilds(subscriptions: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) {
        project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingBuildDetailsStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments Build Details...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingBuildDetailsCompleted()
                    return@withBackgroundProgress
                }

                try {
                    CCv2Api.getInstance().fetchEnvironmentsBuilds(ccv2Token, subscriptions)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                project.messageBus.syncPublisher(TOPIC_ENVIRONMENT).onFetchingBuildDetailsCompleted(subscriptions)
            }
        }
    }

    fun fetchEnvironmentBuild(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onStartCallback: () -> Unit,
        onCompleteCallback: (CCv2BuildDto?) -> Unit,
    ) {
        onStartCallback.invoke()

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Build Details...", true) {
                val ccv2Token = getCCv2Token() ?: return@withBackgroundProgress
                var build: CCv2BuildDto? = null
                try {
                    build = CCv2Api.getInstance().fetchEnvironmentBuild(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(build)
            }
        }
    }

    fun fetchEnvironmentServices(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onStartCallback: () -> Unit,
        onCompleteCallback: (Collection<CCv2ServiceDto>?) -> Unit
    ) {
        onStartCallback.invoke()

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Services...", true) {
                val ccv2Token = getCCv2Token() ?: return@withBackgroundProgress
                var services: Collection<CCv2ServiceDto>? = null

                try {
                    services = CCv1Api.getInstance().fetchEnvironmentServices(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(services)
            }
        }
    }

    fun fetchBuilds(
        subscriptions: Collection<CCv2Subscription>,
        onStartCallback: () -> Unit,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2BuildDto>>) -> Unit
    ) {
        onStartCallback.invoke()
        project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingStarted(subscriptions)

        val ccv2Settings = DeveloperSettingsComponent.getInstance(project).state.ccv2Settings
        val statusNot = CCv2BuildStatus.entries
            .filterNot { ccv2Settings.showBuildStatuses.contains(it) }
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Builds...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var builds = sortedMapOf<CCv2Subscription, Collection<CCv2BuildDto>>()
                try {
                    builds = CCv2Api.getInstance().fetchBuilds(ccv2Token, subscriptions, statusNot)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(builds)
                project.messageBus.syncPublisher(TOPIC_BUILDS).onFetchingCompleted(builds)
            }
        }
    }

    fun fetchDeployments(
        subscriptions: Collection<CCv2Subscription>,
        onStartCallback: () -> Unit,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2DeploymentDto>>) -> Unit
    ) {
        onStartCallback.invoke()
        project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Deployments...", true) {
                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingCompleted(subscriptions.associateWith { emptyList() })
                    return@withBackgroundProgress
                }

                var deployments = sortedMapOf<CCv2Subscription, Collection<CCv2DeploymentDto>>()
                try {
                    deployments = CCv2Api.getInstance().fetchDeployments(ccv2Token, subscriptions)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(deployments)
                project.messageBus.syncPublisher(TOPIC_DEPLOYMENTS).onFetchingCompleted(deployments)
            }
        }
    }

    fun createBuild(subscription: CCv2Subscription, name: String, branch: String) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Creating new CCv2 Build...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildStarted()
                val ccv2Token = getCCv2Token() ?: return@withBackgroundProgress

                try {
                    CCv2Api.getInstance().createBuild(ccv2Token, subscription, name, branch)
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

    fun deleteBuild(project: Project, subscription: CCv2Subscription, build: CCv2BuildDto) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Deleting CCv2 Build - ${build.code}...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildRemovalStarted(subscription, build)

                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildRemovalRequested(subscription, build)
                    return@withBackgroundProgress
                }

                try {
                    CCv2Api.getInstance()
                        .deleteBuild(ccv2Token, subscription, build)
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

    fun deployBuild(
        project: Project,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        build: CCv2BuildDto,
        mode: CCv2DeploymentDatabaseUpdateModeEnum,
        strategy: CCv2DeploymentStrategyEnum
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Deploying CCv2 Build - ${build.code}...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildDeploymentStarted(subscription, build)

                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildDeploymentRequested(subscription, build)
                    return@withBackgroundProgress
                }

                try {
                    CCv2Api.getInstance()
                        .deployBuild(ccv2Token, subscription, environment, build, mode, strategy)
                        .also {
                            Notifications.create(
                                NotificationType.INFORMATION,
                                "CCv2: Build deployment has been requested.",
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

    fun downloadBuildLogs(
        project: Project,
        subscription: CCv2Subscription,
        build: CCv2BuildDto,
        onStartCallback: () -> Unit,
        onCompleteCallback: (Collection<VirtualFile>) -> Unit
    ) {
        onStartCallback.invoke()
        coroutineScope.launch {
            withBackgroundProgress(project, "Downloading CCv2 Build Logs - ${build.code}...") {
                project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildDeploymentStarted(subscription, build)

                val ccv2Token = getCCv2Token()
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(TOPIC_BUILDS).onBuildDeploymentRequested(subscription, build)
                    return@withBackgroundProgress
                }

                try {
                    val buildLogs = CCv2Api.getInstance().downloadBuildLogs(ccv2Token, subscription, build)
                    val buildLogsPath = buildLogs.toPath()
                    val tempDirectory = Files.createTempDirectory("ccv2_${build.code}")
                    tempDirectory.toFile().deleteOnExit()

                    ZipUtil.extract(buildLogsPath, tempDirectory, null, true)

                    buildLogsPath.deleteIfExists()

                    val logFiles = tempDirectory.listDirectoryEntries()
                        .onEach { it.toFile().deleteOnExit() }
                        .mapNotNull { LocalFileSystem.getInstance().findFileByPath(it.pathString) }

                    onCompleteCallback.invoke(logFiles)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }
            }
        }
    }

    fun fetchMediaStoragePublicKey(
        project: Project,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        mediaStorage: CCv2MediaStorageDto,
        onStartCallback: () -> Unit,
        onCompleteCallback: (String?) -> Unit
    ) {
        onStartCallback.invoke()
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Media Storage Public Key - ${mediaStorage.name}...") {
                val ccv2Token = getCCv2Token() ?: return@withBackgroundProgress
                var publicKey: String? = null

                try {
                    publicKey = CCv1Api.getInstance()
                        .fetchMediaStoragePublicKey(ccv2Token, subscription, environment, mediaStorage)
                        ?.publicKey
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout()
                } catch (e: RuntimeException) {
                    notifyOnException(e)
                }

                onCompleteCallback.invoke(publicKey)
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