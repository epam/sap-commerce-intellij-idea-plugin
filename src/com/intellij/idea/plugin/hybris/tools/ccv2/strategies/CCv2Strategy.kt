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

package com.intellij.idea.plugin.hybris.tools.ccv2.strategies

import com.intellij.idea.plugin.hybris.ccv2.api.BuildApi
import com.intellij.idea.plugin.hybris.ccv2.api.DeploymentApi
import com.intellij.idea.plugin.hybris.ccv2.api.EndpointApi
import com.intellij.idea.plugin.hybris.ccv2.api.EnvironmentApi
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ClientException
import com.intellij.idea.plugin.hybris.ccv2.model.CreateBuildRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.CreateDeploymentRequestDTO
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
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class CCv2Strategy {

    suspend fun fetchEnvironments(
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2Environment>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2Environment>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach { subscription ->
                    launch {
                        result[subscription] = progressReporter.sizedStep(1, "Fetching Environments for subscription: $subscription") {
                            val subscriptionCode = subscription.id!!
                            EnvironmentApi(client = client).getEnvironments(subscriptionCode).value
                                ?.map { env ->
                                    val environmentCode = env.code ?: "N/A"
                                    async {
                                        val deploymentAllowed = EndpointApi(client = client)
                                            .getEndpointsWithHttpInfo(subscriptionCode, environmentCode, null, null)
                                            .statusCode == 200
                                        val v1Env = try {
                                            EnvironmentApi(basePath = "https://portalrotapi.hana.ondemand.com/v1", client = client)
                                                .getEnvironmentV1(subscriptionCode, environmentCode)
                                        } catch (e: ClientException) {
                                            // user may not have access to the environment
                                            null
                                        }
                                        val v1EnvHealth = try {
                                            EnvironmentApi(basePath = "https://portalrotapi.hana.ondemand.com/v1", client = client)
                                                .getEnvironmentHealthV1(subscriptionCode, environmentCode)
                                        } catch (e: ClientException) {
                                            // user may not have access to the environment
                                            null
                                        }
                                        env to Triple(deploymentAllowed, v1Env, v1EnvHealth)
                                    }
                                }
                                ?.awaitAll()
                                ?.map { (environment, details) ->
                                    val status = CCv2EnvironmentStatus.tryValueOf(environment.status)
                                    val code = environment.code
                                    val deploymentStatus = details.first
                                    val v1Environment = details.second
                                    val v1EnvironmentHealth = details.third

                                    val link = if (v1Environment != null && status == CCv2EnvironmentStatus.AVAILABLE)
                                        "https://portal.commerce.ondemand.com/subscription/$subscriptionCode/applications/commerce-cloud/environments/$code"
                                    else null

                                    CCv2Environment(
                                        code = code ?: "N/A",
                                        name = environment.name ?: "N/A",
                                        status = status,
                                        type = CCv2EnvironmentType.tryValueOf(environment.type),
                                        deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(environment.deploymentStatus),
                                        deploymentAllowed = deploymentStatus && (status == CCv2EnvironmentStatus.AVAILABLE || status == CCv2EnvironmentStatus.READY_FOR_DEPLOYMENT),
                                        dynatraceLink = v1Environment?.dynatraceUrl,
                                        loggingLink = v1Environment?.loggingUrl?.let { "$it/app/discover" },
                                        problems = v1EnvironmentHealth?.problems,
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

    suspend fun fetchEnvironmentsBuilds(
        ccv2Token: String,
        subscriptions: Map<CCv2Subscription, Collection<CCv2Environment>>
    ) {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val environments = subscriptions.values.flatten()

        reportProgress(environments.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach { (subscription, environments) ->
                    val subscriptionCode = subscription.id!!
                    environments.forEach { environment ->
                        launch {
                            progressReporter.sizedStep(1, "Fetching Deployment details for ${environment.name} of the $subscription") {
                                DeploymentApi(client = client).getDeployments(
                                    subscriptionCode,
                                    environmentCode = environment.code,
                                    dollarCount = true,
                                    dollarTop = 1,
                                )
                                    .value
                                    ?.firstOrNull()
                                    ?.buildCode
                                    ?.let { BuildApi(client = client).getBuild(subscriptionCode, it) }
                                    ?.let { build -> CCv2Build.map(build) }
                                    ?.also { build -> environment.deployedBuild = build }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun fetchBuilds(
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>,
        statusNot: List<String>?,
    ): SortedMap<CCv2Subscription, Collection<CCv2Build>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2Build>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = progressReporter.sizedStep(1, "Fetching Builds for subscription: $it") {
                            BuildApi(client = client)
                                .getBuilds(it.id!!, dollarTop = 20, statusNot = statusNot)
                                .value
                                ?.map { build -> CCv2Build.map(build) }
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
    ): SortedMap<CCv2Subscription, Collection<CCv2Deployment>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2Deployment>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = progressReporter.sizedStep(1, "Fetching Deployments for subscription: $it") {
                            DeploymentApi(client = client)
                                .getDeployments(it.id!!, dollarTop = 20)
                                .value
                                ?.map { deployment ->
                                    CCv2Deployment(
                                        code = deployment.code ?: "N/A",
                                        createdBy = deployment.createdBy ?: "N/A",
                                        createdTime = deployment.createdTimestamp,
                                        buildCode = deployment.buildCode ?: "N/A",
                                        envCode = deployment.environmentCode ?: "N/A",
                                        updateMode = CCv2DeploymentDatabaseUpdateModeEnum.tryValueOf(deployment.databaseUpdateMode),
                                        strategy = CCv2DeploymentStrategyEnum.tryValueOf(deployment.strategy),
                                        scheduledTime = deployment.scheduledTimestamp,
                                        deployedTime = deployment.deployedTimestamp,
                                        failedTime = deployment.failedTimestamp,
                                        undeployedTime = deployment.undeployedTimestamp,
                                        status = CCv2DeploymentStatusEnum.tryValueOf(deployment.status)
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
        build: CCv2Build
    ) {
        ApiClient.accessToken = ccv2Token

        BuildApi(client = createClient())
            .deleteBuild(subscription.id!!, build.code)
    }

    suspend fun deployBuild(
        ccv2Token: String,
        subscription: CCv2Subscription,
        environment: CCv2Environment,
        build: CCv2Build,
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
        build: CCv2Build
    ): File {
        ApiClient.accessToken = ccv2Token

        return BuildApi(client = createClient())
            .getBuildLogs(subscription.id!!, build.code)
    }

    private fun createClient() = ApiClient.builder
        .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
        .build()

    companion object {
        fun getInstance(): CCv2Strategy = ApplicationManager.getApplication().getService(CCv2Strategy::class.java)
    }

}