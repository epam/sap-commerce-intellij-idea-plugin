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

package com.intellij.idea.plugin.hybris.tools.logging.actions

import com.intellij.ide.DataManager
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.system.java.codeInsight.hints.LoggerConstants
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import javax.swing.Icon

open class LoggerAction(private val logLevel: String, val icon: Icon) : AnAction(logLevel, "", icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dataContext = e.dataContext
        val logIdentifier = DataManager.getInstance().loadFromDataContext(dataContext, LoggerConstants.LOGGER_IDENTIFIER_DATA_CONTEXT_KEY) ?: return

        ApplicationManager.getApplication().runReadAction {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Execute HTTP Call to SAP Commerce...") {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        val result = HybrisHacHttpClient.getInstance(project).executeLogUpdate(
                            project,
                            logIdentifier,
                            logLevel,
                            AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
                        )

                        val resultMessage = if (result.statusCode == 200) "Success" else "Failed"
                        val title = "Updating the log level: $resultMessage"

                        val serverName = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris).displayName

                        Notifications.create(
                            NotificationType.INFORMATION,
                            if (result.statusCode == 200) "Updating the log level: Success" else "Updating the log level: Failed",
                            if (result.statusCode == 200)
                                "The log level set to $logLevel for $logIdentifier, server $serverName. "
                            else
                                "The log level is not set to $logLevel for $logIdentifier, server $serverName. "
                        )
                            .hideAfter(5)
                            .notify(project)
                    } finally {
                    }
                }
            })
        }
    }

}

class TraceLoggerAction : LoggerAction("TRACE", HybrisIcons.Log.Level.TRACE)
class DebugLoggerAction : LoggerAction("DEBUG", HybrisIcons.Log.Level.DEBUG)
class InfoLoggerAction : LoggerAction("INFO", HybrisIcons.Log.Level.INFO)
class WarnLoggerAction : LoggerAction("WARN", HybrisIcons.Log.Level.WARN)
class ErrorLoggerAction : LoggerAction("ERROR", HybrisIcons.Log.Level.ERROR)
class FatalLoggerAction : LoggerAction("FATAL", HybrisIcons.Log.Level.FATAL)
class SevereLoggerAction : LoggerAction("SEVERE", HybrisIcons.Log.Level.SEVERE)