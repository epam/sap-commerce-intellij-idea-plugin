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

import com.intellij.idea.plugin.hybris.ccv2.model.EnvironmentDetailDTO
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import javax.swing.Icon
import com.intellij.idea.plugin.hybris.ccv1.model.EnvironmentDetailDTO as EnvironmentDetailV1DTO
import com.intellij.idea.plugin.hybris.ccv1.model.EnvironmentHealthDTO as EnvironmentHealthV1DTO

data class CCv2Environment(
    val code: String,
    val name: String,
    val type: CCv2EnvironmentType,
    val status: CCv2EnvironmentStatus,
    val deploymentStatus: CCv2DeploymentStatusEnum,
    val deploymentAllowed: Boolean = false,
    var deployedBuild: CCv2Build? = null,
    val dynatraceLink: String? = null,
    val loggingLink: String? = null,
    val problems: Int? = null,
    val link: String?,
    val mediaStorages: Collection<CCv2MediaStorage>,
    var services: Collection<CCv2EnvironmentService>? = null,
) : CCv2DTO, Comparable<CCv2Environment> {

    override fun compareTo(other: CCv2Environment) = name.compareTo(other.name)

    companion object {

        fun map(
            environment: EnvironmentDetailDTO,
            deploymentStatus: Boolean,
            v1Environment: EnvironmentDetailV1DTO?,
            v1EnvironmentHealth: EnvironmentHealthV1DTO?
        ): CCv2Environment {
            val status = CCv2EnvironmentStatus.tryValueOf(environment.status)
            val code = environment.code

            val link = if (v1Environment != null && status == CCv2EnvironmentStatus.AVAILABLE)
                "https://${HybrisConstants.CCV2_DOMAIN}/subscription/${environment.subscriptionCode}/applications/commerce-cloud/environments/$code"
            else null

            val mediaStorages = (v1Environment
                ?.mediaStorages
                ?.map { CCv2MediaStorage.map(it) }
                ?: emptyList())

            return CCv2Environment(
                code = code ?: "N/A",
                name = environment.name ?: "N/A",
                status = status,
                type = CCv2EnvironmentType.tryValueOf(environment.type),
                deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(environment.deploymentStatus),
                deploymentAllowed = deploymentStatus && (status == CCv2EnvironmentStatus.AVAILABLE || status == CCv2EnvironmentStatus.READY_FOR_DEPLOYMENT),
                dynatraceLink = v1Environment?.dynatraceUrl,
                loggingLink = v1Environment?.loggingUrl?.let { "$it/app/discover" },
                problems = v1EnvironmentHealth?.problems,
                link = link,
                mediaStorages = mediaStorages
            )
        }
    }
}

enum class CCv2EnvironmentType(val title: String, val icon: Icon) {
    DEV("Development", HybrisIcons.CCV2_ENV_ENVIRONMENT_TYPE_DEV),
    STG("Staging", HybrisIcons.CCV2_ENV_ENVIRONMENT_TYPE_STG),
    PROD("Production", HybrisIcons.CCV2_ENV_ENVIRONMENT_TYPE_PROD),
    UNKNOWN("Unknown", HybrisIcons.CCV2_ENV_ENVIRONMENT_TYPE_UNKNOWN);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}

enum class CCv2EnvironmentStatus(val title: String, val icon: Icon) {
    PROVISIONING("Provisioning", HybrisIcons.CCV2_ENV_STATUS_PROVISIONING),
    AVAILABLE("Available", HybrisIcons.CCV2_ENV_STATUS_AVAILABLE),
    TERMINATING("Terminating", HybrisIcons.CCV2_ENV_STATUS_TERMINATING),
    TERMINATED("Terminated", HybrisIcons.CCV2_ENV_STATUS_TERMINATED),
    READY_FOR_DEPLOYMENT("Ready for deployment", HybrisIcons.CCV2_ENV_STATUS_READY_FOR_DEPLOYMENT),
    UNKNOWN("Unknown", HybrisIcons.CCV2_ENV_STATUS_UNKNOWN);

    companion object {
        fun tryValueOf(name: String?) = CCv2EnvironmentStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}
