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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class CxLoggerAction(private val logLevel: LogLevel) : AnAction() {

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

        CxLoggerAccess.getInstance(project).setLogger(logIdentifier, logLevel)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        super.update(e)
        val project = e.project ?: return

        e.presentation.text = logLevel.name
        e.presentation.icon = logLevel.icon
        e.presentation.isEnabled = CxLoggerAccess.getInstance(project).ready
    }
}

class AllLoggerAction : CxLoggerAction(LogLevel.ALL)
class OffLoggerAction : CxLoggerAction(LogLevel.OFF)
class TraceLoggerAction : CxLoggerAction(LogLevel.TRACE)
class DebugLoggerAction : CxLoggerAction(LogLevel.DEBUG)
class InfoLoggerAction : CxLoggerAction(LogLevel.INFO)
class WarnLoggerAction : CxLoggerAction(LogLevel.WARN)
class ErrorLoggerAction : CxLoggerAction(LogLevel.ERROR)
class FatalLoggerAction : CxLoggerAction(LogLevel.FATAL)
class SevereLoggerAction : CxLoggerAction(LogLevel.SEVERE)

class FetchLoggerStateAction : AnAction(HybrisIcons.Log.Action.FETCH) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val loggerAccessService = CxLoggerAccess.getInstance(project)

        loggerAccessService.fetch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val loggerAccess = CxLoggerAccess.getInstance(project)

        e.presentation.isEnabled = loggerAccess.ready

        val state = CxLoggerAccess.getInstance(project).stateInitialized

        e.presentation.text = if (state) "Refresh Loggers State" else "Fetch Loggers State"
        e.presentation.icon = if (state) HybrisIcons.Log.Action.REFRESH else HybrisIcons.Log.Action.FETCH
    }
}