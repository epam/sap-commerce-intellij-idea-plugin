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

package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.DEBUG_HOST
import com.intellij.idea.plugin.hybris.common.HybrisConstants.DEBUG_PORT
import com.intellij.idea.plugin.hybris.settings.ProjectSettings
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.SystemUtils
import java.io.IOException
import java.net.Socket
import java.nio.file.Paths

class LocalSapCXRunProfileState(
    val executor: Executor,
    environment: ExecutionEnvironment, val project: Project, val configuration: LocalSapCXRunConfiguration
) : CommandLineState(environment), JavaCommandLine, RemoteConnectionCreator {


    private fun getScriptPath(): String {
        val basePath = project.basePath ?: ""
        val settings = ProjectSettings.getInstance(project)
        val hybrisDirectory = settings.hybrisDirectory ?: ""
        val script = if (SystemUtils.IS_OS_WINDOWS) HybrisConstants.HYBRIS_SERVER_BASH_SCRIPT_NAME else HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME

        return Paths.get(basePath, hybrisDirectory, script).toString()
    }

    private fun getWorkDirectory(): String {
        val basePath = project.basePath ?: ""
        val settings = ProjectSettings.getInstance(project)
        val hybrisDirectory = settings.hybrisDirectory ?: ""

        return Paths.get(basePath, hybrisDirectory, HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine(getScriptPath())
        commandLine.setWorkDirectory(getWorkDirectory())
        if (executor is DefaultDebugExecutor) {
            commandLine.addParameter("debug")
        }
        if (sapCXOptions.environmentProperties != null) {
            commandLine.environment.putAll(sapCXOptions.environmentProperties)
        }

        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)

        if (executor is DefaultDebugExecutor) {
            waitForPort(sapCXOptions.remoteDebugHost ?: DEBUG_HOST, Integer.valueOf(sapCXOptions.remoteDebugPort ?: DEBUG_PORT), 30000)
        }

        return processHandler
    }

    private fun waitForPort(host: String, port: Int, timeoutMillis: Long = 30000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                Socket(host, port).use { return } // success
            } catch (e: IOException) {
                Thread.sleep(500)
            }
        }
        throw RuntimeException("Timeout waiting for debug port $host:$port")
    }

    private fun updateDebugPort(debugPort: String) {
        val debuggerRunnerSettings = environment.runnerSettings as GenericDebuggerRunnerSettings
        debuggerRunnerSettings.debugPort = debugPort
    }

    override fun createRemoteConnection(environment: ExecutionEnvironment?): RemoteConnection {
        val remoteConnetion = configuration.getRemoteConnetion()
        updateDebugPort(remoteConnetion.debuggerAddress)
        return remoteConnetion
    }


    override fun getJavaParameters(): JavaParameters = JavaParameters()

    override fun isPollConnection(): Boolean = true

    private val sapCXOptions: LocalSapCXRunnerOptions get() = configuration.getSapCXOptions()


}
