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

package com.intellij.idea.plugin.hybris.tools.logging.actions

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionContext
import com.intellij.idea.plugin.hybris.util.PackageUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

abstract class AbstractLoggerAction(private val logLevel: LogLevel) : AnAction(logLevel.name, null, logLevel.icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val logIdentifier = e.getData(HybrisConstants.DATA_KEY_LOGGER_IDENTIFIER)

        if (logIdentifier == null) {
            Notifications.error("Unable to change the log level", "Cannot retrieve a logger name.")
                .hideAfter(5)
                .notify(project)
            return
        }

        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = LoggingExecutionContext(
            title = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]",
            loggerName = logIdentifier,
            logLevel = logLevel
        )

        project.service<LoggingExecutionClient>().execute(context) { coroutineScope, result ->
            val abbreviationLogIdentifier = PackageUtils.abbreviatePackageName(logIdentifier)

            if (result.statusCode == 200) {
                Notifications.info(
                    "Log level updated",
                    """
                        <p>Level  : $logLevel</p>
                        <p>Logger : $abbreviationLogIdentifier</p>
                        <p>${server.shortenConnectionName()}</p>
                        """.trimIndent()
                )
                    .hideAfter(5)
                    .notify(project)
            } else {
                Notifications.error(
                    "Failed to update log level",
                    """
                        <p>Level  : $logLevel</p>
                        <p>Logger : $abbreviationLogIdentifier</p>
                        <p>${server.shortenConnectionName()}</p>
                        """.trimIndent()
                )
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val isRightPlace = "GoToAction" != e.place
        e.presentation.isEnabled = isRightPlace
        e.presentation.isVisible = isRightPlace
    }

}

class AllLoggerAction : AbstractLoggerAction(LogLevel.ALL)
class OffLoggerAction : AbstractLoggerAction(LogLevel.OFF)
class TraceLoggerAction : AbstractLoggerAction(LogLevel.TRACE)
class DebugLoggerAction : AbstractLoggerAction(LogLevel.DEBUG)
class InfoLoggerAction : AbstractLoggerAction(LogLevel.INFO)
class WarnLoggerAction : AbstractLoggerAction(LogLevel.WARN)
class ErrorLoggerAction : AbstractLoggerAction(LogLevel.ERROR)
class FatalLoggerAction : AbstractLoggerAction(LogLevel.FATAL)
class SevereLoggerAction : AbstractLoggerAction(LogLevel.SEVERE)
