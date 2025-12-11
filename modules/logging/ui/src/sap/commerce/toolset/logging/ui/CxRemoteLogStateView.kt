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

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.CxRemoteLogStateService
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.ui.addItemListener
import javax.swing.JComponent
import javax.swing.JPanel

class CxRemoteLogStateView(private val project: Project) : Disposable {

    private val showFetchLoggers = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val editable = AtomicBooleanProperty(true)
    private val canApply = AtomicBooleanProperty(false)

    private lateinit var dataScrollPane: JBScrollPane

    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    cellNoData(showFetchLoggers, "Fetch Loggers State")
                }
                    .visibleIf(showFetchLoggers)
                    .resizableRow()

                row {
                    cell(newLoggerPanel())
                        .visibleIf(showDataPanel)
                        .align(AlignX.FILL)
                }

                separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                    .visibleIf(showDataPanel)

                row {
                    dataScrollPane = JBScrollPane(JPanel())
                        .apply { border = null }

                    cell(dataScrollPane)
                        .align(Align.FILL)
                        .visibleIf(showDataPanel)
                }
                    .resizableRow()
            }.apply {
                border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
            }
        }
    }

    override fun dispose() = lazyViewPanel.drop()

    suspend fun render(coroutineScope: CoroutineScope, loggers: Collection<CxLoggerPresentation>?): JComponent {
        val viewPanel = lazyViewPanel.value
        if (loggers == null) {
            toggleView(showFetchLoggers)
            return viewPanel
        }

        if (loggers.isEmpty()) {
            val view = noLoggersView("Unable to get list of loggers for the connection.")

            withContext(Dispatchers.EDT) {
                dataScrollPane.setViewportView(view)
            }
        } else {
            val lazyLoggerRows = mutableListOf<LazyLoggerRow>()
            val viewport = dataScrollPane.getViewport()
            val pos = viewport.getViewPosition()
            val view = loggersView(loggers, lazyLoggerRows)

            lazyLoggerRows.forEach {
                lazyLoggerDetails(project, coroutineScope, it)
            }

            withContext(Dispatchers.EDT) {
                dataScrollPane.setViewportView(view)
            }

            invokeLater {
                viewport.viewPosition = pos
            }
        }

        toggleView(showDataPanel)

        return viewPanel
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showFetchLoggers, showDataPanel)
        .forEach { it.set(unhide.contains(it)) }

    private fun loggersView(loggers: Collection<CxLoggerPresentation>, lazyLoggerRows: MutableList<LazyLoggerRow>) = panel {
        loggers
            .sortedBy { it.name }
            .forEach { cxLogger ->
                row {
                    logLevelComboBox()
                        .enabledIf(editable)
                        .bindItem({ cxLogger.level }, { _ -> })
                        .addItemListener(this@CxRemoteLogStateView) { event ->
                            event.item.asSafely<CxLogLevel>()
                                ?.takeUnless { it == cxLogger.level }
                                ?.let { newLogLevel ->
                                    editable.set(false)

                                    CxRemoteLogStateService.getInstance(project).setLogger(cxLogger.name, newLogLevel) { _, _ ->
                                        editable.set(true)
                                    }
                                }
                        }

                    loggerDetailsPlaceholders(cxLogger).also { lazyLoggerRows.add(it) }
                }
                    .layout(RowLayout.PARENT_GRID)
            }
    }

    private fun newLoggerPanel(): DialogPanel {
        lateinit var dPanel: DialogPanel
        lateinit var loggerLevelField: ComboBox<CxLogLevel>
        lateinit var loggerNameField: JBTextField

        return panel {
            row {
                loggerLevelField = logLevelComboBox().component

                loggerNameField = newLoggerTextField(this@CxRemoteLogStateView) {
                    applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as CxLogLevel)
                }
                    .component

                button("Apply Logger") {
                    applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as CxLogLevel)
                }
            }
                .layout(RowLayout.PARENT_GRID)

            row {
                label("Effective level")
                    .bold()
                label("Logger (package or class name)")
                    .bold()
                    .align(AlignX.FILL)

            }
                .layout(RowLayout.PARENT_GRID)
        }
            .apply {
                registerValidators(this@CxRemoteLogStateView) { validations ->
                    canApply.set(validations.values.all { it.okEnabled })
                }
                dPanel = this
            }
    }

    private fun applyNewLogger(newLoggerPanel: DialogPanel, logger: String, level: CxLogLevel) {
        canApply.set(newLoggerPanel.validateAll().all { it.okEnabled })

        if (!canApply.get()) return

        editable.set(false)

        CxRemoteLogStateService.getInstance(project).setLogger(logger, level) { coroutineScope, _ ->
            coroutineScope.launch {
                withContext(Dispatchers.EDT) {
                    editable.set(true)
                }
            }
        }
    }

}