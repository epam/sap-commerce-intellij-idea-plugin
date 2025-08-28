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

package sap.commerce.toolset.logging.ui

import com.intellij.ide.IdeBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.or
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.logging.CxLoggerModel
import javax.swing.JPanel

class LoggersTemplatesStateView(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) : Disposable {

    private val showNothingSelected = AtomicBooleanProperty(true)
    private val showNoLoggerTemplates = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val initialized = AtomicBooleanProperty(true)
    private val canApply = AtomicBooleanProperty(false)

    private lateinit var dataScrollPane: JBScrollPane
    private val panel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    cellNoData(showNoLoggerTemplates, "No Logger Templates")
                    cellNoData(showNothingSelected, IdeBundle.message("empty.text.nothing.selected"))
                }
                    .visibleIf(showNoLoggerTemplates.or(showNothingSelected))
                    .resizableRow()
                    .topGap(TopGap.MEDIUM)

                row {
                    label("Effective level")
                        .bold().gap(RightGap.SMALL)
                    label("Logger (package or class name)")
                        .bold()
                        .align(AlignX.FILL)

                }
                    .visibleIf(showDataPanel)
                    .layout(RowLayout.PARENT_GRID)

                separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                    .visibleIf(showDataPanel)

                row {
                    //
                    dataScrollPane = JBScrollPane(JPanel())
                        .apply { border = null }

                    cell(dataScrollPane)
                        .align(Align.FILL)
                        .visibleIf(showDataPanel)
                }
                    .resizableRow()
            }
                .apply {
                    border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
                }
        }
    }

    val view: DialogPanel
        get() = panel.value

    fun renderNoLoggerTemplates() = toggleView(showNoLoggerTemplates)

    fun renderLoggersTemplate(loggers: Map<String, CxLoggerModel>) {
        initialized.set(false)

        renderLoggersInternal(loggers)
    }

    private fun renderLoggersInternal(loggers: Map<String, CxLoggerModel>) {
        val view = if (loggers.isEmpty()) noLoggersView()
        else createLoggersPanel(loggers.values)

        dataScrollPane.setViewportView(view)

        toggleView(showDataPanel, initialized)
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) {
        listOf(
            showNothingSelected,
            showNoLoggerTemplates,
            showDataPanel,
            initialized
        )
            .forEach { it.set(unhide.contains(it)) }
    }

    private fun Row.cellNoData(property: AtomicBooleanProperty, text: String) = text(text)
        .visibleIf(property)
        .align(Align.CENTER)
        .resizableColumn()

    private fun noLoggersView() = panel {
        row {
            cell(
                InlineBanner(
                    "Unable to get list of loggers for the connection.",
                    EditorNotificationPanel.Status.Warning
                )
                    .showCloseButton(false)
            )
                .align(Align.CENTER)
                .resizableColumn()
        }.resizableRow()
    }

    fun createLoggersPanel(data: Collection<CxLoggerModel>) = panel {
        data.forEach { r ->
            twoColumnsRow(
                {
                    icon(r.level.icon)
                    label(r.level.name)
                },
                {
                    icon(r.icon)
                    label(r.name)
                }
            ).layout(RowLayout.PARENT_GRID)
        }
    }


//        private fun loggersView(loggers: Map<String, CxLoggerModel>) = panel {
//
//            row {
//                cellNoData(AtomicBooleanProperty(loggers.isNotEmpty()), "").bold()
//                cellNoData(AtomicBooleanProperty(loggers.isNotEmpty()), "Effective level").bold()
//                cellNoData(AtomicBooleanProperty(loggers.isNotEmpty()), "").bold()
//                cellNoData(AtomicBooleanProperty(loggers.isNotEmpty()), "Logger (package or class name)").bold()
//            }
//                .visibleIf(initialized.and(showDataPanel))
//                .layout(RowLayout.PARENT_GRID)
//
//
//            separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
//                .visibleIf(showDataPanel.and(initialized))
//
//            loggers.values
//                .filterNot { it.inherited }
//                .sortedBy { it.name }
//                .forEach { cxLogger ->
//                    row {
//                        icon(cxLogger.level.icon)
//                            .customize(UnscaledGaps(0, 0, 0, 16))
//
//                        label(cxLogger.level.name)
//                            .align(AlignX.FILL)
//                            .enabledIf(initialized)
//
//                        icon(cxLogger.icon)
//                            .customize(UnscaledGaps(0, 0, 0, 16))
//
//                        if (cxLogger.resolved) {
//                            link(cxLogger.name) {
//                                cxLogger.psiElementPointer?.element?.let { psiElement ->
//                                    when (psiElement) {
//                                        is PsiPackage -> {
//                                            coroutineScope.launch {
//                                                val directory = readAction {
//                                                    psiElement.getDirectories(GlobalSearchScope.allScope(project))
//                                                        .firstOrNull()
//                                                } ?: return@launch
//
//                                                edtWriteAction {
//                                                    ProjectView.getInstance(project).selectPsiElement(directory, true)
//                                                }
//                                            }
//                                        }
//
//                                        is PsiClass -> PsiNavigationSupport.getInstance()
//                                            .createNavigatable(project, psiElement.containingFile.virtualFile, psiElement.startOffset)
//                                            .navigate(true)
//                                    }
//                                }
//                            }.resizableColumn()
//                        } else {
//                            label(cxLogger.name)
//                        }
//
//                        label(cxLogger.parentName ?: "")
//                    }
//                        .layout(RowLayout.PARENT_GRID)
//                }
//        }

    override fun dispose() = panel.drop()

}