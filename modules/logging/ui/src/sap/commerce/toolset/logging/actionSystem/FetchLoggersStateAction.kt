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

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.CxLoggerAccess

class FetchLoggersStateAction : AnAction() {

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
        e.presentation.disabledIcon = if (loggerAccess.ready) null else AnimatedIcon.Default.INSTANCE
    }
}