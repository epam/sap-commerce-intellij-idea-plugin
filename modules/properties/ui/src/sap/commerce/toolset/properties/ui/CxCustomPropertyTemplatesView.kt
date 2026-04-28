/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.custom.CxCustomPropertyTemplateService
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.actionButton
import javax.swing.JComponent
import javax.swing.JPanel

class CxCustomPropertyTemplatesView(private val project: Project) : Disposable {
    private var templateUUID: String = ""
    private val showDataPanel = AtomicBooleanProperty(false)
    private val canApply = AtomicBooleanProperty(false)

    private lateinit var dataScrollPane: JBScrollPane
    private lateinit var keyFilterField: JBTextField
    private lateinit var valueFilterField: JBTextField
    private lateinit var addKeyField: JBTextField
    private lateinit var addValueField: JBTextField

    private var properties: List<CxPropertyPresentation> = emptyList()
    private var editingPropertyKey: String? = null
    private var editingPropertyValue: String = ""

    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute(): DialogPanel {
                lateinit var dPanel: DialogPanel
                return panel {
                    row {
                        keyFilterField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent {
                                emptyText.text = "Filter by key"
                                document.addDocumentListener(object : javax.swing.event.DocumentListener {
                                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                })
                            }
                            .component

                        valueFilterField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent {
                                emptyText.text = "Filter by value"
                                document.addDocumentListener(object : javax.swing.event.DocumentListener {
                                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = refreshDataView()
                                })
                            }
                            .component
                    }.visibleIf(showDataPanel).layout(RowLayout.PARENT_GRID)

                    row {
                        addKeyField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .validationOnInput { validatePropertyKey(it.text) }
                            .validationOnApply { validatePropertyKey(it.text) }
                            .applyToComponent {
                                emptyText.text = "Key"
                            }
                            .component

                        addValueField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent {
                                emptyText.text = "Value"
                            }
                            .component

                        button("Apply Property") {
                            canApply.set(dPanel.validateAll().all { it.okEnabled })
                            if (!canApply.get()) return@button

                            CxCustomPropertyTemplateService.getInstance(project)
                                .addProperty(templateUUID, addKeyField.text.trim(), addValueField.text)
                            addKeyField.text = ""
                            addValueField.text = ""
                        }
                    }.visibleIf(showDataPanel).layout(RowLayout.PARENT_GRID)

                    separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                        .visibleIf(showDataPanel)

                    row {
                        dataScrollPane = JBScrollPane(JPanel()).apply { border = null }
                        cell(dataScrollPane).align(Align.FILL).visibleIf(showDataPanel)
                    }.resizableRow()
                }.apply {
                    border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
                    dPanel = this
                }
            }
        }
    }

    override fun dispose() = lazyViewPanel.drop()

    suspend fun render(coroutineScope: CoroutineScope, templateUUID: String, properties: Collection<CxPropertyPresentation>): JComponent {
        this.templateUUID = templateUUID
        this.properties = properties.sortedBy { it.key }
        if (editingPropertyKey !in this.properties.map { it.key }.toSet()) {
            editingPropertyKey = null
            editingPropertyValue = ""
        }
        val viewPanel = lazyViewPanel.value

        toggleView(showDataPanel)
        withContext(Dispatchers.EDT) { renderData() }
        return viewPanel
    }

    private fun refreshDataView() {
        if (!::dataScrollPane.isInitialized) return
        renderData()
    }

    private fun renderData() {
        val keyNeedle = keyFilterField.text.trim()
        val valueNeedle = valueFilterField.text.trim()
        val filtered = properties.filter { property ->
            (keyNeedle.isBlank() || property.key.contains(keyNeedle, ignoreCase = true)) &&
                (valueNeedle.isBlank() || property.value.contains(valueNeedle, ignoreCase = true))
        }

        val view = if (filtered.isEmpty()) {
            panel {
                row {
                    label(
                        if (properties.isEmpty()) "Please, use the panel above to add a property."
                        else "No properties match the current filter."
                    )
                        .align(Align.CENTER)
                        .resizableColumn()
                }.resizableRow()
            }
        } else panel {
            row {
                label("Key").bold()
                label("Value").bold().align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            filtered.forEach { property ->
                row {
                    label(property.key)

                    if (editingPropertyKey == property.key) {
                        val valueField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent { text = editingPropertyValue }
                            .applyToComponent {
                                document.addDocumentListener(object : javax.swing.event.DocumentListener {
                                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = syncEditingValue(text)
                                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = syncEditingValue(text)
                                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = syncEditingValue(text)
                                })
                            }
                            .component

                        button("Apply") {
                            editingPropertyKey = null
                            editingPropertyValue = ""
                            CxCustomPropertyTemplateService.getInstance(project)
                                .updateProperty(templateUUID, property.key, valueField.text)
                        }
                    } else {
                        label(property.value).align(AlignX.FILL).resizableColumn()

                        actionButton(object : AnAction(null, "Edit property", HybrisIcons.Connection.EDIT) {
                            override fun actionPerformed(e: AnActionEvent) {
                                editingPropertyKey = property.key
                                editingPropertyValue = property.value
                                renderData()
                            }
                        })
                    }

                    actionButton(object : AnAction(null, "Delete property", HybrisIcons.Log.Action.DELETE) {
                        override fun actionPerformed(e: AnActionEvent) {
                            if (editingPropertyKey == property.key) {
                                editingPropertyKey = null
                                editingPropertyValue = ""
                            }
                            CxCustomPropertyTemplateService.getInstance(project)
                                .deleteProperty(templateUUID, property.key)
                        }
                    })
                }.layout(RowLayout.PARENT_GRID)
            }
        }

        dataScrollPane.setViewportView(view)
    }

    private fun validatePropertyKey(value: String): ValidationInfo? = when {
        value.isBlank() -> ValidationInfo("Property key is not allowed to be empty")
        value.any(Char::isWhitespace) -> ValidationInfo("Property key cannot contain whitespace")
        else -> null
    }

    private fun syncEditingValue(value: String) {
        editingPropertyValue = value
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showDataPanel)
        .forEach { it.set(unhide.contains(it)) }
}
