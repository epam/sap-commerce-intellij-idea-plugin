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

package com.intellij.idea.plugin.hybris.tools.ccv2.dto

import com.intellij.idea.plugin.hybris.ccv2.model.CreateDeploymentRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentDetailDTO
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import java.time.OffsetDateTime
import javax.swing.Icon

data class CCv2DeploymentDto(
    val code: String,
    val createdBy: String,
    val createdTime: OffsetDateTime?,
    val buildCode: String,
    val envCode: String,
    val updateMode: CCv2DeploymentDatabaseUpdateModeEnum,
    val strategy: CCv2DeploymentStrategyEnum,
    val scheduledTime: OffsetDateTime?,
    val deployedTime: OffsetDateTime?,
    val failedTime: OffsetDateTime?,
    val undeployedTime: OffsetDateTime?,
    val status: CCv2DeploymentStatusEnum,
    val link: String?,
) : CCv2DTO {
    fun canTrack() = status == CCv2DeploymentStatusEnum.DEPLOYING || status == CCv2DeploymentStatusEnum.SCHEDULED
}

enum class CCv2DeploymentDatabaseUpdateModeEnum(val title: String, val icon: Icon, val apiMode: CreateDeploymentRequestDTO.DatabaseUpdateMode) {
    NONE("None", HybrisIcons.CCv2.Deployment.UpdateMode.NONE, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE),
    UPDATE("Update", HybrisIcons.CCv2.Deployment.UpdateMode.UPDATE, CreateDeploymentRequestDTO.DatabaseUpdateMode.UPDATE),
    INITIALIZE("Initialize", HybrisIcons.CCv2.Deployment.UpdateMode.INIT, CreateDeploymentRequestDTO.DatabaseUpdateMode.INITIALIZE),
    UNKNOWN("Unknown", HybrisIcons.CCv2.Deployment.UpdateMode.UNKNOWN, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN

        fun tryValueOf(mode: DeploymentDetailDTO.DatabaseUpdateMode?) = entries
            .find { it.name == mode?.name }
            ?: UNKNOWN

        fun allowedOptions() = listOf(NONE, UPDATE, INITIALIZE)
    }
}

enum class CCv2DeploymentStrategyEnum(val title: String, val icon: Icon, val apiStrategy: CreateDeploymentRequestDTO.Strategy) {
    ROLLING_UPDATE("Rolling update", HybrisIcons.CCv2.Deployment.Strategy.ROLLING_UPDATE, CreateDeploymentRequestDTO.Strategy.ROLLING_UPDATE),
    RECREATE("Recreate", HybrisIcons.CCv2.Deployment.Strategy.RECREATE, CreateDeploymentRequestDTO.Strategy.RECREATE),
    GREEN("Blue / Green", HybrisIcons.CCv2.Deployment.Strategy.GREEN, CreateDeploymentRequestDTO.Strategy.GREEN),
    UNKNOWN("Unknown", HybrisIcons.CCv2.Deployment.Strategy.UNKNOWN, CreateDeploymentRequestDTO.Strategy.ROLLING_UPDATE);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN

        fun tryValueOf(strategy: DeploymentDetailDTO.Strategy?) = entries
            .find { it.name == strategy?.name }
            ?: UNKNOWN

        fun allowedOptions() = listOf(ROLLING_UPDATE, RECREATE, GREEN)
    }
}

enum class CCv2DeploymentStatusEnum(val title: String, val icon: Icon) {
    SCHEDULED("Scheduled", HybrisIcons.CCv2.Deployment.Status.SCHEDULED),
    DEPLOYED("Deployed", HybrisIcons.CCv2.Deployment.Status.DEPLOYED),
    DEPLOYING("Deploying", HybrisIcons.CCv2.Deployment.Status.DEPLOYING),
    UNDEPLOYED("Undeployed", HybrisIcons.CCv2.Deployment.Status.UNDEPLOYED),
    FAIL("Fail", HybrisIcons.CCv2.Deployment.Status.FAIL),
    UNKNOWN("Unknown", HybrisIcons.CCv2.Deployment.Status.UNKNOWN);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}

data class CCv2DeploymentRequest(
    val environment: CCv2EnvironmentDto,
    val mode: CCv2DeploymentDatabaseUpdateModeEnum,
    val strategy: CCv2DeploymentStrategyEnum,
    val deploy: Boolean,
    val track: Boolean
)
