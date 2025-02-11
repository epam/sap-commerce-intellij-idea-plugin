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

import com.intellij.idea.plugin.hybris.ccv2.model.BuildProgressDTO
import com.intellij.idea.plugin.hybris.ccv2.model.BuildProgressStartedTaskDTO
import java.time.OffsetDateTime

data class CCv2BuildProgressDto(
    val buildStatus: String,
    val buildCode: String,
    val errorMessage: String,
    val numberOfTasks: Int,
    val percentage: Int,
    val startedTasks: Collection<CCv2BuildProgressStartedTaskDto>,
) : CCv2DTO {

    companion object {
        fun map(buildProgress: BuildProgressDTO) = CCv2BuildProgressDto(
            buildStatus = buildProgress.buildStatus ?: "N/A",
            buildCode = buildProgress.buildCode ?: "N/A",
            errorMessage = buildProgress.errorMessage ?: "N/A",
            numberOfTasks = buildProgress.numberOfTasks ?: 0,
            percentage = buildProgress.percentage ?: 0,
            startedTasks = buildProgress.startedTasks
                ?.map { CCv2BuildProgressStartedTaskDto.map(it) }
                ?: emptyList(),
        )

    }
}


data class CCv2BuildProgressStartedTaskDto(
    val task: String,
    val name: String,
    val startTimestamp: OffsetDateTime?
) {
    companion object {
        fun map(buildProgress: BuildProgressStartedTaskDTO) = CCv2BuildProgressStartedTaskDto(
            task = buildProgress.task ?: "N/A",
            name = buildProgress.name ?: "N/A",
            startTimestamp = buildProgress.startTimestamp
        )
    }
}