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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.util.PackageUtils
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import javax.swing.Icon

abstract class AbstractLoggerAction(private val logLevel: String, val icon: Icon) : AnAction(logLevel, "", icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dataContext = e.dataContext
        val logIdentifier = dataContext.getData(HybrisConstants.LOGGER_IDENTIFIER_DATA_CONTEXT_KEY)

        if (logIdentifier == null) {
            notify(
                project,
                NotificationType.ERROR,
                "Updating the log level: Error",
                "The logger name is NULL."
            )
            return
        }

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

                        val server = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris)
                        val abbreviationLogIdentifier = PackageUtils.abbreviatePackageName(logIdentifier)

                        if (result.statusCode == 200) {
                            notify(
                                project,
                                NotificationType.INFORMATION,
                                "Updating the log level: Success",
                                "The log level set to $logLevel for $abbreviationLogIdentifier, server $server."

                            )
                        } else {
                            notify(
                                project,
                                NotificationType.ERROR,
                                "Updating the log level: Failed",
                                "The log level is not set to $logLevel for $abbreviationLogIdentifier, server $server."
                            )
                        }
                    } finally {
                    }
                }
            })
        }
    }

    private fun notify(
        project: Project,
        notificationType: NotificationType,
        title: String,
        content: String
    ) {
        Notifications.create(
            notificationType,
            title,
            content
        )
            .hideAfter(5)
            .notify(project)
    }

}

class TraceLoggerAction : AbstractLoggerAction("TRACE", HybrisIcons.Log.Level.TRACE)
class DebugLoggerAction : AbstractLoggerAction("DEBUG", HybrisIcons.Log.Level.DEBUG)
class InfoLoggerAction : AbstractLoggerAction("INFO", HybrisIcons.Log.Level.INFO)
class WarnLoggerAction : AbstractLoggerAction("WARN", HybrisIcons.Log.Level.WARN)
class ErrorLoggerAction : AbstractLoggerAction("ERROR", HybrisIcons.Log.Level.ERROR)
class FatalLoggerAction : AbstractLoggerAction("FATAL", HybrisIcons.Log.Level.FATAL)
class SevereLoggerAction : AbstractLoggerAction("SEVERE", HybrisIcons.Log.Level.SEVERE)