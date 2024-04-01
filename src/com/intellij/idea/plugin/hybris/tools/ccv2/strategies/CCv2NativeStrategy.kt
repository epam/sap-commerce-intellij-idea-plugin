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

import com.intellij.idea.plugin.hybris.ccv2.api.EnvironmentApi
import com.intellij.idea.plugin.hybris.ccv2.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Service
class CCv2NativeStrategy : CCv2Strategy {

    override suspend fun fetchEnvironments(
        project: Project,
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2Environment>> {
        ApiClient.accessToken = ccv2Token
        val result = sortedMapOf<CCv2Subscription, Collection<CCv2Environment>>()

        reportProgress(3) {
            coroutineScope {
                subscriptions.forEach {
                    launch {
                        result[it] = EnvironmentApi().getEnvironments(it.id!!).value
                            ?.map { environment ->
                                CCv2Environment(
                                    code = environment.code ?: "N/A",
                                    name = environment.name ?: "N/A",
                                    status = CCv2EnvironmentStatus.tryValueOf(environment.status),
                                    type = CCv2EnvironmentType.tryValueOf(environment.type),
                                    deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(environment.deploymentStatus),
                                )
                            }
                            ?: emptyList()
                    }
                }
            }
        }

        return result
    }

    override suspend fun fetchBuilds(
        project: Project,
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2Build>> {
        ApiClient.accessToken = ccv2Token
        return sortedMapOf()
    }

    override suspend fun fetchDeployments(
        project: Project,
        ccv2Token: String,
        subscriptions: Collection<CCv2Subscription>
    ): SortedMap<CCv2Subscription, Collection<CCv2Deployment>> {
        ApiClient.accessToken = ccv2Token
        return sortedMapOf()
    }

    override suspend fun createBuild(
        project: Project,
        ccv2Token: String,
        subscription: CCv2Subscription,
        name: String,
        branch: String
    ): CCv2Build? {
        ApiClient.accessToken = ccv2Token
        return null
    }

    override suspend fun deleteBuild(
        project: Project,
        ccv2Token: String,
        subscription: CCv2Subscription,
        build: CCv2Build
    ) {
        ApiClient.accessToken = ccv2Token
    }

}