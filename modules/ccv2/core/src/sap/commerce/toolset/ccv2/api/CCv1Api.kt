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

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.ccv1.api.EnvironmentApi
import sap.commerce.toolset.ccv1.api.PermissionsApi
import sap.commerce.toolset.ccv1.api.ServiceApi
import sap.commerce.toolset.ccv1.api.SubscriptionApi
import sap.commerce.toolset.ccv1.invoker.infrastructure.ApiClient
import sap.commerce.toolset.ccv1.model.*
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.dto.CCv2MediaStorageDto
import sap.commerce.toolset.ccv2.dto.CCv2ServiceDto
import sap.commerce.toolset.ccv2.dto.CCv2ServiceReplicaDto
import sap.commerce.toolset.ccv2.model.EnvironmentDetailDTO
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Service
class CCv1Api : Disposable {

    private val apiClient by lazy {
        ApiClient.builder
            .readTimeout(CCv2ProjectSettings.getInstance().readTimeout.toLong(), TimeUnit.SECONDS)
            .build()
    }

    private val _api = mutableMapOf<String, ApiClient>()

    private fun <T : ApiClient> api(apiContext: ApiContext, kClass: KClass<T>) = _api
        .getOrPut(apiContext.apiUrl + "_$kClass") {
            val basePath = apiContext.apiUrl.removeSuffix("/") + "/v1"
            when (kClass) {
                SubscriptionApi::class -> SubscriptionApi(basePath = basePath, client = apiClient)
                EnvironmentApi::class -> EnvironmentApi(basePath = basePath, client = apiClient)
                PermissionsApi::class -> PermissionsApi(basePath = basePath, client = apiClient)
                ServiceApi::class -> ServiceApi(basePath = basePath, client = apiClient)
                else -> throw IllegalArgumentException("${kClass.simpleName} cannot be applied to $apiContext")
            }

        }
        .let { kClass.cast(it) }

    suspend fun fetchSubscriptions(
        apiContext: ApiContext,
    ): List<SubscriptionDTO> = api(apiContext, SubscriptionApi::class)
        .getSubscriptions(
            dollarTop = 100,
            requestHeaders = createRequestParams(apiContext)
        )
        .value

    suspend fun fetchPermissions(
        apiContext: ApiContext
    ): List<PermissionDTO>? = api(apiContext, PermissionsApi::class)
        .getPermissions(requestHeaders = createRequestParams(apiContext))
        .permissionDTOS

    suspend fun fetchEnvironment(
        apiContext: ApiContext,
        v2Environment: EnvironmentDetailDTO
    ): EnvironmentDTO? {
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null

        return api(apiContext, EnvironmentApi::class)
            .getEnvironment(
                subscriptionCode = subscriptionCode,
                environmentCode = environmentCode,
                requestHeaders = createRequestParams(apiContext)
            )
    }

    suspend fun fetchEnvironmentHealth(
        apiContext: ApiContext,
        v2Environment: EnvironmentDetailDTO
    ): EnvironmentHealthDTO? {
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null

        return api(apiContext, EnvironmentApi::class)
            .getEnvironmentHealth(
                subscriptionCode = subscriptionCode,
                environmentCode = environmentCode,
                requestHeaders = createRequestParams(apiContext)
            )
    }

    suspend fun fetchMediaStoragePublicKey(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        mediaStorage: CCv2MediaStorageDto,
    ): MediaStoragePublicKeyDTO = api(apiContext, EnvironmentApi::class)
        .getMediaStoragePublicKey(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            mediaStorageCode = mediaStorage.code,
            requestHeaders = createRequestParams(apiContext)
        )

    suspend fun fetchEnvironmentServices(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2ServiceDto> = api(apiContext, EnvironmentApi::class)
        .getEnvironmentServices(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            requestHeaders = createRequestParams(apiContext)
        )
        .map { CCv2ServiceDto.MappingDto(subscription, environment, it) }
        .map { CCv2ServiceDto.map(it) }

    suspend fun restartServiceReplica(
        apiContext: ApiContext,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        service: CCv2ServiceDto,
        replica: CCv2ServiceReplicaDto
    ): ServiceReplicaStatusDTO = api(apiContext, ServiceApi::class)
        .restartReplica(
            subscriptionCode = subscription.id!!,
            environmentCode = environment.code,
            serviceCode = service.code,
            replicaName = replica.name,
            requestHeaders = createRequestParams(apiContext)
        )

    private fun createRequestParams(apiContext: ApiContext) = mapOf(
        apiContext.authHeader to "Bearer ${apiContext.authToken}"
    )

    override fun dispose() = _api.clear()

    companion object {
        fun getInstance(): CCv1Api = application.service()
    }

}