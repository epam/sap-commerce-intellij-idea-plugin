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
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

open class SAPCCMCommand<T : CCv2DTO>(
    protected val name: String,
    protected val command: String,
    private val headers: List<String>,
) {
    // TODO: use Kotlin coroutines for parallel processing
    fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>,
        transform: (String, Map<String, Int>) -> T,
    ) = subscriptions
        .associateWith { subscription ->
            val parameters = arrayOf(command, "list", "--subscription-code=${subscription.id}")

            // the first row is a header, second+ rows are data rows
            // we need to identify start position for each column by corresponding Column header
            ProgressManager.getInstance().progressIndicator.text2 = "Fetching $name for subscription: $subscription"
            val processOutput = SAPCCM.execute(project, appSettings, *parameters)
                ?.takeIf { it.size > 1 }
                ?: return@associateWith emptyList()

            columnsToRange(processOutput[0], headers)
                ?.let { columns ->
                    processOutput
                        .subList(1, processOutput.size)
                        .map { transform(it, columns) }
                }
                ?: return@associateWith emptyList()
        }

    private fun columnsToRange(headerRow: String, columnNames: List<String>): Map<String, Int>? = columnNames
        .associateWith { headerRow.indexOf(it) }
        .filter { it.value != -1 }
        .takeIf { it.size == columnNames.size }
}

object SAPCCMEnvironmentCommands {
    private const val command = "environment"
    private val listCommand = object : SAPCCMCommand<CCv2Environment>(
        "Environments", command,
        listOf("CODE", "NAME", "STATUS", "TYPE", "DEPLOYMENT STATUS")
    ) {}

    fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, subscriptions) { row, columns ->
        CCv2Environment(
            code = row.substring(0..<columns["NAME"]!!).trim(),
            name = row.substring(columns["NAME"]!!..<columns["STATUS"]!!).trim(),
            status = CCv2EnvironmentStatus.tryValueOf(row.substring(columns["STATUS"]!!..<columns["TYPE"]!!).trim()),
            type = CCv2EnvironmentType.tryValueOf(row.substring(columns["TYPE"]!!..<columns["DEPLOYMENT STATUS"]!!).trim()),
            deploymentStatus = CCv2EnvironmentDeploymentStatus.tryValueOf(row.substring(columns["DEPLOYMENT STATUS"]!!).trim()),
        )
    }
}

object SAPCCMBuildCommands {
    private val gmtZone = ZoneId.of("GMT")
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

    private const val command = "build"
    private val listCommand = object : SAPCCMCommand<CCv2Build>(
        "Builds", command,
        listOf("CODE", "NAME", "BRANCH", "STATUS", "APP. CODE", "APP. DEF. VERSION", "CREATED BY", "START TIME", "END TIME", "BUILD VERSION")
    ) {}

    fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, subscriptions) { row, columns ->
        val buildVersion = row.substring(columns["BUILD VERSION"]!!).trim()
        CCv2Build(
            code = row.substring(0..<columns["NAME"]!!).trim(),
            name = row.substring(columns["NAME"]!!..<columns["BRANCH"]!!).trim(),
            branch = row.substring(columns["BRANCH"]!!..<columns["STATUS"]!!).trim(),
            status = CCv2BuildStatus.tryValueOf(row.substring(columns["STATUS"]!!..<columns["APP. CODE"]!!).trim()),
            appCode = row.substring(columns["APP. CODE"]!!..<columns["APP. DEF. VERSION"]!!).trim(),
            appDefVersion = row.substring(columns["APP. DEF. VERSION"]!!..<columns["CREATED BY"]!!).trim(),
            createdBy = row.substring(columns["CREATED BY"]!!..<columns["START TIME"]!!).trim(),
            startTime = row.substring(columns["START TIME"]!!..<columns["END TIME"]!!).trim()
                .takeIf { it.isNotBlank() }
                ?.let { LocalDateTime.parse(it, dateFormat) }
                ?.let { ZonedDateTime.of(it, gmtZone) },
            endTime = row.substring(columns["END TIME"]!!..<columns["BUILD VERSION"]!!).trim()
                .takeIf { it.isNotBlank() }
                ?.let { LocalDateTime.parse(it, dateFormat) }
                ?.let { ZonedDateTime.of(it, gmtZone) },
            buildVersion = buildVersion,
            version = buildVersion.split("-")
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "N/A"
        )
    }
}
