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

package com.intellij.idea.plugin.hybris.tools.ccv2

import com.intellij.ide.BrowserUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings
import com.intellij.idea.plugin.hybris.settings.DeveloperSettings
import com.intellij.idea.plugin.hybris.tools.ccv2.api.CCv1Api
import com.intellij.idea.plugin.hybris.tools.ccv2.api.CCv2Api
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.options.ApplicationCCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.CCv2Subscription
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.openapi.util.removeUserData
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.platform.util.progress.reportProgress
import com.intellij.util.io.ZipUtil
import kotlinx.coroutines.*
import java.io.Serial
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

@Service(Service.Level.PROJECT)
class CCv2Service(val project: Project, private val coroutineScope: CoroutineScope) : UserDataHolderBase(), Disposable {

    init {
        with(project.messageBus.connect(this)) {
            subscribe(CCv2SettingsListener.TOPIC, object : CCv2SettingsListener {
                override fun onSubscriptionsChanged(subscriptions: List<CCv2Subscription>) = resetCache()
            })
        }
    }

    override fun dispose() = Unit

    fun cached() = getUserData(KEY_ENVIRONMENTS) != null
        || getUserData(KEY_SERVICES) != null

    fun resetCache() {
        removeUserData(KEY_ENVIRONMENTS)
        removeUserData(KEY_SERVICES)

        Notifications
            .create(
                NotificationType.INFORMATION,
                "CCv2 cache has been reset",
            )
            .notify(project)
    }

    fun fetchEnvironments(
        subscriptions: Collection<CCv2Subscription>,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>) -> Unit,
        sendEvents: Boolean = true,
        statuses: EnumSet<CCv2EnvironmentStatus>? = null,
        requestV1Details: Boolean = true,
        requestV1Health: Boolean = true,
        requestServices: Boolean = false,
    ) {
        if (sendEvents) project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onFetchingStarted(subscriptions)

        val ccv2Settings = DeveloperSettings.getInstance(project).ccv2Settings
        val statuses = (statuses ?: ccv2Settings.showEnvironmentStatuses)
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments...", true) {
                val environments = sortedMapOf<CCv2Subscription, Collection<CCv2EnvironmentDto>>()
                reportProgress(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    val environments = (getCCv2Token(subscription)
                                        ?.let { ccv2Token ->
                                            try {
                                                val cachedEnvironments =
                                                    fetchCacheableEnvironments(progressReporter, ccv2Token, subscription, statuses, requestV1Details, requestV1Health)

                                                if (requestServices) {
                                                    cachedEnvironments
                                                        .filter { it.accessible }
                                                        .map { environment ->
                                                            async {
                                                                environment.services = fetchCacheableEnvironmentServices(ccv2Token, subscription, environment)
                                                            }
                                                        }
                                                        .awaitAll()
                                                }

                                                return@let cachedEnvironments
                                            } catch (e: SocketTimeoutException) {
                                                notifyOnTimeout(subscription)
                                            } catch (e: RuntimeException) {
                                                notifyOnException(subscription, e)
                                            }

                                            return@let emptyList()
                                        }
                                        ?: emptyList())

                                    subscription to environments
                                }
                            }
                            .awaitAll()
                            .let { environments.putAll(it) }
                    }
                }

                onCompleteCallback.invoke(environments)
                if (sendEvents) project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onFetchingCompleted(environments)
            }
        }
    }

    fun fetchEnvironmentsBuilds(subscriptions: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>) {
        project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onFetchingBuildDetailsStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments Build Details...", true) {
                val environments = subscriptions.values.flatten()

                reportProgress(environments.size) { progressReporter ->
                    coroutineScope {
                        subscriptions.forEach { (subscription, environments) ->
                            try {
                                val ccv2Token = getCCv2Token(subscription) ?: return@forEach

                                CCv2Api.getInstance().fetchEnvironmentsBuilds(ccv2Token, subscription, environments, this, progressReporter)
                            } catch (e: SocketTimeoutException) {
                                notifyOnTimeout(subscription)
                            } catch (e: RuntimeException) {
                                notifyOnException(subscription, e)
                            }
                        }
                    }
                }

                project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onFetchingBuildDetailsCompleted(subscriptions)
            }
        }
    }

    fun fetchEnvironmentBuild(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onCompleteCallback: (CCv2BuildDto?) -> Unit,
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Build Details...", true) {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                var build: CCv2BuildDto? = null
                try {
                    build = CCv2Api.getInstance().fetchEnvironmentBuild(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(build)
            }
        }
    }

    fun fetchEnvironmentServices(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onCompleteCallback: (Collection<CCv2ServiceDto>?) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Services...", true) {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                var services: Collection<CCv2ServiceDto>? = null

                try {
                    services = fetchCacheableEnvironmentServices(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(services)
            }
        }
    }

    fun fetchEnvironmentDataBackups(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onCompleteCallback: (Collection<CCv2DataBackupDto>?) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Data Backups...", true) {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                var dataBackups: Collection<CCv2DataBackupDto>? = null

                try {
                    dataBackups = CCv2Api.getInstance().fetchEnvironmentDataBackups(ccv2Token, subscription, environment)
                        .sortedByDescending { it.createdTimestamp }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(dataBackups)
            }
        }
    }

    fun fetchEnvironmentServiceProperties(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
        serviceProperties: CCv2ServiceProperties,
        onCompleteCallback: (Map<String, String>?) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Service Properties...", true) {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                var properties: Map<String, String>? = null

                try {
                    properties = CCv2Api.getInstance().fetchServiceProperties(ccv2Token, subscription, environment, service, serviceProperties)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(properties)
            }
        }
    }

    fun fetchBuilds(
        subscriptions: Collection<CCv2Subscription>,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2BuildDto>>) -> Unit
    ) {
        project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onFetchingStarted(subscriptions)

        val ccv2Settings = DeveloperSettings.getInstance(project).ccv2Settings
        val statusNot = CCv2BuildStatus.entries
            .filterNot { ccv2Settings.showBuildStatuses.contains(it) }
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Builds...", true) {
                val builds = sortedMapOf<CCv2Subscription, Collection<CCv2BuildDto>>()
                reportProgress(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    subscription to (getCCv2Token(subscription)
                                        ?.let { ccv2Token ->
                                            try {
                                                return@let CCv2Api.getInstance().fetchBuilds(ccv2Token, subscription, statusNot, progressReporter)
                                            } catch (e: SocketTimeoutException) {
                                                notifyOnTimeout(subscription)
                                            } catch (e: RuntimeException) {
                                                notifyOnException(subscription, e)
                                            }

                                            return@let emptyList()
                                        }
                                        ?: emptyList())
                                }
                            }
                            .awaitAll()
                            .let { builds.putAll(it) }
                    }
                }

                onCompleteCallback.invoke(builds)
                project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onFetchingCompleted(builds)
            }
        }
    }

    fun fetchDeployments(
        subscriptions: Collection<CCv2Subscription>,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2DeploymentDto>>) -> Unit
    ) {
        project.messageBus.syncPublisher(CCv2DeploymentsListener.TOPIC).onFetchingStarted(subscriptions)

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Deployments...", true) {
                val deployments = sortedMapOf<CCv2Subscription, Collection<CCv2DeploymentDto>>()
                reportProgress(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    subscription to (getCCv2Token(subscription)
                                        ?.let { ccv2Token ->
                                            try {
                                                return@let CCv2Api.getInstance().fetchDeployments(ccv2Token, subscription, progressReporter)
                                            } catch (e: SocketTimeoutException) {
                                                notifyOnTimeout(subscription)
                                            } catch (e: RuntimeException) {
                                                notifyOnException(subscription, e)
                                            }
                                            return@let emptyList()
                                        }
                                        ?: emptyList())
                                }
                            }
                            .awaitAll()
                            .let { deployments.putAll(it) }
                    }
                }

                onCompleteCallback.invoke(deployments)
                project.messageBus.syncPublisher(CCv2DeploymentsListener.TOPIC).onFetchingCompleted(deployments)
            }
        }
    }

    fun createBuild(buildRequest: CCv2BuildRequest) {
        coroutineScope.launch {
            val isAutoDeploy = buildRequest.deploymentRequests.any { it.deploy }
            val title = if (isAutoDeploy) "Creating new CCv2 Build (auto-deploy)..."
            else "Creating new CCv2 Build..."
            withBackgroundProgress(project, title) {
                project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildStarted()
                val ccv2Token = getCCv2Token(buildRequest.subscription) ?: return@withBackgroundProgress

                try {
                    CCv2Api.getInstance().createBuild(ccv2Token, buildRequest)
                        .also { buildCode ->
                            if (buildRequest.track) {
                                trackBuild(project, buildRequest, buildCode)
                            } else {
                                Notifications.create(
                                    NotificationType.INFORMATION,
                                    "CCv2: New Build has been scheduled.",
                                    """
                                    Code: ${buildCode}<br>
                                    Name: ${buildRequest.name}<br>
                                    Branch: ${buildRequest.branch}<br>
                                """.trimIndent()
                                )
                                    .hideAfter(10)
                                    .system(true)
                                    .notify(project)
                            }
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(buildRequest.subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(buildRequest.subscription, e)
                }
            }
        }
    }

    fun deleteBuild(project: Project, subscription: CCv2Subscription, build: CCv2BuildDto) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Deleting CCv2 Build - ${build.code}...") {
                project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildRemovalStarted(subscription, build)

                val ccv2Token = getCCv2Token(subscription)
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildRemovalRequested(subscription, build)
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
                                .system(true)
                                .notify(project)
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun restartServicePod(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, service: CCv2ServiceDto, replica: CCv2ServiceReplicaDto) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Restarting CCv2 Replica - ${replica.name}...") {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress

                try {
                    CCv1Api.getInstance()
                        .restartServiceReplica(ccv2Token, subscription, environment, service, replica)
                        .also {
                            Notifications.create(
                                NotificationType.INFORMATION,
                                "CCv2: Replica pod has been restarted.",
                                """
                                    Replica: ${replica.name}<br>
                                    Service: ${service.name}<br>
                                    Environment: ${environment.name}<br>
                                    Subscription: $subscription<br>
                                """.trimIndent()
                            )
                                .hideAfter(10)
                                .system(true)
                                .notify(project)
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun deployBuild(
        project: Project,
        subscription: CCv2Subscription,
        build: CCv2BuildDto,
        deploymentRequest: CCv2DeploymentRequest
    ) {
        if (!deploymentRequest.deploy) return

        coroutineScope.launch {
            withBackgroundProgress(project, "Deploying CCv2 Build - ${build.code}...") {
                project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildDeploymentStarted(subscription, build)

                val ccv2Token = getCCv2Token(subscription)
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildDeploymentRequested(subscription, build)
                    return@withBackgroundProgress
                }

                try {
                    CCv2Api.getInstance()
                        .deployBuild(ccv2Token, subscription, deploymentRequest.environment, build, deploymentRequest.mode, deploymentRequest.strategy)
                        .also { deploymentCode ->
                            if (deploymentRequest.track) {
                                trackDeployment(project, subscription, deploymentCode, build.code)
                            } else {
                                Notifications.create(
                                    NotificationType.INFORMATION,
                                    "CCv2: Build deployment has been requested.",
                                    """
                                    Code: ${build.code}<br>
                                    Subscription: $subscription<br>
                                """.trimIndent()
                                )
                                    .hideAfter(10)
                                    .system(true)
                                    .notify(project)
                            }
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun downloadBuildLogs(
        project: Project,
        subscription: CCv2Subscription,
        build: CCv2BuildDto,
        onCompleteCallback: (Collection<VirtualFile>) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Downloading CCv2 Build Logs - ${build.code}...") {
                project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildDeploymentStarted(subscription, build)

                val ccv2Token = getCCv2Token(subscription)
                if (ccv2Token == null) {
                    project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildDeploymentRequested(subscription, build)
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
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
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
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                var publicKey: String? = null

                try {
                    publicKey = CCv1Api.getInstance()
                        .fetchMediaStoragePublicKey(ccv2Token, subscription, environment, mediaStorage)
                        .publicKey
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(publicKey)
            }
        }
    }

    fun fetchBuildWithCode(
        subscription: CCv2Subscription,
        buildCode: String,
        onCompleteCallback: (CCv2BuildDto) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching Build - $buildCode...", true) {
                val ccv2Token = getCCv2Token(subscription) ?: return@withBackgroundProgress
                try {
                    val build = CCv2Api.getInstance().fetchBuildForCode(ccv2Token, subscription, buildCode)
                    onCompleteCallback.invoke(build)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun fetchDeploymentsForBuild(
        subscription: CCv2Subscription,
        buildCode: String,
        onStartCallback: () -> Unit,
        onCompleteCallback: (List<CCv2DeploymentDto>) -> Unit
    ) {
        onStartCallback.invoke()
        var deployments: List<CCv2DeploymentDto>
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching Deployment for build - $buildCode...", true) {
                reportProgress(1) { progressReporter ->
                    val ccv2Token = getCCv2Token(subscription)
                    try {
                        deployments = CCv2Api.getInstance().fetchDeploymentsForBuild(subscription, buildCode, ccv2Token!!, progressReporter)
                        onCompleteCallback(deployments)
                    } catch (e: SocketTimeoutException) {
                        notifyOnTimeout(subscription)
                    } catch (e: RuntimeException) {
                        notifyOnException(subscription, e)
                    }
                }
            }
        }
    }

    fun trackBuild(project: Project, buildRequest: CCv2BuildRequest, buildCode: String) {
        if (!buildRequest.track) return

        val subscription = buildRequest.subscription

        coroutineScope.launch {
            withBackgroundProgress(project, "Tracking Progress of the Build - $buildCode..", true) {
                var buildStatus = CCv2BuildStatus.UNKNOWN
                var totalProgress = 0
                val ccv2Token = getCCv2Token(subscription)

                reportProgress { progressReporter ->
                    try {
                        while (buildStatus == CCv2BuildStatus.UNKNOWN || buildStatus == CCv2BuildStatus.SCHEDULED) {
                            checkCanceled()

                            progressReporter.indeterminateStep("Build $buildCode scheduled, warming-up...") {
                                val progress = CCv2Api.getInstance().fetchBuildProgress(subscription, buildCode, ccv2Token!!, progressReporter)
                                buildStatus = progress.buildStatus
                                delay(15.seconds)
                            }
                        }

                        while (buildStatus == CCv2BuildStatus.BUILDING) {
                            checkCanceled()

                            val progress = CCv2Api.getInstance().fetchBuildProgress(subscription, buildCode, ccv2Token!!, progressReporter)
                            val reportProgress = progress.percentage - totalProgress
                            totalProgress = progress.percentage
                            buildStatus = progress.buildStatus

                            progressReporter.sizedStep(
                                reportProgress,
                                "Build $buildCode progress ${progress.percentage}% | ${progress.startedTasks.size} of ${progress.numberOfTasks} tasks"
                            ) {
                                if (totalProgress < 100) {
                                    delay(15.seconds)
                                }
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        notifyOnTimeout(subscription)
                    } catch (e: RuntimeException) {
                        notifyOnException(subscription, e)
                    }
                }

                if (buildStatus == CCv2BuildStatus.FAIL) {
                    Notifications
                        .create(
                            NotificationType.INFORMATION,
                            "CCv2: Build Failed",
                            """
                                Subscription: $subscription<br>
                                Build $buildCode has been failed.
                            """.trimIndent()
                        )
                        .system(true)
                        .notify(project)
                } else {
                    Notifications
                        .create(
                            NotificationType.INFORMATION,
                            "CCv2: Build Completed",
                            """
                                Subscription: $subscription<br>
                                Build $buildCode has been completed with ${buildStatus.title}.
                            """.trimIndent()
                        )
                        .system(true)
                        .notify(project)

                    project.messageBus.syncPublisher(CCv2BuildsListener.TOPIC).onBuildCompleted(
                        subscription,
                        buildCode,
                        buildRequest.deploymentRequests
                    )
                }
            }
        }
    }

    fun trackDeployment(project: Project, subscription: CCv2Subscription, deploymentCode: String, buildCode: String) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Tracking Progress of the Deployment - $buildCode..", true) {
                var totalProgress = 0
                val ccv2Token = getCCv2Token(subscription)

                reportProgress { progressReporter ->
                    while (totalProgress < 100) {
                        checkCanceled()

                        try {
                            val progress = CCv2Api.getInstance().fetchDeploymentProgress(subscription, deploymentCode, ccv2Token!!, progressReporter)
                            val reportProgress = progress.percentage - totalProgress
                            totalProgress = progress.percentage

                            if (progress.deploymentStatus == CCv2DeploymentStatusEnum.FAIL) {
                                cancel(CancellationException("Deployment failed"))
                            }

                            progressReporter.sizedStep(reportProgress, "Deployment $buildCode progress ${progress.percentage}%") {
                                if (totalProgress < 100) {
                                    delay(15.seconds)
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            notifyOnTimeout(subscription)
                        } catch (e: RuntimeException) {
                            notifyOnException(subscription, e)
                        }
                    }
                }

                if (totalProgress == 100) {
                    Notifications
                        .create(
                            NotificationType.INFORMATION,
                            "CCv2: Deployment Completed",
                            """
                                Subscription: $subscription<br>
                                Deployment $buildCode has been completed.
                            """.trimIndent()
                        )
                        .system(true)
                        .notify(project)
                }
            }
        }
    }

    private fun getCCv2Token(subscription: CCv2Subscription): String? {
        val appSettings = ApplicationSettings.getInstance()
        val ccv2Token = appSettings.getCCv2Token(subscription.uuid)
            ?: appSettings.getCCv2Token()

        if (ccv2Token != null) return ccv2Token

        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: API Token is not set",
                "Please, specify CCv2 API token via corresponding application settings."
            )
            .addAction("Open Settings") { _, _ ->
                invokeLater {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
            }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(10)
            .system(true)
            .notify(project)
        return null
    }

    private fun notifyOnTimeout(subscription: CCv2Subscription) {
        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Request interrupted on timeout",
                """
                    Subscription: $subscription<br>
                    Exceeded current read timeout, it can be adjusted via CCv2 settings.
                """.trimIndent()
            )
            .addAction("Open Settings") { _, _ ->
                invokeLater {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
            }
            .hideAfter(10)
            .system(true)
            .notify(project)
    }

    private fun notifyOnException(subscription: CCv2Subscription, e: RuntimeException) {
        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Unable to process request",
                """
                    Subscription: $subscription<br>
                    ${e.message ?: ""}
                """.trimIndent()
            )
            .addAction("Open Settings") { _, _ ->
                invokeLater {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
            }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(15)
            .system(true)
            .notify(project)
    }

    private suspend fun fetchCacheableEnvironments(
        progressReporter: ProgressReporter,
        ccv2Token: String,
        subscription: CCv2Subscription,
        statuses: List<String>,
        requestV1Details: Boolean,
        requestV1Health: Boolean
    ): Collection<CCv2EnvironmentDto> {
        val cacheKey = getCacheKeyForEnvironments(ccv2Token, subscription, statuses)
        val allCachedEnvironments = getOrCreateUserDataUnsafe(KEY_ENVIRONMENTS) { mutableMapOf() }
        val cachedEnvironments = allCachedEnvironments[cacheKey]

        if (cachedEnvironments != null) return cachedEnvironments
            .also { it.forEach { it.deployedBuild = null } }

        val environments = CCv2Api.getInstance()
            .fetchEnvironments(progressReporter, ccv2Token, subscription, statuses, requestV1Details, requestV1Health)
            .sortedBy { it.order }

        allCachedEnvironments[cacheKey] = environments

        return environments
    }

    private suspend fun fetchCacheableEnvironmentServices(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2ServiceDto> {
        val cacheKey = getCacheKeyForServices(ccv2Token, subscription, environment)
        val allCachedServices = getOrCreateUserDataUnsafe(KEY_SERVICES) { mutableMapOf() }
        val cachedServices = allCachedServices[cacheKey]

        if (cachedServices != null) return cachedServices

        val services = CCv1Api.getInstance()
            .fetchEnvironmentServices(ccv2Token, subscription, environment)

        allCachedServices[cacheKey] = services

        return services
    }

    private fun getCacheKeyForEnvironments(
        ccv2Token: String,
        subscription: CCv2Subscription,
        statuses: List<String>
    ): String = ccv2Token + "_" + subscription.uuid + "_" + statuses.joinToString("|")

    private fun getCacheKeyForServices(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): String = ccv2Token + "_" + subscription.uuid + "_" + environment.code

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7864373811369713373L
        private val KEY_ENVIRONMENTS = Key<MutableMap<String, Collection<CCv2EnvironmentDto>>>("CCV2_ENVIRONMENTS")
        private val KEY_SERVICES = Key<MutableMap<String, Collection<CCv2ServiceDto>>>("CCV2_SERVICES")

        fun getInstance(project: Project): CCv2Service = project.service()
    }
}