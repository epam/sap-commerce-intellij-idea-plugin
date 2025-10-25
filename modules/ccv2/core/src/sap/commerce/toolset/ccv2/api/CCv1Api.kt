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
import com.intellij.util.application
import sap.commerce.toolset.ccv1.api.EndpointApi
import sap.commerce.toolset.ccv1.api.EnvironmentApi
import sap.commerce.toolset.ccv1.api.PermissionsApi
import sap.commerce.toolset.ccv1.api.ServiceApi
import sap.commerce.toolset.ccv1.invoker.infrastructure.ApiClient
import sap.commerce.toolset.ccv1.model.*
import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.model.EnvironmentDetailDTO
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import java.util.concurrent.TimeUnit

@Service
class CCv1Api {

    private val apiClient by lazy {
        ApiClient.builder
            .readTimeout(CCv2ProjectSettings.getInstance().readTimeout.toLong(), TimeUnit.SECONDS)
            .build()
    }
    private val environmentApi by lazy { EnvironmentApi(client = apiClient) }
    private val permissionsApi by lazy { PermissionsApi(client = apiClient) }
    private val serviceApi by lazy { ServiceApi(client = apiClient) }
    private val endpointApi by lazy { EndpointApi(client = apiClient) }

    suspend fun fetchPermissions(
        accessToken: String
    ): List<PermissionDTO>? = permissionsApi
        .getPermissions(requestHeaders = createRequestParams(accessToken))
        .permissionDTOS

    suspend fun fetchEnvironment(
        accessToken: String,
        v2Environment: EnvironmentDetailDTO
    ): EnvironmentDTO? {
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
        v2Environment: EnvironmentDetailDTO
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
        .map { CCv2ServiceDto.MappingDto(subscription, environment, it) }
        .map { CCv2ServiceDto.map(it) }

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

    suspend fun updateEndpointsMaintenanceMode(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        endpoint: CCv2EndpointDto,
        maintenanceMode: Boolean
    ) = endpointApi.updateEndpointsMaintenanceMode(
        subscriptionCode = subscription.id!!,
        environmentCode = environment.code,
        endpointsUpdateDTO = EndpointsUpdateDTO(
            listOf(EndpointUpdateDTO(endpoint.code, endpoint.webProxy, maintenanceMode))
        ),
        requestHeaders = createRequestParams(accessToken)
    )

    private fun createRequestParams(ccv2Token: String) = mapOf("Authorization" to "Bearer $ccv2Token")

    companion object {
        fun getInstance(): CCv1Api = application.service()
    }

}