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

import com.intellij.idea.plugin.hybris.ccv2.model.BuildDetailDTO
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Util
import java.time.OffsetDateTime
import javax.swing.Icon

data class CCv2Build(
    val code: String = "",
    val name: String,
    var branch: String,
    val status: CCv2BuildStatus = CCv2BuildStatus.UNKNOWN,
    val appCode: String = "",
    val appDefVersion: String = "",
    val createdBy: String = "",
    val startTime: OffsetDateTime? = null,
    val endTime: OffsetDateTime? = null,
    val buildVersion: String = "",
    val version: String = "",
) : CCv2DTO {
    val startTimeFormatted
        get() = CCv2Util.formatTime(startTime)
    val endTimeFormatted
        get() = CCv2Util.formatTime(endTime)

    fun canDelete() = status != CCv2BuildStatus.DELETED && status != CCv2BuildStatus.UNKNOWN
    fun canDeploy() = status == CCv2BuildStatus.SUCCESS

    companion object {
        fun map(build: BuildDetailDTO) = CCv2Build(
            code = build.code ?: "N/A",
            name = build.name ?: "N/A",
            branch = build.branch ?: "N/A",
            status = CCv2BuildStatus.tryValueOf(build.status),
            appCode = build.applicationCode ?: "N/A",
            appDefVersion = build.applicationDefinitionVersion ?: "N/A",
            createdBy = build.createdBy ?: "N/A",
            startTime = build.buildStartTimestamp,
            endTime = build.buildEndTimestamp,
            buildVersion = build.buildVersion ?: "N/A",
            version = build.buildVersion
                ?.split("-")
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "N/A"
        )

    }
}

enum class CCv2BuildStatus(val title: String, val icon: Icon) {
    UNKNOWN("Unknown", HybrisIcons.CCV2_BUILD_STATUS_UNKNOWN),
    SCHEDULED("Scheduled", HybrisIcons.CCV2_BUILD_STATUS_SCHEDULED),
    BUILDING("Building", HybrisIcons.CCV2_BUILD_STATUS_BUILDING),
    SUCCESS("Success", HybrisIcons.CCV2_BUILD_STATUS_SUCCESS),
    FAIL("Fail", HybrisIcons.CCV2_BUILD_STATUS_FAIL),
    DELETED("Deleted", HybrisIcons.CCV2_BUILD_STATUS_DELETED);

    companion object {
        fun tryValueOf(name: String?) = CCv2BuildStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}