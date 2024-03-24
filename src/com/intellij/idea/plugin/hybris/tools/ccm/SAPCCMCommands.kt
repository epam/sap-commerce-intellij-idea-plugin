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
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DTO
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

open class SAPCCMCommand<T : CCv2DTO>(
    protected val name: String,
    protected val command: String,
    private val headers: List<String>,
) {
// TODO: use Kotlin coroutines for parallel processing
    fun list(
        project: Project,
        appSettings: HybrisApplicationSettingsComponent,
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
        appSettings: HybrisApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, subscriptions) { it, columns ->
        CCv2Environment(
            code = it.substring(0..<columns["NAME"]!!).trim(),
            name = it.substring(columns["NAME"]!!..<columns["STATUS"]!!).trim(),
            status = it.substring(columns["STATUS"]!!..<columns["TYPE"]!!).trim(),
            type = CCv2EnvironmentType.tryValueOf(it.substring(columns["TYPE"]!!..<columns["DEPLOYMENT STATUS"]!!).trim()),
            deploymentStatus = it.substring(columns["DEPLOYMENT STATUS"]!!).trim(),
        )
    }
}

object SAPCCMBuildCommands {
    private const val command = "build"
    private val listCommand = object : SAPCCMCommand<CCv2Build>(
        "Builds", command,
        listOf("CODE", "NAME", "BRANCH", "STATUS", "APP. CODE", "APP. DEF. VERSION", "CREATED BY", "START TIME", "END TIME", "BUILD VERSION")
    ) {}

    fun list(
        project: Project,
        appSettings: HybrisApplicationSettingsComponent,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, subscriptions) { it, columns ->
        CCv2Build(
            code = it.substring(0..<columns["NAME"]!!).trim(),
            name = it.substring(columns["NAME"]!!..<columns["BRANCH"]!!).trim(),
            branch = it.substring(columns["BRANCH"]!!..<columns["STATUS"]!!).trim(),
            status = it.substring(columns["STATUS"]!!..<columns["APP. CODE"]!!).trim(),
            appCode = it.substring(columns["APP. CODE"]!!..<columns["APP. DEF. VERSION"]!!).trim(),
            appDefVersion = it.substring(columns["APP. DEF. VERSION"]!!..<columns["CREATED BY"]!!).trim(),
            createdBy = it.substring(columns["CREATED BY"]!!..<columns["START TIME"]!!).trim(),
            startTime = it.substring(columns["START TIME"]!!..<columns["END TIME"]!!).trim(),
            endTime = it.substring(columns["END TIME"]!!..<columns["BUILD VERSION"]!!).trim(),
            buildVersion = it.substring(columns["BUILD VERSION"]!!).trim(),
        )
    }
}
