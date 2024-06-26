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
import com.intellij.idea.plugin.hybris.ccv1.invoker.infrastructure.ApiClient
import com.intellij.idea.plugin.hybris.ccv1.model.EnvironmentDetailDTO
import com.intellij.idea.plugin.hybris.ccv1.model.EnvironmentHealthDTO
import com.intellij.idea.plugin.hybris.ccv1.model.MediaStoragePublicKeyDTO
import com.intellij.idea.plugin.hybris.ccv1.model.PermissionDTO
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2MediaStorageDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.util.concurrent.TimeUnit
import com.intellij.idea.plugin.hybris.ccv2.model.EnvironmentDetailDTO as V2EnvironmentDetailDTO

@Service
class CCv1Api {

    suspend fun fetchPermissions(
        accessToken: String
    ): List<PermissionDTO>? {
        ApiClient.accessToken = accessToken
        val client = createClient()

        return PermissionsApi(client = client)
            .getPermissions()
            .permissionDTOS
    }

    suspend fun fetchEnvironment(
        accessToken: String,
        v2Environment: V2EnvironmentDetailDTO
    ): EnvironmentDetailDTO? {
        ApiClient.accessToken = accessToken
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null
        val client = createClient()

        return EnvironmentApi(client = client)
            .getEnvironment(subscriptionCode, environmentCode)
    }

    suspend fun fetchEnvironmentHealth(
        accessToken: String,
        v2Environment: V2EnvironmentDetailDTO
    ): EnvironmentHealthDTO? {
        ApiClient.accessToken = accessToken
        val subscriptionCode = v2Environment.subscriptionCode ?: return null
        val environmentCode = v2Environment.code ?: return null
        val client = createClient()

        return EnvironmentApi(client = client)
            .getEnvironmentHealth(subscriptionCode, environmentCode)
    }

    suspend fun fetchMediaStoragePublicKey(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto,
        mediaStorage: CCv2MediaStorageDto,
    ): MediaStoragePublicKeyDTO? {
        ApiClient.accessToken = accessToken

        val subscriptionCode = subscription.id ?: return null
        val client = createClient()

        return EnvironmentApi(client = client)
            .getMediaStoragePublicKey(
                subscriptionCode,
                environment.code,
                mediaStorage.code,
            )
    }

    suspend fun fetchEnvironmentServices(
        accessToken: String,
        subscription: CCv2Subscription,
        environment: CCv2EnvironmentDto
    ): Collection<CCv2ServiceDto> {
        ApiClient.accessToken = accessToken

        val subscriptionCode = subscription.id ?: return emptyList()
        val client = createClient()

        return EnvironmentApi(client = client)
            .getEnvironmentServices(
                subscriptionCode,
                environment.code,
            )
            .map { CCv2ServiceDto.map(subscription, environment, it) }
    }

    private fun createClient() = ApiClient.builder
        .readTimeout(ApplicationSettingsComponent.getInstance().state.ccv2ReadTimeout.toLong(), TimeUnit.SECONDS)
        .build()

    companion object {
        fun getInstance(): CCv1Api = ApplicationManager.getApplication().getService(CCv1Api::class.java)
    }

}