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

import sap.commerce.toolset.ccv1.model.EnvironmentHealthDTO
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.model.EnvironmentDetailDTO
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription

data class CCv2EnvironmentDto(
    val code: String,
    val name: String,
    val type: CCv2EnvironmentType,
    val status: CCv2EnvironmentStatus,
    val deploymentStatus: CCv2DeploymentStatusEnum,
    val deploymentAllowed: Boolean = false,
    var deployedBuild: CCv2BuildDto? = null,
    val dynatraceLink: String? = null,
    val loggingLink: String? = null,
    val problems: Int? = null,
    val link: String?,
    val mediaStorages: Collection<CCv2MediaStorageDto>,
    var services: Collection<CCv2ServiceDto>? = null,
    var dataBackups: Collection<CCv2DataBackupDto>? = null,
    var endpoints: Collection<CCv2EndpointDto>? = null,
) : CCv2Dto, Comparable<CCv2EnvironmentDto> {

    val accessible
        get() = dynatraceLink != null
    val order
        get() = when (type) {
            CCv2EnvironmentType.DEV -> 0
            CCv2EnvironmentType.STG -> 1
            CCv2EnvironmentType.PROD -> 2
            CCv2EnvironmentType.UNKNOWN -> 3
        }

    fun canDeploy() = (status in listOf(CCv2EnvironmentStatus.READY_FOR_DEPLOYMENT, CCv2EnvironmentStatus.AVAILABLE))
        && deploymentStatus in listOf(CCv2DeploymentStatusEnum.DEPLOYED, CCv2DeploymentStatusEnum.FAIL)

    override fun compareTo(other: CCv2EnvironmentDto) = name.compareTo(other.name)

    internal data class MappingDto(
        val subscription: CCv2Subscription,
        val environment: EnvironmentDetailDTO,
        var canAccess: Boolean,
        var v1Environment: sap.commerce.toolset.ccv1.model.EnvironmentDetailDTO? = null,
        var v1EnvironmentHealth: EnvironmentHealthDTO? = null,
    )

    companion object {
        internal fun map(mappingDto: MappingDto): CCv2EnvironmentDto {
            val environment = mappingDto.environment
            val canAccess = mappingDto.canAccess
            val v1Environment = mappingDto.v1Environment
            val v1EnvironmentHealth = mappingDto.v1EnvironmentHealth
            val status = CCv2EnvironmentStatus.tryValueOf(environment.status)
            val code = environment.code

            val link = if (v1Environment != null && status == CCv2EnvironmentStatus.AVAILABLE)
                "https://${CCv2Constants.DOMAIN}/subscription/${environment.subscriptionCode}/applications/commerce-cloud/environments/$code"
            else null

            val mediaStorages = (v1Environment
                ?.mediaStorages
                ?.map { CCv2MediaStorageDto.map(environment, it) }
                ?: emptyList())

            return CCv2EnvironmentDto(
                code = code ?: "N/A",
                name = environment.name ?: "N/A",
                status = status,
                type = CCv2EnvironmentType.tryValueOf(environment.type),
                deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(environment.deploymentStatus),
                deploymentAllowed = canAccess && (status == CCv2EnvironmentStatus.AVAILABLE || status == CCv2EnvironmentStatus.READY_FOR_DEPLOYMENT) && link != null,
                dynatraceLink = v1Environment?.dynatraceUrl,
                loggingLink = v1Environment?.loggingUrl?.let { "$it/app/discover" },
                problems = v1EnvironmentHealth?.problems,
                link = link,
                mediaStorages = mediaStorages,
            )
        }
    }
}

