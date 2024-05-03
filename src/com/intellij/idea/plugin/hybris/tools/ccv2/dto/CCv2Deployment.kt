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

import com.intellij.idea.plugin.hybris.ccv2.model.CreateDeploymentRequestDTO
import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentDetailDTO
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Util
import java.time.OffsetDateTime
import javax.swing.Icon

data class CCv2Deployment(
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
) : CCv2DTO {

    val createdTimeFormatted
        get() = CCv2Util.formatTime(createdTime)

    val deployedTimeFormatted
        get() = CCv2Util.formatTime(deployedTime)
}

enum class CCv2DeploymentDatabaseUpdateModeEnum(val title: String, val icon: Icon, val apiMode: CreateDeploymentRequestDTO.DatabaseUpdateMode) {
    NONE("None", HybrisIcons.CCV2_DEPLOYMENT_UPDATE_MODE_NONE, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE),
    UPDATE("Update", HybrisIcons.CCV2_DEPLOYMENT_UPDATE_MODE_UPDATE, CreateDeploymentRequestDTO.DatabaseUpdateMode.UPDATE),
    INITIALIZE("Initialize", HybrisIcons.CCV2_DEPLOYMENT_UPDATE_MODE_INIT, CreateDeploymentRequestDTO.DatabaseUpdateMode.INITIALIZE),
    UNKNOWN("Unknown", HybrisIcons.CCV2_DEPLOYMENT_UPDATE_MODE_UNKNOWN, CreateDeploymentRequestDTO.DatabaseUpdateMode.NONE);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN

        fun tryValueOf(mode: DeploymentDetailDTO.DatabaseUpdateMode?) = entries
            .find { it.name == mode?.name }
            ?: UNKNOWN
    }
}

enum class CCv2DeploymentStrategyEnum(val title: String, val icon: Icon, val apiStrategy: CreateDeploymentRequestDTO.Strategy) {
    ROLLING_UPDATE("Rolling update", HybrisIcons.CCV2_DEPLOYMENT_STRATEGY_ROLLING_UPDATE, CreateDeploymentRequestDTO.Strategy.ROLLING_UPDATE),
    RECREATE("Recreate", HybrisIcons.CCV2_DEPLOYMENT_STRATEGY_RECREATE, CreateDeploymentRequestDTO.Strategy.RECREATE),
    GREEN("Blue / Green", HybrisIcons.CCV2_DEPLOYMENT_STRATEGY_GREEN, CreateDeploymentRequestDTO.Strategy.GREEN),
    UNKNOWN("Unknown", HybrisIcons.CCV2_DEPLOYMENT_STRATEGY_UNKNOWN, CreateDeploymentRequestDTO.Strategy.ROLLING_UPDATE);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN

        fun tryValueOf(strategy: DeploymentDetailDTO.Strategy?) = entries
            .find { it.name == strategy?.name }
            ?: UNKNOWN
    }
}

enum class CCv2DeploymentStatusEnum(val title: String, val icon: Icon) {
    SCHEDULED("Scheduled", HybrisIcons.CCV2_DEPLOYMENT_STATUS_SCHEDULED),
    DEPLOYED("Deployed", HybrisIcons.CCV2_DEPLOYMENT_STATUS_DEPLOYED),
    DEPLOYING("Deploying", HybrisIcons.CCV2_DEPLOYMENT_STATUS_DEPLOYING),
    UNDEPLOYED("Undeployed", HybrisIcons.CCV2_DEPLOYMENT_STATUS_UNDEPLOYED),
    FAIL("Fail", HybrisIcons.CCV2_DEPLOYMENT_STATUS_FAIL),
    UNKNOWN("Unknown", HybrisIcons.CCV2_DEPLOYMENT_STATUS_UNKNOWN);

    companion object {
        fun tryValueOf(name: String?) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}
