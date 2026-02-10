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

package sap.commerce.toolset.ccv2.api

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.util.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.invoker.infrastructure.ApiClient
import sap.commerce.toolset.ccv2.model.*
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Service
class CCv2Api {

    private val apiClient by lazy {
        ApiClient.builder
            .readTimeout(CCv2ProjectSettings.getInstance().readTimeout.toLong(), TimeUnit.SECONDS)
            .build()
    }

    private val _api = mutableMapOf<String, ApiClient>()

    private fun <T : ApiClient> api(apiContext: ApiContext, kClass: KClass<T>) = _api
        .getOrPut(apiContext.apiUrl + "_$kClass") {
            val basePath = apiContext.apiUrl.removeSuffix("/") + "/v2"
            when (kClass) {
                EnvironmentApi::class -> EnvironmentApi(basePath = basePath, client = apiClient)
                EnvironmentScalingApi::class -> EnvironmentScalingApi(basePath = basePath, client = apiClient)
                EndpointApi::class -> EndpointApi(basePath = basePath, client = apiClient)
                DeploymentApi::class -> DeploymentApi(basePath = basePath, client = apiClient)
                BuildApi::class -> BuildApi(basePath = basePath, client = apiClient)
                ServicePropertiesApi::class -> ServicePropertiesApi(basePath = basePath, client = apiClient)
                DatabackupApi::class -> DatabackupApi(basePath = basePath, client = apiClient)
                else -> throw IllegalArgumentException("${kClass.simpleName} is not supported")
            }

        }
        .let { kClass.cast(it) }

    suspend fun fetchEnvironmentScaling(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ) = api(apiContext, EnvironmentScalingApi::class).getEnvironmentScalingDetail(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        requestHeaders = createRequestParams(apiContext),
    )

    suspend fun fetchEnvironments(
        progressReporter: ProgressReporter,
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        statuses: List<String>,
        requestV1Details: Boolean,
        requestV1Health: Boolean,
    ): Collection<CCv2EnvironmentDto> {

        val ccv1Api = CCv1Api.getInstance()

        val subscriptions2Permissions = ccv1Api.fetchPermissions(apiContext)
            ?.associateBy { it.scopeName }
            ?: return emptyList()

        val subscriptionCode = subscription.id!!

        val subscriptionPermissions = subscriptions2Permissions["SYSTEM"]
            ?.takeIf { it.permissions.contains("access_environments") }
            ?: subscriptions2Permissions[subscriptionCode]
            ?: return emptyList()

        return progressReporter.sizedStep(1, "Fetching Environments for subscription: ${subscription.presentableName}") {
            statuses
                .map { status ->
                    async {
                        checkCanceled()
                        api(apiContext, EnvironmentApi::class).getEnvironments(
                            subscriptionCode = subscriptionCode,
                            status = status,
                            requestHeaders = createRequestParams(apiContext)
                        )
                    }
                }
                .awaitAll()
                .mapNotNull { it.value }
                .flatten()
                .mapNotNull { env ->
                    val environmentCode = env.code ?: return@mapNotNull null

                    async {
                        checkCanceled()
                        val canAccess = subscriptionPermissions.environments?.contains(environmentCode) ?: true
                        CCv2EnvironmentDto.MappingDto(subscription, env, canAccess).apply {
                            listOf(
                                async { this@apply.v1Environment = if (requestV1Details) getV1Environment(canAccess, ccv1Api, apiContext, env) else null },
                                async { this@apply.v1EnvironmentHealth = if (requestV1Health) getV1EnvironmentHealth(canAccess, ccv1Api, apiContext, env) else null },
                            ).awaitAll()
                        }
                    }
                }
                .awaitAll()
                .map { CCv2EnvironmentDto.map(it) }
        }
    }

    suspend fun fetchEndpoints(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ) = api(apiContext, EndpointApi::class).getEndpoints(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        requestHeaders = createRequestParams(apiContext)
    )
        .value
        ?.map { CCv2EndpointDto.MappingDto(subscription, environment, it) }
        ?.map { CCv2EndpointDto.map(it) }

    suspend fun fetchEnvironmentDataBackups(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ) = api(apiContext, DatabackupApi::class).getDatabackups(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        requestHeaders = createRequestParams(apiContext)
    )
        .value
        ?.map { CCv2DataBackupDto.map(it) }

    fun fetchEnvironmentsBuilds(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environments: Collection<CCv2EnvironmentDto>,
        coroutineScope: CoroutineScope,
        progressReporter: ProgressReporter
    ) {
        environments.forEach { environment ->
            coroutineScope.launch {
                progressReporter.sizedStep(1, "Fetching Deployment details for ${environment.name} of the ${subscription.presentableName}") {
                    runCatching {
                        fetchEnvironmentBuild(apiContext, subscription, environment)
                    }
                }
            }
        }
    }

    suspend fun fetchEnvironmentBuild(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ): CCv2BuildDto? = api(apiContext, DeploymentApi::class).getDeployments(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        dollarCount = true,
        dollarTop = 1,
        requestHeaders = createRequestParams(apiContext)
    )
        .value
        ?.firstOrNull()
        ?.buildCode
        ?.let { fetchBuildForCode(apiContext, subscription, it) }
        ?.also { build -> environment.deployedBuild = build }

    suspend fun fetchBuildForCode(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        buildCode: String
    ): CCv2BuildDto = api(apiContext, BuildApi::class).getBuild(
        subscriptionCode = subscription.id!!,
        buildCode = buildCode,
        requestHeaders = createRequestParams(apiContext)
    )
        .let { CCv2BuildDto.map(it) }

    suspend fun fetchBuilds(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        statusNot: List<String>?,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Builds for subscription: ${subscription.presentableName}") {
        api(apiContext, BuildApi::class)
            .getBuilds(
                subscriptionCode = subscription.id!!,
                dollarTop = 20,
                statusNot = statusNot,
                requestHeaders = createRequestParams(apiContext)
            )
            .value
            ?.map { build -> CCv2BuildDto.map(build) }
            ?: emptyList()
    }

    suspend fun fetchDeployments(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Deployments for subscription: ${subscription.presentableName}") {
        val subscriptionCode = subscription.id!!

        api(apiContext, DeploymentApi::class)
            .getDeployments(
                subscriptionCode = subscriptionCode,
                dollarTop = 20,
                requestHeaders = createRequestParams(apiContext)
            )
            .value
            ?.map { deployment ->
                val code = deployment.code
                val environmentCode = deployment.environmentCode
                val link = if (environmentCode != null && code != null)
                    "https://${CCv2Constants.DOMAIN}/subscription/$subscriptionCode/applications/commerce-cloud/environments/$environmentCode/deployments/$code"
                else null

                ccv2DeploymentDto(code, deployment, environmentCode, link)
            }
            ?: emptyList()
    }

    suspend fun fetchDeploymentsForBuild(
        subscription: CCv2Subscription,
        buildCode: String,
        apiContext: ApiContext,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Deployments for subscription: ${subscription.presentableName}") {

        api(apiContext, DeploymentApi::class)
            .getDeployments(subscription.id!!, buildCode, dollarTop = 20, requestHeaders = createRequestParams(apiContext))
            .value
            ?.map { deployment ->
                val code = deployment.code
                val environmentCode = deployment.environmentCode
                val link = if (environmentCode != null && code != null)
                    "https://${CCv2Constants.DOMAIN}/subscription/${subscription.id}/applications/commerce-cloud/environments/$environmentCode/deployments/$code"
                else null

                ccv2DeploymentDto(code, deployment, environmentCode, link)
            }
            ?: emptyList()
    }

    suspend fun fetchBuildProgress(
        subscription: CCv2Subscription,
        buildCode: String,
        apiContext: ApiContext,
        progressReporter: ProgressReporter
    ) = progressReporter.indeterminateStep("Fetching the Build progress...") {
        api(apiContext, BuildApi::class)
            .getBuildProgress(subscription.id!!, buildCode, requestHeaders = createRequestParams(apiContext))
            .let { CCv2BuildProgressDto.map(it) }
    }

    suspend fun fetchDeploymentProgress(
        subscription: CCv2Subscription,
        deploymentCode: String,
        apiContext: ApiContext,
        progressReporter: ProgressReporter
    ) = progressReporter.indeterminateStep("Fetching the Deployment progress...") {
        api(apiContext, DeploymentApi::class)
            .getDeploymentProgress(subscription.id!!, deploymentCode, requestHeaders = createRequestParams(apiContext))
            .let { CCv2DeploymentProgressDto.map(it) }
    }

    suspend fun createBuild(
        apiContext: ApiContext,
        buildRequest: CCv2BuildRequest
    ): String = api(apiContext, BuildApi::class)
        .createBuild(
            subscriptionCode = buildRequest.subscription.id!!,
            createBuildRequestDTO = CreateBuildRequestDTO(buildRequest.branch, buildRequest.name),
            requestHeaders = createRequestParams(apiContext)
        )
        .code

    suspend fun deleteBuild(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ) = api(apiContext, BuildApi::class).deleteBuild(
        subscriptionCode = subscription.id!!,
        buildCode = build.code,
        requestHeaders = createRequestParams(apiContext)
    )

    suspend fun deployBuild(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        build: CCv2BuildDto,
        mode: CCv2DeploymentDatabaseUpdateModeEnum,
        strategy: CCv2DeploymentStrategyEnum
    ): String {
        val request = CreateDeploymentRequestDTO(
            buildCode = build.code,
            environmentCode = environment.code,
            databaseUpdateMode = mode.apiMode,
            strategy = strategy.apiStrategy
        )
        return api(apiContext, DeploymentApi::class)
            .createDeployment(
                subscriptionCode = subscription.id!!,
                createDeploymentRequestDTO = request,
                requestHeaders = createRequestParams(apiContext)
            )
            .code
    }

    suspend fun downloadBuildLogs(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ): File = api(apiContext, BuildApi::class)
        .getBuildLogs(
            subscriptionCode = subscription.id!!,
            buildCode = build.code,
            requestHeaders = createRequestParams(apiContext)
        )

    suspend fun fetchServiceProperties(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
        serviceProperties: CCv2ServiceProperties
    ): Map<String, String>? = api(apiContext, ServicePropertiesApi::class)
        .getProperty(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            serviceCode = service.code,
            propertyCode = serviceProperties.id,
            requestHeaders = createRequestParams(apiContext)
        )
        .value
        .let { serviceProperties.parseResponse(it) }

    suspend fun updateEndpoint(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        endpoint: CCv2EndpointDto,
        endpointUpdateDTO: EndpointUpdateDTO,
    ): EndpointDTO = api(apiContext, EndpointApi::class).updateEndpoint(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        endpointCode = endpoint.code,
        endpointUpdateDTO = endpointUpdateDTO,
        requestHeaders = createRequestParams(apiContext),
    )

    suspend fun deleteEndpoint(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        endpoint: CCv2EndpointDto,
    ) = api(apiContext, EndpointApi::class).deleteEndpoint(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        endpointCode = endpoint.code,
        requestHeaders = createRequestParams(apiContext),
    )

    private fun ccv2DeploymentDto(
        code: String?,
        deployment: DeploymentDetailDTO,
        environmentCode: String?,
        link: String?
    ) = CCv2DeploymentDto(
        code = code ?: "N/A",
        createdBy = deployment.createdBy ?: "N/A",
        createdTime = deployment.createdTimestamp,
        buildCode = deployment.buildCode ?: "N/A",
        envCode = environmentCode ?: "N/A",
        updateMode = CCv2DeploymentDatabaseUpdateModeEnum.tryValueOf(deployment.databaseUpdateMode),
        strategy = CCv2DeploymentStrategyEnum.tryValueOf(deployment.strategy),
        scheduledTime = deployment.scheduledTimestamp,
        deployedTime = deployment.deployedTimestamp,
        failedTime = deployment.failedTimestamp,
        undeployedTime = deployment.undeployedTimestamp,
        status = CCv2DeploymentStatusEnum.tryValueOf(deployment.status),
        link = link
    )

    private fun createRequestParams(apiContext: ApiContext) = mapOf(apiContext.authHeader to "Bearer ${apiContext.authToken}")

    private suspend fun getV1Environment(
        canAccess: Boolean,
        ccv1Api: CCv1Api,
        apiContext: ApiContext,
        env: EnvironmentDetailDTO
    ) = if (canAccess) {
        try {
            ccv1Api.fetchEnvironment(apiContext, env)
        } catch (e: Exception) {
            thisLogger().warn(e)
            null
        }
    } else null

    private suspend fun getV1EnvironmentHealth(
        canAccess: Boolean,
        ccv1Api: CCv1Api,
        apiContext: ApiContext,
        env: EnvironmentDetailDTO
    ) = if (canAccess) {
        try {
            ccv1Api.fetchEnvironmentHealth(apiContext, env)
        } catch (e: Exception) {
            thisLogger().warn(e)
            null
        }
    } else null

    companion object {
        fun getInstance(): CCv2Api = application.service()
    }

}