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

package sap.commerce.toolset.logging.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.CxRemoteLogStateService

abstract class CxLoggerLevelAction(private val logLevel: CxLogLevel) : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val logIdentifier = e.getData(CxLogConstants.DATA_KEY_LOGGER_IDENTIFIER)

        if (logIdentifier == null) {
            Notifications.error("Unable to change the log level", "Cannot retrieve a logger name.")
                .hideAfter(5)
                .notify(project)
            return
        }

        CxRemoteLogStateService.getInstance(project).setLogger(logIdentifier, logLevel)
    }

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        super.update(e)
        val project = e.project ?: return@ifNotFromSearchPopup

        e.presentation.text = logLevel.name
        e.presentation.icon = logLevel.icon
        e.presentation.isEnabled = CxRemoteLogStateService.getInstance(project).ready
    }
}

class CxAllLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.ALL)
class CxOffLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.OFF)
class CxTraceLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.TRACE)
class CxDebugLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.DEBUG)
class CxInfoLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.INFO)
class CxWarnLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.WARN)
class CxErrorLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.ERROR)
class CxFatalLoggerLevelAction : CxLoggerLevelAction(CxLogLevel.FATAL)
