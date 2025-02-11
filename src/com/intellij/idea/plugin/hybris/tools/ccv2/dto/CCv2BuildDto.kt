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

import com.intellij.idea.plugin.hybris.ccv2.model.BuildDetailDTO
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Util
import java.time.OffsetDateTime
import javax.swing.Icon

data class CCv2BuildDto(
    val code: String,
    val name: String,
    val branch: String,
    val status: CCv2BuildStatus,
    val appCode: String,
    val appDefVersion: String,
    val createdBy: String,
    val startTime: OffsetDateTime?,
    val endTime: OffsetDateTime?,
    val buildVersion: String,
    val version: String,
    val link: String?,
) : CCv2DTO {
    val duration = CCv2Util.getTimeDiffInMinutes(startTime, endTime).takeIf { it.toInt() != -1 } ?: "N/A"

    fun canDelete() = status != CCv2BuildStatus.DELETED && status != CCv2BuildStatus.UNKNOWN
    fun canDownloadLogs() = status != CCv2BuildStatus.DELETED && status != CCv2BuildStatus.UNKNOWN
    fun canDeploy() = status == CCv2BuildStatus.SUCCESS
    fun canTrack() = status == CCv2BuildStatus.SCHEDULED || status == CCv2BuildStatus.BUILDING || status == CCv2BuildStatus.UNKNOWN

    companion object {
        fun map(build: BuildDetailDTO) = CCv2BuildDto(
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
                ?: "N/A",
            link = if (build.subscriptionCode != null && build.code != null)
                "https://${HybrisConstants.CCV2_DOMAIN}/subscription/${build.subscriptionCode}/applications/commerce-cloud/builds/${build.code}"
            else null
        )

    }
}

enum class CCv2BuildStatus(val title: String, val icon: Icon) {
    UNKNOWN("Unknown", HybrisIcons.CCv2.Build.Status.UNKNOWN),
    SCHEDULED("Scheduled", HybrisIcons.CCv2.Build.Status.SCHEDULED),
    BUILDING("Building", HybrisIcons.CCv2.Build.Status.BUILDING),
    SUCCESS("Success", HybrisIcons.CCv2.Build.Status.SUCCESS),
    FAIL("Fail", HybrisIcons.CCv2.Build.Status.FAIL),
    DELETED("Deleted", HybrisIcons.CCv2.Build.Status.DELETED);

    companion object {
        fun tryValueOf(name: String?) = CCv2BuildStatus.entries
            .find { it.name == name }
            ?: UNKNOWN
    }
}