/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RemoteConnection
import com.intellij.execution.configurations.RemoteState
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Paths

class LocalSapCXRunProfileState(
    val executor: Executor,
    environment: ExecutionEnvironment, val project: Project
) : CommandLineState(environment), RemoteState {

    private val SHMEM_ADDRESS: String = "javadebug"
    private val HOST = "localhost"
    private val PORT = "8000"
    private val SERVER_MODE = false
    private val USE_SOCKET_TRANSPORT = true

    init {
        updateDebugPort(PORT)
    }

    private fun getScriptPath(): String {
        val basePath = project.basePath!!
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val hybrisDirectory = settings.hybrisDirectory!!
        val script = if (SystemUtils.IS_OS_WINDOWS) HybrisConstants.HYBRIS_SERVER_BASH_SCRIPT_NAME else HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME

        return Paths.get(basePath, hybrisDirectory, script).toString()
    }

    private fun getWorkDirectory(): String {
        val basePath = project.basePath!!
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        val hybrisDirectory = settings.hybrisDirectory!!

        return Paths.get(basePath, hybrisDirectory, HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine(getScriptPath())
        commandLine.setWorkDirectory(getWorkDirectory())
        if (executor is DefaultDebugExecutor) {
            commandLine.addParameter("debug")
        }
        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun getRemoteConnection(propertyString: String): RemoteConnection {
        var useSocket: Boolean = USE_SOCKET_TRANSPORT
        var hostName: String = HOST
        var address: String = PORT
        var serverMode: Boolean = SERVER_MODE

        for (setting in propertyString.split(',')) {
            val part = setting.split('=')
            when (part[0]) {
                "transport" -> useSocket = part[1] == "dt_socket"
                "server" -> serverMode = part[1] == "y"
                "address" -> address = part[1]
                "hostName" -> hostName = part[1]
            }
        }

        val debugPort = if (useSocket) address else SHMEM_ADDRESS
        updateDebugPort(debugPort)

        return RemoteConnection(useSocket, hostName, debugPort, !serverMode)
    }

    private fun updateDebugPort(debugPort: String) {
        val debuggerRunnerSettings = environment.runnerSettings as GenericDebuggerRunnerSettings
        debuggerRunnerSettings.debugPort = debugPort
    }

    override fun getRemoteConnection(): RemoteConnection {
        val propertyService = PropertyService.getInstance(project)
        if (propertyService != null) {
            val debugOptions = propertyService.findProperty(HybrisConstants.TOMCAT_JAVA_DEBUG_OPTIONS)!!

            val propertyPrefix = "-Xrunjdwp:"
            val startIndex: Int = debugOptions.indexOf(propertyPrefix)
            val prefixStartIndex = startIndex + propertyPrefix.length

            val endIndex: Int = debugOptions.indexOf(' ', prefixStartIndex)
            val debugSetting: String = if (endIndex > -1) debugOptions.substring(prefixStartIndex, endIndex) else debugOptions.substring(prefixStartIndex)
            return getRemoteConnection(debugSetting)
        }
        return RemoteConnection(USE_SOCKET_TRANSPORT, HOST, PORT, SERVER_MODE)
    }

}