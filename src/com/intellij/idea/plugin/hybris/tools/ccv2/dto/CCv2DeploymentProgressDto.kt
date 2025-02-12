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

import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentProgressDTO
import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentProgressStageDTO
import com.intellij.idea.plugin.hybris.ccv2.model.DeploymentProgressStepDTO
import java.time.OffsetDateTime

data class CCv2DeploymentProgressDto(
    val subscriptionCode: String,
    val deploymentCode: String,
    val deploymentStatus: String,
    val percentage: Int,
    val stages: Collection<CCv2DeploymentProgressStageDto>,
) : CCv2DTO {

    companion object {
        fun map(progress: DeploymentProgressDTO) = CCv2DeploymentProgressDto(
            subscriptionCode = progress.subscriptionCode ?: "N/A",
            deploymentCode = progress.deploymentCode ?: "N/A",
            deploymentStatus = progress.deploymentStatus ?: "N/A",
            percentage = progress.percentage ?: 0,
            stages = progress.stages
                ?.map { CCv2DeploymentProgressStageDto.map(it) }
                ?: emptyList(),
        )

    }
}

data class CCv2DeploymentProgressStageDto(
    val name: String,
    val type: String,
    val status: String,
    val logLink: String,
    val startTimestamp: OffsetDateTime?,
    val endTimestamp: OffsetDateTime?,
    val steps: Collection<CCv2DeploymentProgressStepDto>,
) {
    companion object {
        fun map(progress: DeploymentProgressStageDTO) = CCv2DeploymentProgressStageDto(
            name = progress.name ?: "N/A",
            type = progress.type ?: "N/A",
            status = progress.status ?: "N/A",
            logLink = progress.logLink ?: "N/A",
            startTimestamp = progress.startTimestamp,
            endTimestamp = progress.endTimestamp,
            steps =  progress.steps
                ?.map { CCv2DeploymentProgressStepDto.map(it) }
                ?: emptyList(),
        )
    }
}

data class CCv2DeploymentProgressStepDto(
    val code: String,
    val name: String,
    val status: String,
    val message: String,
    val startTimestamp: OffsetDateTime?,
    val endTimestamp: OffsetDateTime?,
) {
    companion object {
        fun map(progress: DeploymentProgressStepDTO) = CCv2DeploymentProgressStepDto(
            code = progress.code ?: "N/A",
            name = progress.name ?: "N/A",
            status = progress.status ?: "N/A",
            message = progress.message ?: "N/A",
            startTimestamp = progress.startTimestamp,
            endTimestamp = progress.endTimestamp
        )
    }
}