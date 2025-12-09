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
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.CxLogService
import sap.commerce.toolset.logging.CxLogUiConstants
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.ui.actionButton
import sap.commerce.toolset.ui.addItemListener
import javax.swing.JPanel

class CxCustomLogTemplatesView(private val project: Project) : Disposable {

    private var templateUUID: String = ""

    private var job = SupervisorJob()
    private var coroutineScope = CoroutineScope(Dispatchers.Default + job)

    private val showNoLoggerTemplates = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val initialized = AtomicBooleanProperty(true)
    private val canApply = AtomicBooleanProperty(false)
    private val editable = AtomicBooleanProperty(true)

    private lateinit var dPanel: DialogPanel
    private lateinit var loggerLevelField: ComboBox<CxLogLevel>
    private lateinit var loggerNameField: JBTextField
    private lateinit var dataScrollPane: JBScrollPane

    private val panel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    cellNoData(showNoLoggerTemplates, "No Logger Templates")
                }
                    .visibleIf(showNoLoggerTemplates)
                    .resizableRow()
                    .topGap(TopGap.MEDIUM)

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
            }
                .apply {
                    border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
                    dPanel = this
                    editable.set(true)

                    registerValidators(this@CxCustomLogTemplatesView) { validations ->
                        canApply.set(validations.values.all { it.okEnabled })
                    }
                }
        }
    }

    val view: DialogPanel
        get() = panel.value

    override fun dispose() {
        job.cancel()
        panel.drop()
    }

    fun render(templateUUID: String, loggers: Map<String, CxLoggerPresentation>) {
        initialized.set(false)

        this.templateUUID = templateUUID
        loggerLevelField.selectedItem = CxLogService.getInstance(project).findTemplate(templateUUID)?.defaultEffectiveLevel ?: CxLogLevel.ALL

        renderLoggersInternal(loggers)
    }

    private fun createLoggersPanel(data: Collection<CxLoggerPresentation>) = panel {
        data.forEach { cxLogger ->
            row {
                logLevelComboBox()
                    .bindItem({ cxLogger.level }, { _ -> })
                    .addItemListener(this@CxCustomLogTemplatesView) { event ->
                        val newLevel = event.item.asSafely<CxLogLevel>() ?: return@addItemListener
                        CxLogService.getInstance(project).updateLogger(templateUUID, cxLogger.name, newLevel)
                    }

                lazyLoggerDetails(project, coroutineScope, cxLogger)

                actionButton(
                    ActionManager.getInstance().getAction("sap.cx.loggers.delete.logger"), sinkExtender = fun(it: DataSink) {
                        it[CxLogUiConstants.DataKeys.TemplateUUID] = templateUUID
                        it[CxLogUiConstants.DataKeys.LoggerName] = cxLogger.name
                    }
                )
                    .customize(UnscaledGaps(0, 0, 0, 25))
            }
        }
    }

    private fun renderLoggersInternal(loggers: Map<String, CxLoggerPresentation>) {
        initLazyRenderingScope()

        val view = if (loggers.isEmpty()) noLoggersView(
            "Please, use the panel above to add a logger.",
            EditorNotificationPanel.Status.Info,
        )
        else createLoggersPanel(loggers.values)

        val viewport = dataScrollPane.getViewport()
        val pos = viewport.getViewPosition()

        dataScrollPane.setViewportView(view)

        invokeLater {
            viewport.viewPosition = pos
        }

        toggleView(showDataPanel, initialized)
    }

    private fun initLazyRenderingScope() {
        job.cancel()
        job = SupervisorJob()
        coroutineScope = CoroutineScope(Dispatchers.Default + job)
    }

    private fun newLoggerPanel(): DialogPanel = panel {
        row {
            loggerLevelField = logLevelComboBox().component

            loggerNameField = newLoggerTextField(this@CxCustomLogTemplatesView) {
                applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as CxLogLevel)
            }
                .component

            button("Apply Logger") {
                applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as CxLogLevel)
            }
        }
            .visibleIf(showDataPanel)
            .layout(RowLayout.PARENT_GRID)

        row {
            label("Effective level")
                .bold()
            label("Logger (package or class name)")
                .bold()
                .align(AlignX.FILL)
        }
            .layout(RowLayout.PARENT_GRID)
            .visibleIf(showDataPanel)
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) {
        listOf(
            showNoLoggerTemplates,
            showDataPanel,
            initialized
        )
            .forEach { it.set(unhide.contains(it)) }
    }

    private fun applyNewLogger(newLoggerPanel: DialogPanel, logger: String, effectiveLevel: CxLogLevel) {
        canApply.set(newLoggerPanel.validateAll().all { it.okEnabled })

        if (!canApply.get()) return

        CxLogService.getInstance(project).addLogger(templateUUID, logger, effectiveLevel)
        resetInputs()
    }

    private fun resetInputs() {
        loggerNameField.text = ""
        loggerLevelField.selectedItem = CxLogLevel.ALL
    }
}
