/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.ccv2.mcp

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.util.application
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.api.ApiContext
import sap.commerce.toolset.ccv2.api.CCv1Api
import sap.commerce.toolset.ccv2.api.CCv2Api
import sap.commerce.toolset.ccv2.api.KymaApiContext
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.mcp.CCv2McpMapper.mcpDto
import sap.commerce.toolset.ccv2.mcp.dto.*
import sap.commerce.toolset.ccv2.model.EndpointUpdateDTO
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Authentication
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.unscramble.CCv2UnscrambleService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@Service
class CCv2McpService {

    fun listSubscriptions(): CCv2SubscriptionsMcpDto {
        val subscriptions = CCv2ProjectSettings.getInstance().subscriptions
        return CCv2SubscriptionsMcpDto(
            total = subscriptions.size,
            items = subscriptions.map { CCv2SubscriptionMcpDto(id = it.id, name = it.name) },
        )
    }

    suspend fun listEnvironments(subscriptionId: String?): CCv2EnvironmentsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val statuses = CCv2EnvironmentStatus.entries
            .filter { it != CCv2EnvironmentStatus.UNKNOWN }
            .map { it.name }

        val environments = coroutineScope {
            reportProgressScope(1) { reporter ->
                CCv2Api.getInstance().fetchEnvironments(reporter, apiContext, subscription, statuses, false, false)
            }
        }

        return CCv2EnvironmentsMcpDto(
            subscription = subscription.presentableName,
            total = environments.size,
            items = environments.map { it.mcpDto() },
        )
    }

    suspend fun listBuilds(subscriptionId: String?, top: Int): CCv2BuildsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val builds = coroutineScope {
            reportProgressScope(1) { reporter ->
                CCv2Api.getInstance().fetchBuilds(
                    apiContext = apiContext,
                    subscription = subscription,
                    statusNot = listOf(CCv2BuildStatus.DELETED.name),
                    top = top,
                    orderBy = null,
                    progressReporter = reporter,
                )
            }
        }

        return CCv2BuildsMcpDto(
            subscription = subscription.presentableName,
            total = builds.size,
            items = builds.map { it.mcpDto() },
        )
    }

    suspend fun getBuild(subscriptionId: String?, buildCode: String): CCv2BuildMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        return CCv2Api.getInstance().fetchBuildForCode(apiContext, subscription, buildCode).mcpDto()
    }

    suspend fun createBuild(subscriptionId: String?, branch: String, name: String): CCv2OperationResultMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val buildRequest = CCv2BuildRequest(
            subscription = subscription,
            branch = branch,
            name = name,
            track = false,
            deploymentRequests = emptyList(),
        )
        val buildCode = CCv2Api.getInstance().createBuild(apiContext, buildRequest)
        return CCv2OperationResultMcpDto(
            subscription = subscription.presentableName,
            success = true,
            code = buildCode,
            message = "Build '$buildCode' scheduled for branch '$branch'.",
        )
    }

    suspend fun listDeployments(subscriptionId: String?): CCv2DeploymentsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val deployments = coroutineScope {
            reportProgressScope(1) { reporter ->
                CCv2Api.getInstance().fetchDeployments(apiContext, subscription, reporter)
            }
        }

        return CCv2DeploymentsMcpDto(
            subscription = subscription.presentableName,
            total = deployments.size,
            items = deployments.map { it.mcpDto() },
        )
    }

    suspend fun deployBuild(
        subscriptionId: String?,
        buildCode: String,
        environmentCode: String,
        mode: String,
        strategy: String,
    ): CCv2OperationResultMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val build = CCv2Api.getInstance().fetchBuildForCode(apiContext, subscription, buildCode)
        val updateMode = CCv2DeploymentDatabaseUpdateModeEnum.tryValueOf(mode.uppercase())
            .takeIf { it != CCv2DeploymentDatabaseUpdateModeEnum.UNKNOWN }
            ?: error("Invalid deployment mode '$mode'. Valid values: ${CCv2DeploymentDatabaseUpdateModeEnum.allowedOptions().joinToString { it.name }}")
        val deployStrategy = CCv2DeploymentStrategy.tryValueOf(strategy.uppercase())
            .takeIf { it != CCv2DeploymentStrategy.UNKNOWN }
            ?: error("Invalid deployment strategy '$strategy'. Valid values: ${CCv2DeploymentStrategy.allowedOptions().joinToString { it.name }}")

        val deploymentCode = CCv2Api.getInstance().deployBuild(apiContext, subscription, environment, build, updateMode, deployStrategy)

        return CCv2OperationResultMcpDto(
            subscription = subscription.presentableName,
            success = true,
            code = deploymentCode,
            message = "Build '$buildCode' deployment to environment '$environmentCode' initiated. Deployment code: '$deploymentCode'.",
        )
    }

    suspend fun getDeploymentStatus(subscriptionId: String?, deploymentCode: String): CCv2DeploymentMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val deployments = coroutineScope {
            reportProgressScope(1) { reporter ->
                CCv2Api.getInstance().fetchDeploymentsForBuild(subscription, deploymentCode, apiContext, reporter)
            }
        }
        return deployments.find { it.code == deploymentCode }
            ?.let { it.mcpDto() }
            ?: error("Deployment '$deploymentCode' not found for subscription '${subscription.presentableName}'.")
    }

    suspend fun listEnvironmentServices(subscriptionId: String?, environmentCode: String): CCv2ServicesMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val services = CCv1Api.getInstance().fetchEnvironmentServices(apiContext, subscription, environment)

        return CCv2ServicesMcpDto(
            subscription = subscription.presentableName,
            environmentCode = environmentCode,
            total = services.size,
            items = services.map { it.mcpDto() },
        )
    }

    suspend fun restartServicePod(
        subscriptionId: String?,
        environmentCode: String,
        serviceCode: String,
        replicaName: String,
    ): CCv2OperationResultMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val services = CCv1Api.getInstance().fetchEnvironmentServices(apiContext, subscription, environment)
        val service = services.find { it.code == serviceCode }
            ?: error("Service '$serviceCode' not found in environment '$environmentCode'. Use sap_commerce_ccv2_list_environment_services to list services.")
        val replica = service.replicas.find { it.name == replicaName }
            ?: error("Replica '$replicaName' not found in service '$serviceCode'. Use sap_commerce_ccv2_list_environment_services to list replicas.")

        CCv1Api.getInstance().restartServiceReplica(apiContext, subscription, environment, service, replica)

        return CCv2OperationResultMcpDto(
            subscription = subscription.presentableName,
            success = true,
            message = "Replica '$replicaName' of service '$serviceCode' in environment '$environmentCode' restart initiated.",
        )
    }

    suspend fun listEnvironmentEndpoints(subscriptionId: String?, environmentCode: String): CCv2EndpointsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val endpoints = CCv2Api.getInstance().fetchEndpoints(apiContext, subscription, environment)
            ?: emptyList()

        return CCv2EndpointsMcpDto(
            subscription = subscription.presentableName,
            environmentCode = environmentCode,
            total = endpoints.size,
            items = endpoints.map { it.mcpDto() },
        )
    }

    suspend fun toggleEndpointMaintenance(
        subscriptionId: String?,
        environmentCode: String,
        endpointCode: String,
    ): CCv2OperationResultMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val endpoints = CCv2Api.getInstance().fetchEndpoints(apiContext, subscription, environment)
            ?: emptyList()
        val endpoint = endpoints.find { it.code == endpointCode }
            ?: error("Endpoint '$endpointCode' not found in environment '$environmentCode'. Use sap_commerce_ccv2_list_environment_endpoints to list endpoints.")

        val payload = EndpointUpdateDTO(maintenanceMode = !endpoint.maintenanceMode)
        CCv2Api.getInstance().updateEndpoint(apiContext, subscription, environment, endpoint, payload)

        val action = if (endpoint.maintenanceMode) "deactivated" else "activated"
        return CCv2OperationResultMcpDto(
            subscription = subscription.presentableName,
            success = true,
            message = "Maintenance mode $action for endpoint '$endpointCode' in environment '$environmentCode'.",
        )
    }

    suspend fun listDataBackups(subscriptionId: String?, environmentCode: String): CCv2DataBackupsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val backups = CCv2Api.getInstance().fetchEnvironmentDataBackups(apiContext, subscription, environment)
            ?.sortedByDescending { it.createdTimestamp }
            ?: emptyList()

        return CCv2DataBackupsMcpDto(
            subscription = subscription.presentableName,
            environmentCode = environmentCode,
            total = backups.size,
            items = backups.map { it.mcpDto() },
        )
    }

    suspend fun listScheduledActivities(subscriptionId: String?, environmentCode: String): CCv2ScheduledActivitiesMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val environment = resolveEnvironment(subscription, apiContext, environmentCode)
        val activities = CCv2Api.getInstance().fetchScheduledActivities(apiContext, subscription, environment)
            ?.sortedByDescending { it.scheduledTimestamp }
            ?: emptyList()

        return CCv2ScheduledActivitiesMcpDto(
            subscription = subscription.presentableName,
            environmentCode = environmentCode,
            total = activities.size,
            items = activities.map { it.mcpDto() },
        )
    }

    suspend fun downloadBuildLogs(subscriptionId: String?, buildCode: String): CCv2BuildLogsMcpDto {
        val (subscription, apiContext) = getApiContext(subscriptionId)
        val build = CCv2Api.getInstance().fetchBuildForCode(apiContext, subscription, buildCode)

        val logZip = withContext(Dispatchers.IO) {
            CCv2Api.getInstance().downloadBuildLogs(apiContext, subscription, build)
        }

        val logFiles = withContext(Dispatchers.IO) {
            val tempDir = Files.createTempDirectory("ccv2_mcp_${buildCode}")
            tempDir.toFile().deleteOnExit()
            com.intellij.util.io.ZipUtil.extract(logZip.toPath(), tempDir, null, true)
            logZip.delete()

            tempDir.toFile()
                .walkTopDown()
                .filter { it.isFile }
                .map { file ->
                    val targetName = file.nameWithoutExtension + ".log"
                    val targetFile = file.resolveSibling(targetName)
                    if (file.name.endsWith(".txt")) file.renameTo(targetFile)
                    val logFile = if (targetFile.exists()) targetFile else file
                    CCv2LogFileMcpDto(
                        name = logFile.name,
                        path = logFile.absolutePath,
                    )
                }
                .toList()
        }

        return CCv2BuildLogsMcpDto(
            subscription = subscription.presentableName,
            buildCode = buildCode,
            files = logFiles,
        )
    }

    fun unscrambleLog(logText: String): CCv2UnscrambleResultMcpDto {
        val service = CCv2UnscrambleService.getInstance()
        val stackTrace = service.buildStackTraceString(logText)
            ?: return CCv2UnscrambleResultMcpDto(
                success = false,
                message = "Input does not contain a recognizable CCv2 JSON exception (missing 'thrown.extendedStackTrace' field).",
            )

        return CCv2UnscrambleResultMcpDto(
            success = true,
            stackTrace = stackTrace,
            message = "Stack trace extracted successfully.",
        )
    }

    suspend fun setCredentials(
        subscriptionId: String?,
        clientId: String,
        clientSecret: String,
    ): CCv2OperationResultMcpDto {
        require(clientId.isNotBlank()) { "clientId must not be blank" }
        require(clientSecret.isNotBlank()) { "clientSecret must not be blank" }

        val settings = CCv2ProjectSettings.getInstance()
        val subscription = if (subscriptionId != null) {
            settings.subscriptions.find { it.id == subscriptionId || it.name == subscriptionId }
                ?: error("Subscription '$subscriptionId' not found. Use sap_commerce_ccv2_list_subscriptions to list available subscriptions.")
        } else null

        val credKey = if (subscription == null) {
            CredentialAttributes(CCv2Constants.SECURE_STORAGE_CCV2_AUTHENTICATION)
        } else {
            CredentialAttributes(CCv2Constants.SECURE_STORAGE_CCV2_AUTHENTICATION + " - " + subscription.uuid)
        }

        withContext(Dispatchers.IO) {
            PasswordSafe.instance[credKey] = Credentials(clientId, clientSecret)
        }

        val scope = subscription?.presentableName ?: "global (shared across all subscriptions)"
        return CCv2OperationResultMcpDto(
            subscription = scope,
            success = true,
            message = "Credentials saved for $scope. clientId='$clientId'.",
        )
    }

    private suspend fun getApiContext(subscriptionId: String?): Pair<CCv2Subscription, ApiContext> {
        val settings = CCv2ProjectSettings.getInstance()
        val subscription = if (subscriptionId != null) {
            settings.subscriptions.find { it.id == subscriptionId || it.name == subscriptionId }
                ?: error(
                    "Subscription '$subscriptionId' not found. " +
                    "Use sap_commerce_ccv2_list_subscriptions to list available subscriptions."
                )
        } else {
            settings.subscriptions.firstOrNull()
                ?: error("No CCv2 subscriptions configured. Add a subscription via CCv2 application settings.")
        }

        val apiContext = withContext(Dispatchers.IO) { resolveApiContext(settings, subscription) }
            ?: error(
                "Failed to obtain a CCv2 API token for subscription '${subscription.presentableName}'. " +
                "Use sap_commerce_ccv2_set_credentials to configure client credentials, " +
                "or verify the token endpoint and resource settings in CCv2 application settings."
            )

        return subscription to apiContext
    }

    private fun resolveApiContext(
        settings: CCv2ProjectSettings,
        subscription: CCv2Subscription,
    ): ApiContext? {
        val apiUrl = settings.kymaApiUrl

        // Try subscription-specific credentials first
        val subscriptionAuth = subscription.authentication
        val subscriptionCredentials = settings.getCCv2Authentication(subscription.uuid)
        if (subscriptionAuth != null && subscriptionCredentials != null) {
            val context = retrieveAuthToken(apiUrl, subscriptionAuth, subscriptionCredentials)
            if (context != null) return context
        }

        // Fall back to global (shared) credentials
        val globalCredentials = settings.getCCv2Authentication() ?: return null
        return retrieveAuthToken(apiUrl, settings.authentication, globalCredentials)
    }

    private fun retrieveAuthToken(
        apiUrl: String,
        auth: CCv2Authentication,
        credentials: Credentials,
    ): ApiContext? {
        val requestBody = mapOf(
            "client_id" to (credentials.userName ?: ""),
            "client_secret" to (credentials.getPasswordAsString() ?: ""),
            "grant_type" to URLEncoder.encode("client_credentials", StandardCharsets.UTF_8),
            "resource" to URLEncoder.encode(auth.resource, StandardCharsets.UTF_8),
        ).entries.joinToString("&") { "${it.key}=${it.value}" }

        return try {
            HttpRequests.post(auth.tokenEndpoint, "application/x-www-form-urlencoded")
                .accept("application/json")
                .connect { request ->
                    request.write(requestBody)
                    val json = Json.parseToJsonElement(request.readString())
                    json.jsonObject["access_token"]?.jsonPrimitive?.content
                        ?.let { KymaApiContext(apiUrl, it) }
                }
        } catch (e: Exception) {
            thisLogger().warn("Failed to retrieve CCv2 auth token", e)
            null
        }
    }

    private suspend fun resolveEnvironment(
        subscription: CCv2Subscription,
        apiContext: ApiContext,
        environmentCode: String,
    ): CCv2EnvironmentDto {
        val statuses = CCv2EnvironmentStatus.entries.filter { it != CCv2EnvironmentStatus.UNKNOWN }.map { it.name }
        val environments = coroutineScope {
            reportProgressScope(1) { reporter ->
                CCv2Api.getInstance().fetchEnvironments(reporter, apiContext, subscription, statuses, false, false)
            }
        }
        return environments.find { it.code == environmentCode }
            ?: error(
                "Environment '$environmentCode' not found for subscription '${subscription.presentableName}'. " +
                "Use sap_commerce_ccv2_list_environments to list available environments."
            )
    }

    companion object {
        fun getInstance(): CCv2McpService = application.service()
    }
}
