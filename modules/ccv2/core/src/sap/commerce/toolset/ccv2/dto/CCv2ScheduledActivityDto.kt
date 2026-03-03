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

package sap.commerce.toolset.ccv2.dto

import sap.commerce.toolset.ccv2.model.ScheduledActivityDetailDTO
import java.time.OffsetDateTime

data class CCv2ScheduledActivityDto(
    val code: String,
    val activityType: CCv2ScheduledActivityType,
    val scheduledTimestamp: OffsetDateTime,
    val startedTimestamp: OffsetDateTime?,
    val finishedTimestamp: OffsetDateTime?,
    val status: CCv2ScheduledActivityStatus,
    val readOnly: Boolean,
    val createdBy: String,
    val createdTimestamp: OffsetDateTime?,
    val lastModifiedBy: String,
    val lastModifiedTimestamp: OffsetDateTime?,
    val activityName: String,
    val activityNotes: String,
    val activityImpactNotes: String,
) : CCv2Dto {
    companion object {
        fun map(dto: ScheduledActivityDetailDTO) = CCv2ScheduledActivityDto(
            code = dto.code,
            activityType = CCv2ScheduledActivityType.tryValueOf(dto.activityType),
            scheduledTimestamp = dto.scheduledTimestamp,
            startedTimestamp = dto.startedTimestamp,
            finishedTimestamp = dto.finishedTimestamp,
            status = CCv2ScheduledActivityStatus.of(dto.status),
            readOnly = dto.readOnly ?: true,
            createdBy = dto.createdBy ?: "N/A",
            createdTimestamp = dto.createdTimestamp,
            lastModifiedBy = dto.createdBy ?: "N/A",
            lastModifiedTimestamp = dto.lastModifiedTimestamp,
            activityName = dto.activityName ?: "N/A",
            activityNotes = dto.activityNotes ?: "N/A",
            activityImpactNotes = dto.activityImpactNotes ?: "N/A",
        )
    }
}
