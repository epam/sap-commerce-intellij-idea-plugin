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

import com.intellij.idea.plugin.hybris.tools.ccm.SAPCCM

data class CCv2Deployment(
    val code: String,
    val createdBy: String,
    val createdTime: String,
    val buildCode: String,
    val envCode: String,
    val updateMode: CCv2DeploymentDatabaseUpdateModeEnum,
    val strategy: CCv2DeploymentStrategyEnum,
    val scheduledTime: String,
    val deployedTime: String,
    val failedTime: String,
    val undeployedTime: String,
    val status: String,
    val cancelledBy: String,
    val cancelledTime: String,
    val cancelFinishedTime: String,
    val cancelFailed: String,
) : CCv2DTO {

    val createdTimeFormatted
        get() = SAPCCM.formatTime(createdTime)

    val deployedTimeFormatted
        get() = SAPCCM.formatTime(deployedTime)
}

enum class CCv2DeploymentDatabaseUpdateModeEnum(val title: String) {
    NONE("None"),
    UPDATE("Update"),
    INITIALIZE("Initialize"),
    UNKNOWN("Unknown");

    companion object {
        fun tryValueOf(name: String) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}

enum class CCv2DeploymentStrategyEnum(val title: String) {
    ROLLING_UPDATE("Rolling update"),
    RECREATE("Recreate"),
    GREEN("Blue / Green"),
    UNKNOWN("Unknown");

    companion object {
        fun tryValueOf(name: String) = entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}
