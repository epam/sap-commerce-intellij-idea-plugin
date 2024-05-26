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

import com.intellij.idea.plugin.hybris.ccv2.model.DatabackupDetailDTO
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Util
import java.time.OffsetDateTime

data class CCv2DataBackupDto(
    val dataBackupCode: String,
    val name: String,
    val buildCode: String,
    val status: String,
    val dataBackupType: String,
    val description: String,
    var createdBy: String,
    val createdTimestamp: OffsetDateTime?,
    val canBeRestored: Boolean,
    val canBeDeleted: Boolean,
    val canBeCanceled: Boolean,
) {
    val createdTimestampFormatted
        get() = CCv2Util.formatTime(createdTimestamp)

    companion object {
        fun map(dto: DatabackupDetailDTO) = CCv2DataBackupDto(
            dataBackupCode = dto.databackupCode ?: "N/A",
            name = dto.name ?: "N/A",
            buildCode = dto.buildCode ?: "N/A",
            status = dto.status ?: "N/A",
            dataBackupType = dto.databackupType ?: "N/A",
            description = dto.description ?: "N/A",
            createdBy = dto.createdBy ?: "N/A",
            createdTimestamp = dto.createdTimestamp,
            canBeRestored = dto.canBeRestored ?: false,
            canBeDeleted = dto.canBeDeleted ?: false,
            canBeCanceled = dto.canBeCanceled ?: false
        )
    }
}
