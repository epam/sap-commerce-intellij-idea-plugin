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

package com.intellij.idea.plugin.hybris.tools.ccv2.dto

import com.intellij.idea.plugin.hybris.ccv1.model.ServiceDTO
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.util.asSafely
import java.time.OffsetDateTime
import java.util.*

data class CCv2ServiceDto(
    val code: String,
    val name: String,
    val modifiedBy: String,
    val modifiedTime: OffsetDateTime,
    val customerScalableSupported: Boolean,
    val runnable: Boolean,
    val desiredReplicas: Int?,
    val availableReplicas: Int?,
    val link: String,
    val replicas: Collection<CCv2ServiceReplicaDto>,
    val supportedProperties: EnumSet<CCv2ServiceProperties> = CCv2ServiceProperties.getSupportedProperties(code),
    var customerProperties: Map<String, String>? = null,
    var securityProperties: Map<String, String>? = null,
    var initialPasswords: Map<String, String>? = null,
    var greenDeploymentSupported: Boolean? = null,
) {

    companion object {
        fun map(subscription: CCv2Subscription, environment: CCv2EnvironmentDto, dto: ServiceDTO) = CCv2ServiceDto(
            code = dto.code,
            name = dto.name,
            modifiedBy = dto.modifiedBy,
            modifiedTime = dto.modifiedTime,
            customerScalableSupported = dto.customerScalableSupported,
            runnable = dto.runnable,
            desiredReplicas = dto.desiredReplicas,
            availableReplicas = dto.availableReplicas,
            link = "https://${HybrisConstants.CCV2_DOMAIN}/subscription/${subscription.id!!}/applications/commerce-cloud/environments/${environment.code}/services/${dto.code}/replicas",
            replicas = dto.replicas
                ?.map { CCv2ServiceReplicaDto.map(it) }
                ?: emptyList()
        )
    }
}

enum class CCv2ServiceProperties(val title: String, val id: String, val documentationUrl: String) {
    CUSTOMER_PROPERTIES(
        "Service Properties",
        "customer-properties",
        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/bf789103937b40ce95161b5b6be5f7ad.html"
    ) {
        override fun parseResponse(apiResponse: Any) = apiResponse
            .asSafely<String>()
            ?.split("\n")
            ?.map { propertySeparatorRegex.split(it, 2) }
            ?.filter { it.size == 2 }
            ?.associate { it[0] to it[1] }
    },

    INITIAL_PASSWORDS(
        "Initial Passwords",
        "initialpassword",
        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/6dc5b06e1daa4f0baec0c5d045873780.html"
    ) {
        override fun parseResponse(apiResponse: Any) = apiResponse
            .asSafely<List<Map<String, String>>>()
            ?.mapNotNull {
                val uid = it["uid"] ?: return@mapNotNull null
                val password = it["password"] ?: return@mapNotNull null

                uid to password
            }
            ?.associate { it.first to it.second }
    },

    GREEN_DEPLOYMENT_SUPPORTED(
        "Deploying a Green Preview Build",
        "green-deployment-supported",
        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/8d53fa2b3e764586b329dfcf2ffd7168.html"
    ) {
        override fun parseResponse(apiResponse: Any) = apiResponse
            .asSafely<Boolean>()
            ?.let { mapOf(GREEN_DEPLOYMENT_SUPPORTED_KEY to it.toString()) }
    },

    // Not Supported, added for reference
    SECURITY_PROPERTIES(
        "Encryption Keys",
        "security-properties",
        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/f2c88b83a0794f13a2249e136684f324.html"
    ) {
        override fun parseResponse(apiResponse: Any) = null
    };

    abstract fun parseResponse(apiResponse: Any): Map<String, String>?

    companion object {
        private val propertySeparatorRegex = Regex("=")
        const val GREEN_DEPLOYMENT_SUPPORTED_KEY = "green_deployment_supported"

        // Details here: https://me.sap.com/notes/0003383335
        fun getSupportedProperties(serviceCode: String): EnumSet<CCv2ServiceProperties> = when (serviceCode) {
            "hcs_admin" -> EnumSet.of(INITIAL_PASSWORDS, CUSTOMER_PROPERTIES)

            "hcs_common" -> EnumSet.of(CUSTOMER_PROPERTIES)

            "hcs_platform_accstorefront",
            "hcs_platform_api",
            "hcs_js_apps" -> EnumSet.of(CUSTOMER_PROPERTIES, GREEN_DEPLOYMENT_SUPPORTED)

            "hcs_platform_backgroundProcessing",
            "hcs_platform_backoffice",
            "hcs_datahub" -> EnumSet.of(CUSTOMER_PROPERTIES)

            else -> EnumSet.noneOf(CCv2ServiceProperties::class.java)
        }
    }
}
