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
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.util.and
import com.intellij.openapi.observable.util.or
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.startOffset
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.logging.CxLoggerAccess
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.LogLevel
import javax.swing.JPanel

class LoggersTemplatesStateView(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) : Disposable {

    private val showNothingSelected = AtomicBooleanProperty(true)
    private val showNoLogger = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val initialized = AtomicBooleanProperty(true)
    private val canApply = AtomicBooleanProperty(false)

    private lateinit var dataScrollPane: JBScrollPane
    private val panel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    cellNoData(showNoLogger, "No Logger Templates")
                    cellNoData(showNothingSelected, IdeBundle.message("empty.text.nothing.selected"))
                }
                    .visibleIf(showNoLogger.or(showNothingSelected))
                    .resizableRow()
                    .topGap(TopGap.MEDIUM)

                row {
                    cell(newLoggerPanel())
                        .visibleIf(showDataPanel)
                        .align(AlignX.FILL)
                }

                separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                    .visibleIf(showDataPanel.and(initialized))

                row {
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

    fun renderNoLoggerTemplates() = toggleView(showNoLogger)
    fun renderNothingSelected() = toggleView(showNothingSelected)

    fun renderLoggersTemplate(loggers: Map<String, CxLoggerModel>) {
        initialized.set(false)

        renderLoggersInternal(loggers)
    }

    private fun renderLoggersInternal(loggers: Map<String, CxLoggerModel>) {
        val view = if (loggers.isEmpty()) noLoggersView()
        else loggersView(loggers)

        dataScrollPane.setViewportView(view)

        toggleView(showDataPanel)
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) {
        listOf(
            showNothingSelected,
            showNoLogger,
            showDataPanel
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

    private fun loggersView(loggers: Map<String, CxLoggerModel>) = panel {
        loggers.values
            .filterNot { it.inherited }
            .sortedBy { it.name }
            .forEach { cxLogger ->
                row {
                    icon(cxLogger.level.icon)
                        .gap(RightGap.SMALL)
                    label(cxLogger.level.name)
                        .align(AlignX.FILL)
                        .enabledIf(initialized)

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

    private fun newLoggerPanel(): DialogPanel {
        lateinit var dPanel: DialogPanel
        lateinit var loggerLevelField: ComboBox<LogLevel>
        lateinit var loggerNameField: JBTextField

        return panel {
            row {

                label("Effective level")

                label("Logger (package or class name)")
                    .resizableColumn()
            }
                .visibleIf(initialized)
                .layout(RowLayout.PARENT_GRID)
        }
            .apply {
                registerValidators(this@LoggersTemplatesStateView) { validations ->
                    canApply.set(validations.values.all { it.okEnabled })
                }
                dPanel = this
            }
    }

    private fun applyNewLogger(newLoggerPanel: DialogPanel, logger: String, level: LogLevel) {
        canApply.set(newLoggerPanel.validateAll().all { it.okEnabled })

        if (!canApply.get()) return

        initialized.set(false)

        CxLoggerAccess.getInstance(project).setLogger(logger, level) { coroutineScope, _ ->
            coroutineScope.launch {
                withContext(Dispatchers.EDT) {
                    initialized.set(true)
                }
            }
        }
    }

    override fun dispose() = panel.drop()

}