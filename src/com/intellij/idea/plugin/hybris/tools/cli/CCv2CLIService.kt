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

package com.intellij.idea.plugin.hybris.tools.cli

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineStateUtil
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.tools.cli.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.cli.dto.CCv2EnvironmentType
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.messages.Topic
import java.io.File
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class CCv2CLIService(val project: Project) {

    private val messageBus = project.messageBus

    fun fetchEnvironments(subscriptions: Collection<CCv2Subscription>): Map<CCv2Subscription, Collection<CCv2Environment>> {
        messageBus.syncPublisher(TOPIC_ENVIRONMENT).fetchingStarted()

        val indicator = ProgressManager.getInstance().progressIndicator
        val appSettings = HybrisApplicationSettingsComponent.getInstance()
        val cliToken = appSettings.sapCLIToken ?: return emptyMap()
        val authParameters = arrayOf("config", "set", "auth-credentials", cliToken)

        indicator.text2 = "Authenticating with the provided token..."
        execCLI(appSettings, *authParameters) ?: return emptyMap()

        return subscriptions
            .associateWith { subscription ->
                val parameters = arrayOf("environment", "list", "--subscription-code=${subscription.id}")

                // the first row is a header, second+ rows are data rows
                // we need to identify start position for each column by corresponding Column header
                indicator.text2 = "Fetching environments for subscription: $subscription"
                val processOutput = execCLI(appSettings, *parameters)
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
            .also { messageBus.syncPublisher(TOPIC_ENVIRONMENT).fetchingCompleted(it) }
    }

    private fun execCLI(
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
                    if (event.exitCode != 0) {
                        // TODO: baaad, do something...
                    } else {
                        processOutput.addAll(output.stdoutLines)
                    }
                }
            })

            handler.startNotify()

            // TODO: add timeout configuration
            val waitFor = handler.waitFor(10 * 1000L)
            if (!waitFor) {
                // TODO: inform somehow
                handler.destroyProcess()
                return null
            }

            return if (handler.exitCode != 0) null
            else processOutput
        } catch (e: ExecutionException) {
            return null
        }
    }

    companion object {

        val TOPIC_ENVIRONMENT = Topic("HYBRIS_CCv2_CLI_ENVIRONMENT_LISTENER", CCv2CLIEnvironmentListener::class.java)
        fun getInstance(project: Project): CCv2CLIService = project.getService(CCv2CLIService::class.java)
    }
}