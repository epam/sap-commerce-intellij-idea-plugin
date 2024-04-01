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
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DeploymentStatusEnum
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentType
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.ProgressReporter

object SAPCCMEnvironmentCommands {
    private const val COMMAND = "environment"
    private val listCommand = object : AbstractSAPCCMListCommand<CCv2Environment>(
        "Environments", COMMAND,
        listOf("CODE", "NAME", "STATUS", "TYPE", "DEPLOYMENT STATUS")
    ) {}

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

    private fun mapToDTO(row: String, columns: Map<String, Int>) = CCv2Environment(
        code = row.substring(0..<columns["NAME"]!!).trim(),
        name = row.substring(columns["NAME"]!!..<columns["STATUS"]!!).trim(),
        status = CCv2EnvironmentStatus.tryValueOf(row.substring(columns["STATUS"]!!..<columns["TYPE"]!!).trim()),
        type = CCv2EnvironmentType.tryValueOf(row.substring(columns["TYPE"]!!..<columns["DEPLOYMENT STATUS"]!!).trim()),
        deploymentStatus = CCv2DeploymentStatusEnum.tryValueOf(row.substring(columns["DEPLOYMENT STATUS"]!!).trim()),
    )
}