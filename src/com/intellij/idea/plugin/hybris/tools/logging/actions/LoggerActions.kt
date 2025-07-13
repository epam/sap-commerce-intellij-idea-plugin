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
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.util.PackageUtils
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.application

abstract class AbstractLoggerAction(private val logLevel: LogLevel) : AnAction(logLevel.name, null, logLevel.icon) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val logIdentifier = e.getData(HybrisConstants.DATA_KEY_LOGGER_IDENTIFIER)

        if (logIdentifier == null) {
            notify(
                project,
                NotificationType.ERROR,
                "Unable to change the log level",
                "Cannot retrieve a logger name."
            )
            return
        }

        application.runReadAction {
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
                                "Log level updated",
                                """
                                    <p>Level  : $logLevel</p>
                                    <p>Logger : $abbreviationLogIdentifier</p>
                                    <p>${server.shortenConnectionName()}</p>"""

                            )
                        } else {
                            notify(
                                project,
                                NotificationType.ERROR,
                                "Failed to update log level",
                                """
                                    <p>Level  : $logLevel</p>
                                    <p>Logger : $abbreviationLogIdentifier</p>
                                    <p>${server.shortenConnectionName()}</p>"""
                            )
                        }
                    } finally {
                    }
                }
            })
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val isRightPlace = "GoToAction" != e.place
        e.presentation.isEnabled = isRightPlace
        e.presentation.isVisible = isRightPlace
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

class AllLoggerAction : AbstractLoggerAction(LogLevel.ALL)
class OffLoggerAction : AbstractLoggerAction(LogLevel.OFF)
class TraceLoggerAction : AbstractLoggerAction(LogLevel.TRACE)
class DebugLoggerAction : AbstractLoggerAction(LogLevel.DEBUG)
class InfoLoggerAction : AbstractLoggerAction(LogLevel.INFO)
class WarnLoggerAction : AbstractLoggerAction(LogLevel.WARN)
class ErrorLoggerAction : AbstractLoggerAction(LogLevel.ERROR)
class FatalLoggerAction : AbstractLoggerAction(LogLevel.FATAL)
class SevereLoggerAction : AbstractLoggerAction(LogLevel.SEVERE)
