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

import sap.commerce.toolset.ccv2.model.DeploymentProgressStepDTO
import java.time.OffsetDateTime

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