/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.ccv2.mcp

import sap.commerce.toolset.ccv2.dto.*
import sap.commerce.toolset.ccv2.mcp.dto.*

internal object CCv2McpMapper {

    fun CCv2EnvironmentDto.mcpDto() = CCv2EnvironmentMcpDto(
        code = code,
        name = name,
        type = type.name,
        status = status.name,
        deploymentStatus = deploymentStatus.name,
        deploymentAllowed = deploymentAllowed,
        link = link,
    )

    fun CCv2BuildDto.mcpDto() = CCv2BuildMcpDto(
        code = code,
        name = name,
        branch = branch,
        status = status.name,
        appCode = appCode,
        appDefVersion = appDefVersion,
        createdBy = createdBy,
        startTime = startTime?.toString(),
        endTime = endTime?.toString(),
        buildVersion = buildVersion,
        deployed = deployed,
        link = link,
    )

    fun CCv2DeploymentDto.mcpDto() = CCv2DeploymentMcpDto(
        code = code,
        buildCode = buildCode,
        environmentCode = envCode,
        status = status.name,
        updateMode = updateMode.name,
        strategy = strategy.name,
        createdBy = createdBy,
        createdTime = createdTime?.toString(),
        scheduledTime = scheduledTime?.toString(),
        deployedTime = deployedTime?.toString(),
        failedTime = failedTime?.toString(),
        link = link,
    )

    fun CCv2ServiceDto.mcpDto() = CCv2ServiceMcpDto(
        code = code,
        name = name,
        desiredReplicas = desiredReplicas,
        availableReplicas = availableReplicas,
        replicas = replicas.map { CCv2ReplicaMcpDto(name = it.name, status = it.status, ready = it.ready) },
        link = link,
    )

    fun CCv2EndpointDto.mcpDto() = CCv2EndpointMcpDto(
        code = code,
        name = name,
        service = service,
        url = url,
        maintenanceMode = maintenanceMode,
        link = link,
    )

    fun CCv2DataBackupDto.mcpDto() = CCv2DataBackupMcpDto(
        code = dataBackupCode,
        name = name,
        buildCode = buildCode,
        status = status,
        type = dataBackupType,
        description = description,
        createdBy = createdBy,
        createdTime = createdTimestamp?.toString(),
    )

    fun CCv2ScheduledActivityDto.mcpDto() = CCv2ScheduledActivityMcpDto(
        code = code,
        activityType = activityType.name,
        activityName = activityName,
        status = status.name,
        scheduledTime = scheduledTimestamp.toString(),
        startedTime = startedTimestamp?.toString(),
        finishedTime = finishedTimestamp?.toString(),
        createdBy = createdBy,
        createdTime = createdTimestamp?.toString(),
    )
}
