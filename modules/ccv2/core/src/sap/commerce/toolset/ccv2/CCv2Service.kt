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

package sap.commerce.toolset.ccv2

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.credentialStore.Credentials
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
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
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.ZipUtil
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.ccv2.api.*
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.event.*
import sap.commerce.toolset.ccv2.model.EndpointUpdateDTO
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2ApplicationSettingsState
import sap.commerce.toolset.ccv2.settings.state.CCv2Authentication
import sap.commerce.toolset.ccv2.settings.state.CCv2AuthenticationMode
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.util.directoryExists
import java.io.Serial
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

@Service(Service.Level.PROJECT)
class CCv2Service(private val project: Project, private val coroutineScope: CoroutineScope) : UserDataHolderBase(), Disposable {

    init {
        with(project.messageBus.connect(this)) {
            subscribe(CCv2SettingsListener.TOPIC, object : CCv2SettingsListener {
                override fun onChange(state: CCv2ApplicationSettingsState) = resetCache()
            })
        }
    }

    override fun dispose() = Unit

    fun cached() = getUserData(KEY_ENVIRONMENTS) != null
        || getUserData(KEY_SERVICES) != null
        || getUserData(KEY_ENDPOINTS) != null

    fun resetCache() {
        removeUserData(KEY_ENVIRONMENTS)
        removeUserData(KEY_SERVICES)
        removeUserData(KEY_ENDPOINTS)

        Notifications
            .create(
                NotificationType.INFORMATION,
                "CCv2 cache has been reset",
            )
            .notify(project)
    }

    fun fetchSubscriptions(ccv2Token: ApiContext) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Subscriptions...", true) {
                try {
                    val subscriptions = CCv1Api.getInstance().fetchSubscriptions(ccv2Token)

                    project.messageBus.syncPublisher(CCv2SubscriptionsListener.TOPIC).onFetchingComplete(subscriptions)
                } catch (e: Throwable) {
                    project.messageBus.syncPublisher(CCv2SubscriptionsListener.TOPIC).onFetchingError(e)
                }
            }
        }
    }

    fun fetchEnvironmentScaling(subscription: CCv2Subscription, environment: CCv2EnvironmentDto) {
        if (environment.scaling != null) {
            project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onScalingFetched(environment, environment.scaling)
            return
        }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Scaling Details...", true) {
                try {
                    val apiContext = getApiContext(subscription)
                    val scaling = if (apiContext != null) CCv2Api.getInstance().fetchEnvironmentScaling(apiContext, subscription, environment)
                        .let { CCv2EnvironmentScalingDto.map(it) }
                    else null

                    environment.scaling = scaling

                    project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onScalingFetched(environment, scaling)
                } catch (e: Throwable) {
                    project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onScalingFetchingError(environment, e)
                }
            }
        }
    }

    fun fetchEnvironments(
        subscriptions: Collection<CCv2Subscription>,
        onCompleteCallback: (SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>>) -> Unit,
        sendEvents: Boolean = true,
        statuses: EnumSet<CCv2EnvironmentStatus>? = null,
        requestV1Details: Boolean = true,
        requestV1Health: Boolean = true,
        requestServices: Boolean = false,
        requestEndpoints: Boolean = false,
    ) {
        if (sendEvents) project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onFetchingStarted(subscriptions)

        val ccv2Settings = CCv2DeveloperSettings.getInstance(project).ccv2Settings
        val statuses = (statuses ?: ccv2Settings.showEnvironmentStatuses)
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environments...", true) {
                val environments = sortedMapOf<CCv2Subscription, Collection<CCv2EnvironmentDto>>()
                reportProgressScope(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    checkCanceled()
                                    val ccv2Token = getApiContext(subscription) ?: return@async (subscription to emptyList())
                                    try {
                                        val cachedEnvironments = fetchCacheableEnvironments(progressReporter, ccv2Token, subscription, statuses, requestV1Details, requestV1Health)

                                        cachedEnvironments
                                            .filter { it.accessible }
                                            .flatMap { environment ->
                                                listOfNotNull(
                                                    if (requestServices) {
                                                        async {
                                                            checkCanceled()
                                                            environment.services = fetchCacheableEnvironmentServices(ccv2Token, subscription, environment)
                                                        }
                                                    } else null,

                                                    if (requestEndpoints) {
                                                        async {
                                                            checkCanceled()
                                                            environment.endpoints = fetchCacheableEnvironmentEndpoints(ccv2Token, subscription, environment)
                                                        }
                                                    } else null,
                                                )
                                            }
                                            .awaitAll()

                                        return@async subscription to cachedEnvironments
                                    } catch (e: SocketTimeoutException) {
                                        notifyOnTimeout(subscription, e)
                                    } catch (e: RuntimeException) {
                                        notifyOnException(subscription, e)
                                    }

                                    subscription to emptyList()
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

                reportProgressScope(environments.size) { progressReporter ->
                    coroutineScope {
                        subscriptions.forEach { (subscription, environments) ->
                            try {
                                val ccv2Token = getApiContext(subscription) ?: return@forEach

                                CCv2Api.getInstance().fetchEnvironmentsBuilds(ccv2Token, subscription, environments, this, progressReporter)
                            } catch (e: SocketTimeoutException) {
                                notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var build: CCv2BuildDto? = null
                try {
                    build = CCv2Api.getInstance().fetchEnvironmentBuild(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var services: Collection<CCv2ServiceDto>? = null

                try {
                    services = fetchCacheableEnvironmentServices(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(services)
            }
        }
    }

    fun fetchEnvironmentEndpoints(
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        onCompleteCallback: (Collection<CCv2EndpointDto>?) -> Unit
    ) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Environment Endpoints...", true) {
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var endpoints: Collection<CCv2EndpointDto>? = null

                try {
                    endpoints = fetchCacheableEnvironmentEndpoints(ccv2Token, subscription, environment)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }

                onCompleteCallback.invoke(endpoints)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var dataBackups: Collection<CCv2DataBackupDto>? = null

                try {
                    dataBackups = CCv2Api.getInstance().fetchEnvironmentDataBackups(ccv2Token, subscription, environment)
                        ?.sortedByDescending { it.createdTimestamp }
                        ?: emptyList()
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var properties: Map<String, String>? = null

                try {
                    properties = CCv2Api.getInstance().fetchServiceProperties(ccv2Token, subscription, environment, service, serviceProperties)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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

        val ccv2Settings = CCv2DeveloperSettings.getInstance(project).ccv2Settings
        val statusNot = CCv2BuildStatus.entries
            .filterNot { ccv2Settings.showBuildStatuses.contains(it) }
            .map { it.name }

        coroutineScope.launch {
            withBackgroundProgress(project, "Fetching CCv2 Builds...", true) {
                val builds = sortedMapOf<CCv2Subscription, Collection<CCv2BuildDto>>()
                reportProgressScope(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    checkCanceled()
                                    subscription to (getApiContext(subscription)
                                        ?.let { ccv2Token ->
                                            try {
                                                return@let CCv2Api.getInstance().fetchBuilds(ccv2Token, subscription, statusNot, progressReporter)
                                            } catch (e: SocketTimeoutException) {
                                                notifyOnTimeout(subscription, e)
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
                reportProgressScope(subscriptions.size) { progressReporter ->
                    coroutineScope {
                        subscriptions
                            .map { subscription ->
                                async {
                                    checkCanceled()
                                    subscription to (getApiContext(subscription)
                                        ?.let { ccv2Token ->
                                            try {
                                                return@let CCv2Api.getInstance().fetchDeployments(ccv2Token, subscription, progressReporter)
                                            } catch (e: SocketTimeoutException) {
                                                notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(buildRequest.subscription) ?: return@withBackgroundProgress

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
                    notifyOnTimeout(buildRequest.subscription, e)
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

                val ccv2Token = getApiContext(subscription)
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
                                    Subscription: ${subscription.presentableName}<br>
                                """.trimIndent()
                            )
                                .hideAfter(10)
                                .system(true)
                                .notify(project)
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
                } catch (e: RuntimeException) {
                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun restartServicePod(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, service: CCv2ServiceDto, replica: CCv2ServiceReplicaDto) {
        coroutineScope.launch {
            withBackgroundProgress(project, "Restarting CCv2 Replica - ${replica.name}...") {
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress

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
                                    Subscription: ${subscription.presentableName}<br>
                                """.trimIndent()
                            )
                                .hideAfter(10)
                                .system(true)
                                .notify(project)
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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

                val ccv2Token = getApiContext(subscription)
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
                                    Subscription: ${subscription.presentableName}<br>
                                """.trimIndent()
                                )
                                    .hideAfter(10)
                                    .system(true)
                                    .notify(project)
                            }
                        }
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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

                val ccv2Token = getApiContext(subscription)
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

                    val logFiles = tempDirectory
                        .takeIf { it.directoryExists }
                        ?.listDirectoryEntries()
                        ?.map {
                            // rename <logFile>.txt to <logFile>.log
                            Files.move(it, it.resolveSibling(it.nameWithoutExtension + ".log"))
                        }
                        ?.onEach { it.toFile().deleteOnExit() }
                        ?.mapNotNull { LocalFileSystem.getInstance().findFileByPath(it.pathString) }
                        ?: emptyList()

                    onCompleteCallback.invoke(logFiles)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                var publicKey: String? = null

                try {
                    publicKey = CCv1Api.getInstance()
                        .fetchMediaStoragePublicKey(ccv2Token, subscription, environment, mediaStorage)
                        .publicKey
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                try {
                    val build = CCv2Api.getInstance().fetchBuildForCode(ccv2Token, subscription, buildCode)
                    onCompleteCallback.invoke(build)
                } catch (e: SocketTimeoutException) {
                    notifyOnTimeout(subscription, e)
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
                reportProgressScope(1) { progressReporter ->
                    val ccv2Token = getApiContext(subscription)
                    try {
                        deployments = CCv2Api.getInstance().fetchDeploymentsForBuild(subscription, buildCode, ccv2Token!!, progressReporter)
                        onCompleteCallback(deployments)
                    } catch (e: SocketTimeoutException) {
                        notifyOnTimeout(subscription, e)
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress

                reportProgressScope { progressReporter ->
                    try {
                        while (buildStatus == CCv2BuildStatus.UNKNOWN || buildStatus == CCv2BuildStatus.SCHEDULED) {
                            checkCanceled()

                            progressReporter.indeterminateStep("Build $buildCode scheduled, warming-up...") {
                                val progress = CCv2Api.getInstance().fetchBuildProgress(subscription, buildCode, ccv2Token, progressReporter)
                                buildStatus = progress.buildStatus
                                delay(15.seconds)
                            }
                        }

                        while (buildStatus == CCv2BuildStatus.BUILDING) {
                            checkCanceled()

                            val progress = CCv2Api.getInstance().fetchBuildProgress(subscription, buildCode, ccv2Token, progressReporter)
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
                        notifyOnTimeout(subscription, e)
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
                                Subscription: ${subscription.presentableName}<br>
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
                                Subscription: ${subscription.presentableName}<br>
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
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress

                reportProgressScope { progressReporter ->
                    while (totalProgress < 100) {
                        checkCanceled()

                        try {
                            val progress = CCv2Api.getInstance().fetchDeploymentProgress(subscription, deploymentCode, ccv2Token, progressReporter)
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
                            notifyOnTimeout(subscription, e)
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
                                Subscription: ${subscription.presentableName}<br>
                                Deployment $buildCode has been completed.
                            """.trimIndent()
                        )
                        .system(true)
                        .notify(project)
                }
            }
        }
    }

    fun toggleEndpointMaintenanceMode(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, endpoint: CCv2EndpointDto) {
        endpoint.actionsAllowed = false

        coroutineScope.launch {
            val title = if (endpoint.maintenanceMode) "Deactivating Maintenance Mode - ${environment.code} - ${endpoint.name}"
            else "Activating Maintenance Mode - ${environment.code} - ${endpoint.name}"

            withBackgroundProgress(project, title, true) {
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                checkCanceled()

                try {
                    val payload = EndpointUpdateDTO(
                        maintenanceMode = !endpoint.maintenanceMode,
                    )
                    CCv2Api.getInstance().updateEndpoint(ccv2Token, subscription, environment, endpoint, payload)

                    environment.endpoints = null
                    resetCache(KEY_ENDPOINTS, ccv2Token, subscription, environment)

                    project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onEndpointUpdate(environment)
                } catch (e: SocketTimeoutException) {
                    endpoint.actionsAllowed = true

                    notifyOnTimeout(subscription, e)
                } catch (e: RuntimeException) {
                    endpoint.actionsAllowed = true

                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun deleteEndpoint(project: Project, subscription: CCv2Subscription, environment: CCv2EnvironmentDto, endpoint: CCv2EndpointDto) {
        endpoint.actionsAllowed = false

        coroutineScope.launch {
            withBackgroundProgress(project, "Deleting Endpoint - ${environment.code} - ${endpoint.name}", true) {
                val ccv2Token = getApiContext(subscription) ?: return@withBackgroundProgress
                checkCanceled()

                try {
                    CCv2Api.getInstance().deleteEndpoint(ccv2Token, subscription, environment, endpoint)

                    environment.endpoints = null
                    resetCache(KEY_ENDPOINTS, ccv2Token, subscription, environment)

                    project.messageBus.syncPublisher(CCv2EnvironmentsListener.TOPIC).onEndpointUpdate(environment)
                } catch (e: SocketTimeoutException) {
                    endpoint.actionsAllowed = true

                    notifyOnTimeout(subscription, e)
                } catch (e: RuntimeException) {
                    endpoint.actionsAllowed = true

                    notifyOnException(subscription, e)
                }
            }
        }
    }

    fun retrieveAuthToken(apiUrl: String, auth: CCv2Authentication, credentials: Credentials): ApiContext? {
        val requestBody = mapOf(
            "client_id" to (credentials.userName ?: ""),
            "client_secret" to (credentials.getPasswordAsString() ?: ""),
            "grant_type" to URLEncoder.encode("client_credentials", StandardCharsets.UTF_8),
            "resource" to URLEncoder.encode(auth.resource, StandardCharsets.UTF_8)
        )
            .entries
            .joinToString("&")

        return try {
            HttpRequests.post(auth.tokenEndpoint, "application/x-www-form-urlencoded")
                .accept("application/json")
                .connect { request ->
                    request.write(requestBody)

                    Gson()
                        .fromJson(request.readString(), JsonObject::class.java)
                        .get("access_token")?.asString
                        ?.let { KymaApiContext(apiUrl, it) }
                }
        } catch (e: Exception) {
            // Handle connection errors or non-200 responses
            thisLogger().warn("Exception while fetching auth token", e)
            null
        }
    }

    private fun getApiContext(subscription: CCv2Subscription): ApiContext? {
        val appSettings = CCv2ProjectSettings.getInstance()

        val ccv2Token = if (subscription.authenticationMode == CCv2AuthenticationMode.TOKEN) {
            (appSettings.getCCv2Token(subscription.uuid) ?: appSettings.getCCv2Token())
                ?.let { HanaApiContext(appSettings.hanaApiUrl, it) }
        } else retrieveCCv2ClientToken(subscription, appSettings)

        if (ccv2Token != null) return ccv2Token

        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: API Token is not set",
                "Please, specify CCv2 API token via corresponding application settings."
            )
            .addAction("Open Settings") { _, _ -> project.triggerAction("ccv2.open.settings.action") }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(10)
            .system(true)
            .notify(project)
        return null
    }

    private fun retrieveCCv2ClientToken(subscription: CCv2Subscription, appSettings: CCv2ProjectSettings): ApiContext? {
        val subscriptionAuth = subscription.authentication
        val subscriptionClient = appSettings.getCCv2Authentication(subscription.uuid)
        val apiUrl = appSettings.kymaApiUrl

        val subscriptionAuthToken = if (subscriptionAuth != null && subscriptionClient != null) {
            retrieveAuthToken(apiUrl, subscriptionAuth, subscriptionClient)
        } else null

        if (subscriptionAuthToken != null) return subscriptionAuthToken

        val auth = appSettings.authentication
        val client = appSettings.getCCv2Authentication() ?: return null

        return retrieveAuthToken(apiUrl, auth, client)
    }

    private fun notifyOnTimeout(subscription: CCv2Subscription, e: SocketTimeoutException) = notifyOnTimeout(
        "Subscription: ${subscription.presentableName}", e
    )

    private fun notifyOnException(subscription: CCv2Subscription, e: RuntimeException) = notifyOnException(
        "Subscription: ${subscription.presentableName}", e
    )

    private fun notifyOnTimeout(content: String, e: SocketTimeoutException) {
        thisLogger().warn(e)

        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Request interrupted on timeout",
                """
                    $content<br>
                    Exceeded current read timeout, it can be adjusted via CCv2 settings.
                """.trimIndent()
            )
            .addAction("Open Settings") { _, _ -> project.triggerAction("ccv2.open.settings.action") }
            .hideAfter(10)
            .system(true)
            .notify(project)
    }

    private fun notifyOnException(content: String, e: RuntimeException) {
        thisLogger().warn(e)

        Notifications
            .create(
                NotificationType.WARNING,
                "CCv2: Unable to process request",
                """
                    $content<br>
                    ${e.message ?: ""}
                """.trimIndent()
            )
            .addAction("Open Settings") { _, _ -> project.triggerAction("ccv2.open.settings.action") }
            .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
            .hideAfter(15)
            .system(true)
            .notify(project)
    }

    private suspend fun fetchCacheableEnvironments(
        progressReporter: ProgressReporter,
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        statuses: List<String>,
        requestV1Details: Boolean,
        requestV1Health: Boolean
    ): Collection<CCv2EnvironmentDto> {
        val cacheKey = getCacheKey(ccv2Token, subscription, statuses)
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
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2ServiceDto> {
        val cacheKey = getCacheKey(ccv2Token, subscription, environment)
        val allCachedServices = getOrCreateUserDataUnsafe(KEY_SERVICES) { mutableMapOf() }
        val cachedServices = allCachedServices[cacheKey]

        if (cachedServices != null) return cachedServices

        val services = CCv1Api.getInstance()
            .fetchEnvironmentServices(ccv2Token, subscription, environment)

        allCachedServices[cacheKey] = services

        return services
    }

    private suspend fun fetchCacheableEnvironmentEndpoints(
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2EndpointDto>? {
        val cacheKey = getCacheKey(ccv2Token, subscription, environment)
        val mutableCache = getOrCreateUserDataUnsafe(KEY_ENDPOINTS) { mutableMapOf() }
        val cachedValue = mutableCache[cacheKey]

        if (cachedValue != null) return cachedValue

        val endpoints = CCv2Api.getInstance()
            .fetchEndpoints(ccv2Token, subscription, environment)

        if (endpoints == null) mutableCache.remove(cacheKey)
        else mutableCache[cacheKey] = endpoints

        return endpoints
    }

    private fun getCacheKey(
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        statuses: List<String>
    ): String = ccv2Token.authToken + "_" + ccv2Token.authHeader + "_" + subscription.uuid + "_" + statuses.joinToString("|")

    private fun getCacheKey(
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): String = ccv2Token.authToken + "_" + ccv2Token.authHeader + "_" + subscription.uuid + "_" + environment.code

    private fun <T : MutableMap<String, *>> resetCache(
        key: Key<T>,
        ccv2Token: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ) {
        val cacheKey = getCacheKey(ccv2Token, subscription, environment)
        val cache = getUserData(key) ?: return
        cache.remove(cacheKey)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7864373811369713373L

        // in case of a new cached keys, always modify #cached and #resetCache methods
        private val KEY_ENVIRONMENTS = Key<MutableMap<String, Collection<CCv2EnvironmentDto>>>("CCV2_ENVIRONMENTS")
        private val KEY_SERVICES = Key<MutableMap<String, Collection<CCv2ServiceDto>>>("CCV2_SERVICES")
        private val KEY_ENDPOINTS = Key<MutableMap<String, Collection<CCv2EndpointDto>>>("CCV2_ENDPOINTS")

        fun getInstance(project: Project): CCv2Service = project.service()
    }
}