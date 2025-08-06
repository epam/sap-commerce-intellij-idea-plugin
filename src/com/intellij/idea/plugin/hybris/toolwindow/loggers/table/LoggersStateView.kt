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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.table

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class LoggersStateView(
    val project: Project,
    val coroutineScope: CoroutineScope
) {

    fun renderView(loggers: Map<String, CxLoggerModel>, connectionSettings: RemoteConnectionSettings, applyView: (CoroutineScope, JComponent) -> Unit) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            val view = panel {
                row {
                    scrollCell(
                        LoggersTable.of(project, loggers, connectionSettings)
                    )
                        .align(Align.FILL)
                }.resizableRow()
            }

            applyView(this, view)
        }
    }

    companion object {
        fun getInstance(project: Project): LoggersStateView = project.service()
    }

}