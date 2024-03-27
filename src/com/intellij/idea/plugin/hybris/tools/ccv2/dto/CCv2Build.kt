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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.swing.Icon

private val CCV2_DATE_FORMAT_LOCAL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss")
private val CCV2_DATE_FORMAT_CCM_NANO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
private val CCV2_DATE_FORMAT_CCM: DateTimeFormatter = DateTimeFormatter.ISO_DATE
private val ZONE_GMT = ZoneId.of("GMT")

data class CCv2Build(
    val code: String,
    val name: String,
    val branch: String,
    val status: CCv2BuildStatus,
    val appCode: String,
    val appDefVersion: String,
    val createdBy: String,
    val startTime: String?,
    val endTime: String?,
    val buildVersion: String,
    val version: String,
) : CCv2DTO {
    val startTimeFormatted
        get() = formatTime(startTime)
    val endTimeFormatted
        get() = formatTime(endTime)

    private fun formatTime(time: String?) = time
        ?.let {
            tryParse(it, CCV2_DATE_FORMAT_CCM_NANO)
                ?: tryParse(it, CCV2_DATE_FORMAT_CCM)
        }
        ?.let { ZonedDateTime.of(it, ZONE_GMT) }
        ?.withZoneSameInstant(ZoneId.systemDefault())
        ?.format(CCV2_DATE_FORMAT_LOCAL)
        ?: (time ?: "N/A")

    private fun tryParse(time: String, formatter: DateTimeFormatter) = try {
        LocalDateTime.parse(time, formatter)
    } catch (e: DateTimeParseException) {
        null
    }

    fun canDelete() = status != CCv2BuildStatus.DELETED && status != CCv2BuildStatus.UNKNOWN
}

enum class CCv2BuildStatus(val title: String, val icon: Icon) {
    UNKNOWN("Unknown", HybrisIcons.CCV2_BUILD_STATUS_UNKNOWN),
    SCHEDULED("Scheduled", HybrisIcons.CCV2_BUILD_STATUS_SCHEDULED),
    BUILDING("Building", HybrisIcons.CCV2_BUILD_STATUS_BUILDING),
    SUCCESS("Success", HybrisIcons.CCV2_BUILD_STATUS_SUCCESS),
    FAIL("Fail", HybrisIcons.CCV2_BUILD_STATUS_FAIL),
    DELETED("Deleted", HybrisIcons.CCV2_BUILD_STATUS_DELETED);

    companion object {
        fun tryValueOf(name: String) = CCv2BuildStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}