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

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.options.ApplicationCCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DTO
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

object SAPCCM {

    fun execute(
        project: Project,
        appSettings: ApplicationSettingsComponent,
        vararg parameters: String
    ): List<String>? {
        val sapCLIDirectory = appSettings.state.sapCLIDirectory
            ?.takeIf { it.isNotBlank() }
        if (sapCLIDirectory == null) {
            Notifications.create(
                NotificationType.ERROR,
                "SAP CCM: Invalid Configuration",
                "Directory for SAP CLI is not set, please specify it via corresponding application settings."
            )
                .addAction("Open Settings") { _, _ ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
                .hideAfter(10)
                .notify(project)
            return null
        }

        val validationError = validateSAPCCMDirectory(sapCLIDirectory)
        if (validationError != null) {
            Notifications.create(
                NotificationType.ERROR,
                "SAP CCM: Invalid Configuration",
                validationError
            )
                .addAction("Open Settings") { _, _ ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ApplicationCCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                }
                .hideAfter(10)
                .notify(project)
            return null
        }

        val cliDirectory = File(sapCLIDirectory)
        val execPath = if (SystemInfo.isWindows) "bin/sapccm.bat"
        else "bin/sapccm"

        val gcl = GeneralCommandLine()
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(cliDirectory)
            .withExePath(execPath)
            .withCharset(StandardCharsets.UTF_8)
            .withParameters(*parameters)

        val processOutput = mutableListOf<String>()
        try {
            val handler = JavaCommandLineStateUtil.startProcess(gcl, true)
            handler.addProcessListener(object : CapturingProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    if (event.exitCode == 0) {
                        processOutput.addAll(output.stdoutLines)
                    } else {
                        processOutput.add(output.stderr)
                        processOutput.add(output.stdout)
                    }
                }
            })

            handler.startNotify()

            val timeout = appSettings.state.ccv2ReadTimeout * 1000L
            val waitFor = handler.waitFor(timeout)
            if (!waitFor) {
                handler.destroyProcess()

                throw SocketTimeoutException()
            }

            return if (handler.exitCode != 0) {
                val content = processOutput.joinToString(System.lineSeparator())
                throw SAPCCMClientException(content)
            } else processOutput
        } catch (e: ExecutionException) {
            return null
        }
    }

    // the first row is a header, second+ rows are data rows
    // we need to identify start position for each column by corresponding Column header
    fun <T : CCv2DTO> transformResult(
        headers: List<String>,
        result: List<String>,
        transform: (String, Map<String, Int>) -> T,
    ): List<T> {
        val processOutput = result
            .takeIf { it.size > 1 }
            ?: return emptyList()

        return columnsToRange(processOutput[0], headers)
            ?.let { columns ->
                processOutput
                    .subList(1, processOutput.size)
                    .map { transform(it, columns) }
            }
            ?: emptyList()
    }

    fun formatTime(time: String?) = time
        ?.let {
            tryParse(it, HybrisConstants.CCV2_DATE_FORMAT_CCM_NANO)
                ?: tryParse(it, HybrisConstants.CCV2_DATE_FORMAT_CCM)
        }
        ?.let { ZonedDateTime.of(it, HybrisConstants.ZONE_GMT) }
        ?.withZoneSameInstant(ZoneId.systemDefault())
        ?.format(HybrisConstants.CCV2_DATE_FORMAT_LOCAL)
        ?: "N/A"

    fun validateSAPCCMDirectory(directory: String): String? {
        val executable = if (SystemInfo.isWindows) "sapccm.bat"
        else "sapccm"

        val valid = Paths.get(directory, "bin", executable)
            .takeIf { path -> path.exists() }
            ?.isExecutable()
            ?: false
        return if (!valid) "Invalid SAP CCM directory, cannot find <strong>bin/$executable</strong> executable file."
        else null
    }

    private fun columnsToRange(headerRow: String, columnNames: List<String>): Map<String, Int>? = columnNames
        .associateWith { headerRow.indexOf(it) }
        .filter { it.value != -1 }
        .takeIf { it.size == columnNames.size }

    private fun tryParse(time: String, formatter: DateTimeFormatter) = try {
        LocalDateTime.parse(time, formatter)
    } catch (e: DateTimeParseException) {
        null
    }
}