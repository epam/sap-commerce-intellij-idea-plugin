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

import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerAccess
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.startOffset
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class LoggersStateView(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun renderView(loggers: Map<String, CxLoggerModel>, applyView: (CoroutineScope, JComponent) -> Unit) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            val view = panel {
                row {
                    scrollCell(dataView(loggers))
                        .resizableColumn()
                        .align(Align.FILL)
                }
                    .resizableRow()
            }

            applyView(this, view)
        }
    }

    private fun dataView(loggers: Map<String, CxLoggerModel>): JComponent = panel {
        val editable = AtomicBooleanProperty(true)

        row {
            label("Effective level")

            label("Logger")
            label("")

            label("Parent")
        }
            .bottomGap(BottomGap.SMALL)
            .layout(RowLayout.PARENT_GRID)

        loggers.values
//            .sortedWith(compareBy({ it.parentName }, { it.name }))
            .sortedBy { it.name }
            .forEach { cxLogger ->
                row {
                    val model = DefaultComboBoxModel<LogLevel>().apply {
                        LogLevel.entries
                            .filter { it != LogLevel.CUSTOM || (cxLogger.level == LogLevel.CUSTOM) }
                            .forEach { addElement(it) }
                    }
                    comboBox(
                        model,
                        renderer = SimpleListCellRenderer.create { label, value, _ ->
                            if (value != null) {
                                label.icon = value.icon
                                label.text = value.name
                            }
                        }
                    )
                        .align(AlignX.FILL)
                        .bindItem({ cxLogger.level }, { _ -> })
                        .enabledIf(editable)
                        .component
                        .addItemListener { event ->
                            val currentCxLogger = loggers[cxLogger.name] ?: return@addItemListener
                            val newLogLevel = event.item.asSafely<LogLevel>() ?: return@addItemListener

                            if (currentCxLogger.level != newLogLevel) {
                                editable.set(false)
                                CxLoggerAccess.getInstance(project).setLogger(cxLogger.name, newLogLevel)
                            }
                        }

                    icon(cxLogger.icon)
                        .gap(RightGap.SMALL)

                    if (cxLogger.resolved) {
                        link(cxLogger.name) {
                            cxLogger.psiElementPointer?.element?.let { psiElement ->
                                when (psiElement) {
                                    is PsiPackage -> {
                                        coroutineScope.launch {
                                            val directory = readAction {
                                                psiElement.getDirectories(GlobalSearchScope.allScope(project))
                                                    .firstOrNull()
                                            } ?: return@launch

                                            edtWriteAction {
                                                ProjectView.getInstance(project).selectPsiElement(directory, true)
                                            }
                                        }
                                    }

                                    is PsiClass -> PsiNavigationSupport.getInstance()
                                        .createNavigatable(project, psiElement.containingFile.virtualFile, psiElement.startOffset)
                                        .navigate(true)
                                }
                            }
                        }
                    } else {
                        label(cxLogger.name)
                    }

                    label(cxLogger.parentName ?: "")
                }
                    .layout(RowLayout.PARENT_GRID)
            }
    }
        .apply {
            border = JBUI.Borders.empty(0, 16, 16, 16)
        }

    companion object {
        fun getInstance(project: Project): LoggersStateView = project.service()
    }

}