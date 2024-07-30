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

import com.intellij.idea.plugin.hybris.ccv1.api.EnvironmentApi
import com.intellij.idea.plugin.hybris.ccv1.api.PermissionsApi
import com.intellij.idea.plugin.hybris.ccv1.api.ServiceApi
import com.intellij.idea.plugin.hybris.ccv1.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv1.model.*
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2MediaStorageDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.util.concurrent.TimeUnit
import com.intellij.idea.plugin.hybris.ccv2.model.EnvironmentDetailDTO as V2EnvironmentDetailDTO

@Service
class CCv1Api {

    private val apiClient by lazy {
        ApiClient.builder
            .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
            .build()
    }
    private val environmentApi by lazy { EnvironmentApi(client = apiClient) }
    private val permissionsApi by lazy { PermissionsApi(client = apiClient) }
    private val serviceApi by lazy { ServiceApi(client = apiClient) }

    suspend fun fetchPermissions(
        accessToken: String
    ): List<PermissionDTO>? = permissionsApi
        .getPermissions(requestHeaders = createRequestParams(accessToken))
        .permissionDTOS

    suspend fun fetchEnvironment(
        accessToken: String,
        v2Environment: V2EnvironmentDetailDTO
    ): EnvironmentDetailDTO? {
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null

        return environmentApi
            .getEnvironment(
                subscriptionCode = subscriptionCode,
                environmentCode = environmentCode,
                requestHeaders = createRequestParams(accessToken)
            )
    }

    suspend fun fetchEnvironmentHealth(
        accessToken: String,
        v2Environment: V2EnvironmentDetailDTO
    ): EnvironmentHealthDTO? {
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null

        return environmentApi
            .getEnvironmentHealth(
                subscriptionCode = subscriptionCode,
                environmentCode = environmentCode,
                requestHeaders = createRequestParams(accessToken)
            )
    }

    suspend fun fetchMediaStoragePublicKey(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        mediaStorage: CCv2MediaStorageDto,
    ): MediaStoragePublicKeyDTO = environmentApi
        .getMediaStoragePublicKey(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            mediaStorageCode = mediaStorage.code,
            requestHeaders = createRequestParams(accessToken)
        )

    suspend fun fetchEnvironmentServices(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2ServiceDto> = environmentApi
        .getEnvironmentServices(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            requestHeaders = createRequestParams(accessToken)
        )
        .map { CCv2ServiceDto.map(subscription, environment, it) }

    suspend fun restartServiceReplica(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
        replica: CCv2ServiceReplicaDto
    ): ServiceReplicaStatusDTO = serviceApi
        .restartReplica(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            serviceCode = service.code,
            replicaName = replica.name,
            requestHeaders = createRequestParams(accessToken)
        )

    private fun createRequestParams(ccv2Token: String) = mapOf("Authorization" to "Bearer $ccv2Token")

    companion object {
        fun getInstance(): CCv1Api = ApplicationManager.getApplication().getService(CCv1Api::class.java)
    }

}