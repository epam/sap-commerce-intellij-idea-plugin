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
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Deployment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentDatabaseUpdateModeEnum
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentStatusEnum
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentStrategyEnum
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.ProgressReporter

object SAPCCMDeploymentCommands {
    private const val COMMAND = "deployment"
    private val headers = listOf(
        "CODE",
        "CREATED BY",
        "CREATED TIME",
        "BUILD CODE",
        "ENV. CODE",
        "UPDATE MODE",
        "STRATEGY",
        "SCHEDULED TIME",
        "DEPLOYED TIME",
        "FAILED TIME",
        "UNDEPLOYED TIME",
        "STATUS",
        "CANCELED BY",
        "CANCELED TIME",
        "CANCEL FINISHED TIME",
        "CANCEL FAILED",
    )

    private val listCommand = object : AbstractSAPCCMListCommand<CCv2Deployment>("Deployments", COMMAND, headers) {}

    suspend fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        progressReporter: ProgressReporter,
        subscriptions: Collection<CCv2Subscription>
    ) = listCommand.list(project, appSettings, progressReporter, subscriptions) { row, columns -> mapToDTO(row, columns) }

    suspend fun list(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        subscription: CCv2Subscription
    ) = listCommand.list(project, appSettings, subscription) { row, columns -> mapToDTO(row, columns) }

    private fun mapToDTO(row: String, columns: Map<String, Int>) = CCv2Deployment(
        code = row.substring(0..<columns["CREATED BY"]!!).trim(),
        createdBy = row.substring(columns["CREATED BY"]!!..<columns["CREATED TIME"]!!).trim(),
        createdTime = row.substring(columns["CREATED TIME"]!!..<columns["BUILD CODE"]!!).trim(),
        buildCode = row.substring(columns["BUILD CODE"]!!..<columns["ENV. CODE"]!!).trim(),
        envCode = row.substring(columns["ENV. CODE"]!!..<columns["UPDATE MODE"]!!).trim(),
        updateMode = CCv2DeploymentDatabaseUpdateModeEnum.tryValueOf(
            row.substring(columns["UPDATE MODE"]!!..<columns["STRATEGY"]!!).trim()
        ),
        strategy = CCv2DeploymentStrategyEnum.tryValueOf(
            row.substring(columns["STRATEGY"]!!..<columns["SCHEDULED TIME"]!!).trim()
        ),
        scheduledTime = row.substring(columns["SCHEDULED TIME"]!!..<columns["DEPLOYED TIME"]!!).trim(),
        deployedTime = row.substring(columns["DEPLOYED TIME"]!!..<columns["FAILED TIME"]!!).trim(),
        failedTime = row.substring(columns["FAILED TIME"]!!..<columns["UNDEPLOYED TIME"]!!).trim(),
        undeployedTime = row.substring(columns["UNDEPLOYED TIME"]!!..<columns["STATUS"]!!).trim(),
        status = CCv2DeploymentStatusEnum.tryValueOf(
            row.substring(columns["STATUS"]!!..<columns["CANCELED BY"]!!).trim()
        )
    )
}