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

import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class LoggersStateView(private val project: Project, private val coroutineScope: CoroutineScope) {

    private val editable = AtomicBooleanProperty(true)

    fun renderView(loggers: Map<String, CxLoggerModel>, applyView: (CoroutineScope, JComponent) -> Unit) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            val view = panel {
                row {
                    scrollCell(
                        dataView(loggers)
                    )
                        .resizableColumn()
                        .align(Align.FILL)
                }
                    .resizableRow()
                    .bottomGap(BottomGap.SMALL)
            }
                .apply {
                    border = JBUI.Borders.empty(0, 16, 16, 16)
                }

            applyView(this, view)
        }
    }

    private fun dataView(loggers: Map<String, CxLoggerModel>): JComponent = panel {
        row {
            label("Effective level")

            label("Logger")
            label("")

            label("Parent")
        }
            .bottomGap(BottomGap.SMALL)
            .layout(RowLayout.PARENT_GRID)

        loggers.forEach { (_, cxLogger) ->
            row {
                comboBox(
                    EnumComboBoxModel(LogLevel::class.java),
                    renderer = SimpleListCellRenderer.create { label, value, _ ->
                        if (value != null) {
                            label.icon = value.icon
                            label.text = value.name
                        }
                    }
                )
                    .bindItem({ cxLogger.level }, { level ->
                        level?.let {

                        }
                    })
                    .enabledIf(editable)
                    .comment(if (cxLogger.level == LogLevel.CUSTOM) "custom: ${cxLogger.effectiveLevel}" else null)

                icon(cxLogger.icon)
                    .gap(RightGap.SMALL)
                label(cxLogger.name)

                label(cxLogger.parentName ?: "")
            }
                .layout(RowLayout.PARENT_GRID)
        }
    }

    companion object {
        fun getInstance(project: Project): LoggersStateView = project.service()
    }

}