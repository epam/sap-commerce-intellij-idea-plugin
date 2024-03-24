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

package com.intellij.idea.plugin.hybris.tools.ccv2.strategies

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.ide.BrowserUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.CCv2SettingsConfigurableProvider
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentType
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

@Service
class SAPCCMCCv2Strategy : CCv2Strategy {

    override fun fetchEnvironments(project: Project, ccv2Token: String, subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Environment>> {
        val appSettings = HybrisApplicationSettingsComponent.getInstance()
        val indicator = ProgressManager.getInstance().progressIndicator
        val authParameters = arrayOf("config", "set", "auth-credentials", ccv2Token)

        indicator.text2 = "Authenticating with the provided token..."
        executeCLI(project, appSettings, *authParameters) ?: return emptyMap()

        return subscriptions
            .associateWith { subscription ->
                val parameters = arrayOf("environment", "list", "--subscription-code=${subscription.id}")

                // the first row is a header, second+ rows are data rows
                // we need to identify start position for each column by corresponding Column header
                indicator.text2 = "Fetching environments for subscription: $subscription"
                val processOutput = executeCLI(project, appSettings, *parameters)
                    ?.takeIf { it.size > 1 }
                    ?: return@associateWith emptyList()

                val columns = listOf("CODE", "NAME", "STATUS", "TYPE", "DEPLOYMENT STATUS")
                    .associateWith { processOutput[0].indexOf(it) }
                    .filter { it.value != -1 }
                    .takeIf { it.size == 5 }
                    ?: return@associateWith emptyList()

                return@associateWith processOutput
                    .subList(1, processOutput.size)
                    .map {
                        CCv2Environment(
                            code = it.substring(0..<columns["NAME"]!!).trim(),
                            name = it.substring(columns["NAME"]!!..<columns["STATUS"]!!).trim(),
                            status = it.substring(columns["STATUS"]!!..<columns["TYPE"]!!).trim(),
                            type = CCv2EnvironmentType.tryValueOf(it.substring(columns["TYPE"]!!..<columns["DEPLOYMENT STATUS"]!!).trim()),
                            deploymentStatus = it.substring(columns["DEPLOYMENT STATUS"]!!).trim(),
                        )
                    }
            }
    }

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

    private fun executeCLI(
        project: Project,
        appSettings: HybrisApplicationSettingsComponent,
        vararg parameters: String
    ): List<String>? {
        val cliDirectory = appSettings.state.sapCLIDirectory
            ?.let { File(it) }
            ?.takeIf { it.exists() }
            ?: return null
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

            val timeout = appSettings.state.sapCLITimeout * 1000L
            val waitFor = handler.waitFor(timeout)
            if (!waitFor) {
                handler.destroyProcess()

                Notifications
                    .create(
                        NotificationType.WARNING,
                        "SAP CCM: Interrupted on timeout",
                        "Exceeded current timeout of $timeout seconds, it can be adjusted within CCv2 settings."
                    )
                    .addAction("Open Settings") { _, _ ->
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, CCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                    }
                    .hideAfter(10)
                    .notify(project)

                return null
            }

            return if (handler.exitCode != 0) {
                val content = processOutput.joinToString(System.lineSeparator())
                val notification = Notifications
                    .create(
                        NotificationType.WARNING,
                        "SAP CCM: Unable to process request",
                        content
                    )
                if (content.contains("UNAUTHORIZED")) {
                    notification
                        .addAction("Open Settings") { _, _ ->
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, CCv2SettingsConfigurableProvider.SettingsConfigurable::class.java)
                        }
                        .addAction("Generating API Tokens...") { _, _ -> BrowserUtil.browse(HybrisConstants.URL_HELP_GENERATING_API_TOKENS) }
                }
                notification
                    .notify(project)

                return null
            } else processOutput
        } catch (e: ExecutionException) {
            return null
        }
    }
}