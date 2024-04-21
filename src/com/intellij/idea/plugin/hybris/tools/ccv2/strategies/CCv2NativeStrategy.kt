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

import com.github.weisj.jsvg.de
import com.intellij.idea.plugin.hybris.ccv2.api.*
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv2.model.*
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class CCv2NativeStrategy : CCv2Strategy {

    override suspend fun fetchEnvironments(
        project: Project,
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2Environment>> {
        ApiClient.accessToken = ccv2Token
        val client = createClient()
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2Environment>>()

        reportProgress(subscriptions.size) { progressReporter ->
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = progressReporter.sizedStep(1, "Fetching Environments for subscription: $it") {
                            val subscriptionCode = it.id!!
                            EnvironmentApi(client = client).getEnvironments(subscriptionCode).value
                                ?.map { env ->
                                    val environmentCode = env.code ?: "N/A"
                                    async {
                                        val deploymentAllowed = EndpointApi(client = client).getEndpointsWithHttpInfo(subscriptionCode, environmentCode, null, null)
                                            .statusCode == 200
                                        env to deploymentAllowed
                                    }
                                }
                                ?.awaitAll()
                                ?.map { (environment, deploymentAllowed) ->
                                    val status = CCv2EnvironmentStatus.tryValueOf(environment.status)
                                    CCv2Environment(
                                        code = environment.code ?: "N/A",
                                        name = environment.name ?: "N/A",
                                        status = status,
                                        type = CCv2EnvironmentType.tryValueOf(environment.type),
                                        deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(environment.deploymentStatus),
                                        deploymentAllowed = deploymentAllowed && (status == CCv2EnvironmentStatus.AVAILABLE || status == CCv2EnvironmentStatus.READY_FOR_DEPLOYMENT)
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

    override suspend fun fetchEnvironmentsBuilds(
        project: Project,
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

    override suspend fun fetchBuilds(
        project: Project,
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>,
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
                                .getBuilds(it.id!!, dollarTop = 20)
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

    override suspend fun fetchDeployments(
        project: Project,
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
                                        createdTime = deployment.createdTimestamp
                                            ?.toString() ?: "N/A",
                                        buildCode = deployment.buildCode ?: "N/A",
                                        envCode = deployment.environmentCode ?: "N/A",
                                        updateMode = CCv2DeploymentDatabaseUpdateModeEnum.tryValueOf(deployment.databaseUpdateMode),
                                        strategy = CCv2DeploymentStrategyEnum.tryValueOf(deployment.strategy),
                                        scheduledTime = deployment.scheduledTimestamp?.toString() ?: "N/A",
                                        deployedTime = deployment.deployedTimestamp?.toString() ?: "N/A",
                                        failedTime = deployment.failedTimestamp?.toString() ?: "N/A",
                                        undeployedTime = deployment.undeployedTimestamp?.toString() ?: "N/A",
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

    override suspend fun createBuild(
        project: Project,
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

    override suspend fun deleteBuild(
        project: Project,
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2Build
    ) {
        ApiClient.accessToken = ccv2Token

        BuildApi(client = createClient())
            .deleteBuild(subscription.id!!, build.code)
    }

    override suspend fun deployBuild(
        project: Project,
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

    private fun createClient() = ApiClient.builder
        .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
        .build()

}