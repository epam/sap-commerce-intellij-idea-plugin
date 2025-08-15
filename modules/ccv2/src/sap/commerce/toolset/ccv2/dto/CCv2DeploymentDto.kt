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

import java.time.OffsetDateTime

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
) : CCv2Dto {
    fun canTrack() = status == CCv2DeploymentStatusEnum.DEPLOYING || status == CCv2DeploymentStatusEnum.SCHEDULED
}