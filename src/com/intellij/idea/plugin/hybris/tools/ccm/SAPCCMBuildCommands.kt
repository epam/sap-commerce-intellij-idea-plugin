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

package com.intellij.idea.plugin.hybris.tools.ccm

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildStatus
import com.intellij.openapi.project.Project

object SAPCCMBuildCommands {
    private const val command = "build"
    private val headers = listOf("CODE", "NAME", "BRANCH", "STATUS", "APP. CODE", "APP. DEF. VERSION", "CREATED BY", "START TIME", "END TIME", "BUILD VERSION")

    private val listCommand = object : AbstractSAPCCMListCommand<CCv2Build>("Builds", command, headers) {}

    fun create(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscription: CCv2Subscription,
        name: String,
        branch: String
    ): CCv2Build? {
        val parameters = arrayOf(
            command, "create",
            "--no-wait",
            "--branch=$branch",
            "--name=$name",
            "--subscription-code=${subscription.id}"
        )

        return SAPCCM.execute(project, appSettings, *parameters)
            ?.let { SAPCCM.transformResult(headers, it) { row, columns -> mapToDTO(row, columns) } }
            ?.firstOrNull()
    }

    fun delete(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscription: CCv2Subscription,
        build: CCv2Build
    ) {

        val parameters = arrayOf(
            command, "delete",
            "--build-code=${build.code}",
            "--subscription-code=${subscription.id}"
        )

        SAPCCM.execute(project, appSettings, *parameters)
    }

    fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, subscriptions) { row, columns -> mapToDTO(row, columns) }

    private fun mapToDTO(row: String, columns: Map<String, Int>): CCv2Build {
        val buildVersion = row.substring(columns["BUILD VERSION"]!!).trim()
        return CCv2Build(
            code = row.substring(0..<columns["NAME"]!!).trim(),
            name = row.substring(columns["NAME"]!!..<columns["BRANCH"]!!).trim(),
            branch = row.substring(columns["BRANCH"]!!..<columns["STATUS"]!!).trim(),
            status = CCv2BuildStatus.tryValueOf(row.substring(columns["STATUS"]!!..<columns["APP. CODE"]!!).trim()),
            appCode = row.substring(columns["APP. CODE"]!!..<columns["APP. DEF. VERSION"]!!).trim(),
            appDefVersion = row.substring(columns["APP. DEF. VERSION"]!!..<columns["CREATED BY"]!!).trim(),
            createdBy = row.substring(columns["CREATED BY"]!!..<columns["START TIME"]!!).trim(),
            startTime = row.substring(columns["START TIME"]!!..<columns["END TIME"]!!).trim()
                .takeIf { it.isNotBlank() },
            endTime = row.substring(columns["END TIME"]!!..<columns["BUILD VERSION"]!!).trim()
                .takeIf { it.isNotBlank() },
            buildVersion = buildVersion,
            version = buildVersion.split("-")
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "N/A"
        )
    }


}