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

package com.intellij.idea.plugin.hybris.tools.ccv2.api

import com.intellij.idea.plugin.hybris.ccv1.invoker.infrastructure.ClientException
import com.intellij.idea.plugin.hybris.ccv2.api.*
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv2.model.CreateBuildRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.CreateDeploymentRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentDetailDTO
import com.intellij.idea.plugin.hybris.ccv2.model.EnvironmentDetailDTO
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.platform.util.progress.ProgressReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.utils.flatMapToNullableSet
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class CCv2Api {

    private val apiClient by lazy {
        ApiClient.builder
            .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
            .build()
    }
    private val environmentApi by lazy { EnvironmentApi(client = apiClient) }
    private val deploymentApi by lazy { DeploymentApi(client = apiClient) }
    private val buildApi by lazy { BuildApi(client = apiClient) }
    private val servicePropertiesApi by lazy { ServicePropertiesApi(client = apiClient) }
    private val databackupApi by lazy { DatabackupApi(client = apiClient) }

    suspend fun fetchEnvironments(
        ccv2Token: String,
        subscription: CCv2Subscription,
        statuses: List<String>,
        progressReporter: ProgressReporter
    ): Collection<CCv2EnvironmentDto> {

        val ccv1Api = CCv1Api.getInstance()

        val subscriptions2Permissions = ccv1Api.fetchPermissions(ccv2Token)
            ?.associateBy { it.scopeName }
            ?: return emptyList()

        val subscriptionCode = subscription.id!!

        val subscriptionPermissions = subscriptions2Permissions[subscriptionCode]
            ?: return emptyList()

        return progressReporter.sizedStep(1, "Fetching Environments for subscription: $subscription") {
            statuses
                .map { status ->
                    async {
                        environmentApi.getEnvironments(
                            subscriptionCode = subscriptionCode,
                            status = status,
                            requestHeaders = createRequestParams(ccv2Token)
                        )
                    }
                }
                .awaitAll()
                .flatMapToNullableSet { it.value }
                ?.map { env ->
                    val canAccess = subscriptionPermissions.environments?.contains(env.code) ?: true
                    async {
                        val v1Env = getV1Environment(canAccess, ccv1Api, ccv2Token, env)
                        val v1EnvHealth = getV1EnvironmentHealth(canAccess, ccv1Api, ccv2Token, env)

                        env to Triple(canAccess, v1Env, v1EnvHealth)
                    }
                }
                ?.awaitAll()
                ?.map { (environment, details) ->
                    CCv2EnvironmentDto.map(environment, details.first, details.second, details.third)
                }
                ?: emptyList()
        }
    }

    suspend fun fetchEnvironmentDataBackups(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ) = databackupApi.getDatabackups(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        requestHeaders = createRequestParams(ccv2Token)
    )
        .value
        ?.map { CCv2DataBackupDto.map(it) }
        ?: emptyList()

    fun fetchEnvironmentsBuilds(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environments: Collection<CCv2EnvironmentDto>,
        coroutineScope: CoroutineScope,
        progressReporter: ProgressReporter
    ) {
        environments.forEach { environment ->
            coroutineScope.launch {
                progressReporter.sizedStep(1, "Fetching Deployment details for ${environment.name} of the $subscription") {
                    fetchEnvironmentBuild(ccv2Token, subscription, environment)
                }
            }
        }
    }

    suspend fun fetchEnvironmentBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ): CCv2BuildDto? = deploymentApi.getDeployments(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        dollarCount = true,
        dollarTop = 1,
        requestHeaders = createRequestParams(ccv2Token)
    )
        .value
        ?.firstOrNull()
        ?.buildCode
        ?.let { fetchBuildForCode(ccv2Token, subscription, it) }
        ?.also { build -> environment.deployedBuild = build }

    suspend fun fetchBuildForCode(
        ccv2Token: String,
        subscription: CCv2Subscription,
        buildCode: String
    ): CCv2BuildDto = buildApi.getBuild(
        subscriptionCode = subscription.id!!,
        buildCode = buildCode,
        requestHeaders = createRequestParams(ccv2Token)
    )
        .let { CCv2BuildDto.map(it) }

    suspend fun fetchBuilds(
        ccv2Token: String,
        subscription: CCv2Subscription,
        statusNot: List<String>?,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Builds for subscription: $subscription") {
        buildApi
            .getBuilds(
                subscriptionCode = subscription.id!!,
                dollarTop = 20,
                statusNot = statusNot,
                requestHeaders = createRequestParams(ccv2Token)
            )
            .value
            ?.map { build -> CCv2BuildDto.map(build) }
            ?: emptyList()
    }

    suspend fun fetchDeployments(
        ccv2Token: String,
        subscription: CCv2Subscription,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Deployments for subscription: $subscription") {
        val subscriptionCode = subscription.id!!

        deploymentApi
            .getDeployments(
                subscriptionCode = subscriptionCode,
                dollarTop = 20,
                requestHeaders = createRequestParams(ccv2Token)
            )
            .value
            ?.map { deployment ->
                val code = deployment.code
                val environmentCode = deployment.environmentCode
                val link = if (environmentCode != null && code != null)
                    "https://${HybrisConstants.CCV2_DOMAIN}/subscription/$subscriptionCode/applications/commerce-cloud/environments/$environmentCode/deployments/$code"
                else null

                cCv2DeploymentDto(code, deployment, environmentCode, link)
            }
            ?: emptyList()
    }

    suspend fun fetchDeploymentsForBuild(
        subscription: CCv2Subscription,
        buildCode: String,
        ccv2Token: String,
        progressReporter: ProgressReporter
    ) = progressReporter.sizedStep(1, "Fetching Deployments for subscription: $subscription") {

        deploymentApi
            .getDeployments(subscription.id!!, buildCode, dollarTop = 20, requestHeaders = createRequestParams(ccv2Token))
            .value
            ?.map { deployment ->
                val code = deployment.code
                val environmentCode = deployment.environmentCode
                val link = if (environmentCode != null && code != null)
                    "https://${HybrisConstants.CCV2_DOMAIN}/subscription/${subscription.id}/applications/commerce-cloud/environments/$environmentCode/deployments/$code"
                else null

                cCv2DeploymentDto(code, deployment, environmentCode, link)
            }
            ?: emptyList()
    }

    suspend fun fetchBuildProgress(
        subscription: CCv2Subscription,
        buildCode: String,
        ccv2Token: String,
        progressReporter: ProgressReporter
    ) = progressReporter.indeterminateStep("Fetching the Build progress...") {
        buildApi
            .getBuildProgress(subscription.id!!, buildCode, requestHeaders = createRequestParams(ccv2Token))
            .let { CCv2BuildProgressDto.map(it) }
    }

    suspend fun fetchDeploymentProgress(
        subscription: CCv2Subscription,
        deploymentCode: String,
        ccv2Token: String,
        progressReporter: ProgressReporter
    ) = progressReporter.indeterminateStep("Fetching the Deployment progress...") {
        deploymentApi
            .getDeploymentProgress(subscription.id!!, deploymentCode, requestHeaders = createRequestParams(ccv2Token))
            .let { CCv2DeploymentProgressDto.map(it) }
    }

    suspend fun createBuild(
        ccv2Token: String,
        buildRequest: CCv2BuildRequest
    ): String = buildApi
        .createBuild(
            subscriptionCode = buildRequest.subscription.id!!,
            createBuildRequestDTO = CreateBuildRequestDTO(buildRequest.branch, buildRequest.name),
            requestHeaders = createRequestParams(ccv2Token)
        )
        .code

    suspend fun deleteBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ) = buildApi.deleteBuild(
        subscriptionCode = subscription.id!!,
        buildCode = build.code,
        requestHeaders = createRequestParams(ccv2Token)
    )

    suspend fun deployBuild(
        ccv2Token: String,
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
        return deploymentApi
            .createDeployment(
                subscriptionCode = subscription.id!!,
                createDeploymentRequestDTO = request,
                requestHeaders = createRequestParams(ccv2Token)
            )
            .code
    }

    suspend fun downloadBuildLogs(
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ): File = buildApi
        .getBuildLogs(
            subscriptionCode = subscription.id!!,
            buildCode = build.code,
            requestHeaders = createRequestParams(ccv2Token)
        )

    suspend fun fetchServiceProperties(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
        serviceProperties: CCv2ServiceProperties
    ): Map<String, String>? = servicePropertiesApi
        .getProperty(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            serviceCode = service.code,
            propertyCode = serviceProperties.id,
            requestHeaders = createRequestParams(ccv2Token)
        )
        .value
        .let { serviceProperties.parseResponse(it) }

    private fun cCv2DeploymentDto(
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

    private fun createRequestParams(ccv2Token: String) = mapOf("Authorization" to "Bearer $ccv2Token")

    private suspend fun getV1Environment(
        canAccess: Boolean,
        ccv1Api: CCv1Api,
        ccv2Token: String,
        env: EnvironmentDetailDTO
    ) = if (canAccess) {
        try {
            ccv1Api.fetchEnvironment(ccv2Token, env)
        } catch (e: ClientException) {
            null
        }
    } else null

    private suspend fun getV1EnvironmentHealth(
        canAccess: Boolean,
        ccv1Api: CCv1Api,
        ccv2Token: String,
        env: EnvironmentDetailDTO
    ) = if (canAccess) {
        try {
            ccv1Api.fetchEnvironmentHealth(ccv2Token, env)
        } catch (e: ClientException) {
            null
        }
    } else null

    companion object {
        fun getInstance(): CCv2Api = ApplicationManager.getApplication().getService(CCv2Api::class.java)
    }

}