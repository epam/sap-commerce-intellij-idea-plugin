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
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
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
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.InlineBanner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.LogLevel
import sap.commerce.toolset.ui.addKeyListener
import sap.commerce.toolset.ui.event.KeyListener
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.JPanel

class CustomLoggersTemplatesStateView(
    private val project: Project,
    private val coroutineScope: CoroutineScope
) : Disposable {

    private val showNothingSelected = AtomicBooleanProperty(true)
    private val showNoLoggerTemplates = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val initialized = AtomicBooleanProperty(true)
    private val canApply = AtomicBooleanProperty(false)
    private val editable = AtomicBooleanProperty(true)

    private lateinit var dataScrollPane: JBScrollPane
    private val panel by lazy {
        lateinit var dPanel: DialogPanel
        lateinit var loggerLevelField: ComboBox<LogLevel>
        lateinit var loggerNameField: JBTextField

        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    loggerLevelField = comboBox(
                        model = EnumComboBoxModel(LogLevel::class.java),
                        renderer = SimpleListCellRenderer.create { label, value, _ ->
                            if (value != null) {
                                label.icon = value.icon
                                label.text = value.name
                            }
                        }
                    )
                        .component

                    loggerNameField = textField()
                        .resizableColumn()
                        .align(AlignX.FILL)
                        .validationOnInput {
                            if (it.text.isBlank()) error("Please enter a logger name")
                            else null
                        }
                        .validationOnApply {
                            if (it.text.isBlank()) error("Please enter a logger name")
                            else null
                        }
                        .addKeyListener(this@CustomLoggersTemplatesStateView, object : KeyListener {
                            override fun keyReleased(e: KeyEvent) {
                                if (e.keyCode == KeyEvent.VK_ENTER) {
                                    applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as LogLevel)
                                }
                            }
                        })
                        .component

                    button("Apply Logger") {
                        applyNewLogger(dPanel, loggerNameField.text, loggerLevelField.selectedItem as LogLevel)
                    }
                }
                    .visibleIf(showDataPanel)
                    .layout(RowLayout.PARENT_GRID)
                row {
                    cellNoData(showNoLoggerTemplates, "No Logger Templates")
                    cellNoData(showNothingSelected, IdeBundle.message("empty.text.nothing.selected"))
                }
                    .visibleIf(showNoLoggerTemplates.or(showNothingSelected))
                    .resizableRow()
                    .topGap(TopGap.MEDIUM)

                row {
                    label("Effective level")
                        .bold()
                        .applyToComponent {
                            preferredSize = Dimension(JBUI.scale(100), preferredSize.height)
                        }
                    label("Logger (package or class name)")
                        .bold()
                        .align(AlignX.FILL)
                }
                    .visibleIf(showDataPanel)

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
                }
                .apply {
                    registerValidators(this@CustomLoggersTemplatesStateView) { validations ->
                        canApply.set(validations.values.all { it.okEnabled })
                    }
                    dPanel = this
                }
                .apply {
                    editable.set(true)
                }
        }
    }

    val view: DialogPanel
        get() = panel.value

    fun renderNothingSelected() = toggleView(showNothingSelected)
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
                    "Add a First Logger.",
                    EditorNotificationPanel.Status.Info
                )
                    .showCloseButton(false)
            )
                .align(Align.CENTER)
                .resizableColumn()
        }.resizableRow()
    }

    fun createLoggersPanel(data: Collection<CxLoggerModel>) = panel {
        data.forEach { r ->
            row {
                icon(r.level.icon)
                label(r.level.name)
                    .applyToComponent {
                        preferredSize = Dimension(JBUI.scale(68), preferredSize.height)
                    }

                icon(r.icon)
                if (r.resolved) {
                    link(r.name) {
                        r.psiElementPointer?.element?.let { psiElement ->
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
                    }.resizableColumn()
                } else {
                    label(r.name)
                }
            }
        }
    }

    override fun dispose() = panel.drop()

    private fun applyNewLogger(newLoggerPanel: DialogPanel, logger: String, level: LogLevel) {
        canApply.set(newLoggerPanel.validateAll().all { it.okEnabled })

        if (!canApply.get()) return


    }
}