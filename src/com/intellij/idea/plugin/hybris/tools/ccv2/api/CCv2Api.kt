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

package com.intellij.idea.plugin.hybris.tools.ccv2.api

import com.intellij.idea.plugin.hybris.ccv2.api.BuildApi
import com.intellij.idea.plugin.hybris.ccv2.api.DeploymentApi
import com.intellij.idea.plugin.hybris.ccv2.api.EnvironmentApi
import com.intellij.idea.plugin.hybris.ccv2.api.ServicePropertiesApi
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv2.model.CreateBuildRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.CreateDeploymentRequestDTO
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.utils.flatMapToNullableSet
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class CCv2Api {

    suspend fun fetchEnvironments(
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>,
        statuses: List<String>
    ): SortedMap<CCv2Subscription, Collection<CCv2EnvironmentDto>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2EnvironmentDto>>()
        val ccv1Api = CCv1Api.getInstance()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                val subscriptions2Permissions = ccv1Api.fetchPermissions(ccv2Token)
                    ?.associateBy { it.scopeName }
                    ?: return@coroutineScope null

                subscriptions.forEach { subscription ->
                    launch {
                        result[subscription] = progressReporter.sizedStep(1, "Fetching Environments for subscription: $subscription") {
                            val subscriptionCode = subscription.id!!

                            val subscriptionPermissions = subscriptions2Permissions[subscriptionCode]
                                ?: return@sizedStep emptyList()

                            statuses
                                .map { status ->
                                    async {
                                        EnvironmentApi(client = client)
                                            .getEnvironments(subscriptionCode, status = status)
                                    }
                                }
                                .awaitAll()
                                .flatMapToNullableSet { it.value }
                                ?.map { env ->
                                    val canAccess = subscriptionPermissions.environments.contains(env.code)
                                    async {
                                        val v1Env = if (canAccess) ccv1Api.fetchEnvironment(ccv2Token, env) else null
                                        val v1EnvHealth = if (canAccess) ccv1Api.fetchEnvironmentHealth(ccv2Token, env) else null

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
                }
            }
        }

        return result
    }

    suspend fun fetchEnvironmentsBuilds(
        ccv2Token: String,
        subscriptions: Map<CCv2Subscription, Collection<CCv2EnvironmentDto>>
    ) {
        val environments = subscriptions.values.flatten()

        reportProgress(environments.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach { (subscription, environments) ->
                    environments.forEach { environment ->
                        launch {
                            progressReporter.sizedStep(1, "Fetching Deployment details for ${environment.name} of the $subscription") {
                                fetchEnvironmentBuild(ccv2Token, subscription, environment)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun fetchEnvironmentBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
    ): CCv2BuildDto? {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val subscriptionCode = subscription.id!!

        return DeploymentApi(client = client).getDeployments(
            subscriptionCode,
            environmentCode = environment.code,
            dollarCount = true,
            dollarTop = 1,
        )
            .value
            ?.firstOrNull()
            ?.buildCode
            ?.let { fetchBuildForCode(ccv2Token, subscription, it) }
            ?.also { build -> environment.deployedBuild = build }
    }

    suspend fun fetchBuildForCode(
        ccv2Token: String,
        subscription: CCv2Subscription,
        buildCode: String
    ): CCv2BuildDto {
        ApiClient.accessToken = ccv2Token
        val subscriptionCode = subscription.id!!
        val client = createClient()

        return BuildApi(client = client)
            .getBuild(subscriptionCode, buildCode)
            .let { CCv2BuildDto.map(it) }
    }

    suspend fun fetchBuilds(
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>,
        statusNot: List<String>?,
    ): SortedMap<CCv2Subscription, Collection<CCv2BuildDto>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2BuildDto>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = progressReporter.sizedStep(1, "Fetching Builds for subscription: $it") {
                            BuildApi(client = client)
                                .getBuilds(it.id!!, dollarTop = 20, statusNot = statusNot)
                                .value
                                ?.map { build -> CCv2BuildDto.map(build) }
                                ?: emptyList()
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun fetchDeployments(
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2DeploymentDto>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2DeploymentDto>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = progressReporter.sizedStep(1, "Fetching Deployments for subscription: $it") {
                            val subscriptionCode = it.id!!
                            DeploymentApi(client = client)
                                .getDeployments(subscriptionCode, dollarTop = 20)
                                .value
                                ?.map { deployment ->
                                    val code = deployment.code
                                    val environmentCode = deployment.environmentCode
                                    val link = if (environmentCode != null && code != null)
                                        "https://${HybrisConstants.CCV2_DOMAIN}/subscription/$subscriptionCode/applications/commerce-cloud/environments/$environmentCode/deployments/$code"
                                    else null

                                    CCv2DeploymentDto(
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
                                }
                                ?: emptyList()
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun createBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        name: String,
        branch: String
    ): String {
        ApiClient.accessToken = ccv2Token

        return BuildApi(client = createClient())
            .createBuild(subscription.id!!, CreateBuildRequestDTO(branch, name))
            .code
    }

    suspend fun deleteBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ) {
        ApiClient.accessToken = ccv2Token

        BuildApi(client = createClient())
            .deleteBuild(subscription.id!!, build.code)
    }

    suspend fun deployBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        build: CCv2BuildDto,
        mode: CCv2DeploymentDatabaseUpdateModeEnum,
        strategy: CCv2DeploymentStrategyEnum
    ): String {
        ApiClient.accessToken = ccv2Token

        val request = CreateDeploymentRequestDTO(
            buildCode = build.code,
            environmentCode = environment.code,
            databaseUpdateMode = mode.apiMode,
            strategy = strategy.apiStrategy
        )
        return DeploymentApi(client = createClient())
            .createDeployment(subscription.id!!, request)
            .code
    }

    suspend fun downloadBuildLogs(
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2BuildDto
    ): File {
        ApiClient.accessToken = ccv2Token

        return BuildApi(client = createClient())
            .getBuildLogs(subscription.id!!, build.code)
    }

    suspend fun fetchServiceProperties(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
    ): Map<String, String>? {
        ApiClient.accessToken = ccv2Token
        val subscriptionCode = subscription.id ?: return null

        return ServicePropertiesApi(client = createClient())
            .getProperty(
                subscriptionCode,
                environment.code,
                service.code,
                HybrisConstants.CCV2_SERVICE_CUSTOMER_PROPERTIES_CODE
            )
            .value
            .let { it as? String }
            ?.split("\n")
            ?.map { propertySeparatorRegex.split(it, 2) }
            ?.filter { it.size == 2 }
            ?.associate { it[0] to it[1] }
    }

    private fun createClient() = ApiClient.builder
        .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
        .build()

    companion object {
        fun getInstance(): CCv2Api = ApplicationManager.getApplication().getService(CCv2Api::class.java)
        private val propertySeparatorRegex = Regex("=")
    }

}