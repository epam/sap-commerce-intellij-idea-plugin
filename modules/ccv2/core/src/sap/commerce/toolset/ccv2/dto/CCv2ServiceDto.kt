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

package sap.commerce.toolset.ccv2.dto

import sap.commerce.toolset.ccv1.model.ServiceDTO
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
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
            link = "https://${CCv2Constants.DOMAIN}/subscription/${subscription.id!!}/applications/commerce-cloud/environments/${environment.code}/services/${dto.code}/replicas",
            replicas = dto.replicas
                ?.map { CCv2ServiceReplicaDto.map(it) }
                ?: emptyList()
        )
    }
}
