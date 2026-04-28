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
import com.intellij.ui.AnimatedIcon
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
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.exec.CxRemotePropertyStatePage
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.actionButton
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Timer

class CxRemotePropertyStateView(private val project: Project) : Disposable {
    private val showFetchProperties = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val showFetchingState = AtomicBooleanProperty(false)
    private val canApply = AtomicBooleanProperty(false)
    private val hasPreviousPage = AtomicBooleanProperty(false)
    private val hasNextPage = AtomicBooleanProperty(false)

    private lateinit var dataScrollPane: JBScrollPane
    private lateinit var keyFilterField: JBTextField
    private lateinit var valueFilterField: JBTextField
    private lateinit var addKeyField: JBTextField
    private lateinit var addValueField: JBTextField
    private lateinit var paginationLabel: JLabel
    private lateinit var fetchingLabel: JLabel

    private lateinit var currentConnection: HacConnectionSettingsState
    private var statePage: CxRemotePropertyStatePage? = null
    private var properties: List<CxPropertyPresentation> = emptyList()
    private var editingPropertyKey: String? = null
    private var editingPropertyValue: String = ""
    private val filterDebounceTimer = Timer(FILTER_DEBOUNCE_MS, ActionListener { fetchFilteredPage() }).apply {
        isRepeats = false
    }

    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute(): DialogPanel {
                lateinit var dPanel: DialogPanel

                return panel {
                row {
                    text("Fetch Properties")
                        .align(Align.CENTER)
                        .resizableColumn()
                }.visibleIf(showFetchProperties)
                    .resizableRow()

                row {
                    fetchingLabel = label("").component.apply {
                        icon = AnimatedIcon.Default.INSTANCE
                    }
                    cell(fetchingLabel)
                        .align(AlignX.FILL)
                }.visibleIf(showFetchingState)
                    .layout(RowLayout.PARENT_GRID)

                row {
                    keyFilterField = textField()
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .applyToComponent {
                            emptyText.text = "Filter by key"
                            document.addDocumentListener(object : javax.swing.event.DocumentListener {
                                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                            })
                        }
                        .component

                    valueFilterField = textField()
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .applyToComponent {
                            emptyText.text = "Filter by value"
                            document.addDocumentListener(object : javax.swing.event.DocumentListener {
                                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = onFilterChanged()
                            })
                        }
                        .component
                }.visibleIf(showDataPanel)
                    .layout(RowLayout.PARENT_GRID)

                row {
                    button("Previous") {
                        setFetching(true)
                        statePage
                            ?.takeIf { it.page > 1 }
                            ?.let {
                                CxRemotePropertyStateService.getInstance(project)
                                    .fetch(
                                        page = it.page - 1,
                                        server = currentConnection,
                                        pageSize = it.pageSize,
                                        keyFilter = it.keyFilter,
                                        valueFilter = it.valueFilter,
                                    )
                            }
                    }.enabledIf(hasPreviousPage)

                    paginationLabel = label("").component

                    button("Next") {
                        setFetching(true)
                        statePage
                            ?.takeIf { it.page < it.totalPages }
                            ?.let {
                                CxRemotePropertyStateService.getInstance(project)
                                    .fetch(
                                        page = it.page + 1,
                                        server = currentConnection,
                                        pageSize = it.pageSize,
                                        keyFilter = it.keyFilter,
                                        valueFilter = it.valueFilter,
                                    )
                            }
                    }.enabledIf(hasNextPage)
                }.visibleIf(showDataPanel)
                    .layout(RowLayout.PARENT_GRID)

                row {
                    addKeyField = textField()
                        .label("Key:")
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .validationOnInput {
                            validatePropertyKey(it.text)
                        }
                        .validationOnApply {
                            validatePropertyKey(it.text)
                        }
                        .component

                    addValueField = textField()
                        .label("Value:")
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .component

                    button("Apply Property") {
                        canApply.set(dPanel.validateAll().all { it.okEnabled })
                        if (!canApply.get()) return@button

                        setFetching(true)
                        CxRemotePropertyStateService.getInstance(project).upsertProperty(currentConnection, addKeyField.text.trim(), addValueField.text) { ok ->
                            if (ok) {
                                addKeyField.text = ""
                                addValueField.text = ""
                            }
                        }
                    }
                }.visibleIf(showDataPanel)
                    .layout(RowLayout.PARENT_GRID)

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

    override fun dispose() {
        filterDebounceTimer.stop()
        lazyViewPanel.drop()
    }

    suspend fun render(
        coroutineScope: CoroutineScope,
        connection: HacConnectionSettingsState,
        statePage: CxRemotePropertyStatePage?,
    ): JComponent {
        val viewPanel = lazyViewPanel.value
        val service = CxRemotePropertyStateService.getInstance(project)

        if (statePage == null) {
            currentConnection = connection
            withContext(Dispatchers.EDT) {
                fetchingLabel.text = "Fetching data from '${connection.shortenConnectionName}'"
                setFetching(service.isFetching(connection))
                toggleView(if (service.isFetching(connection)) showFetchingState else showFetchProperties)
            }
            return viewPanel
        }

        val connectionChanged = !::currentConnection.isInitialized || currentConnection.uuid != connection.uuid
        currentConnection = connection
        this.statePage = statePage
        this.properties = statePage.properties.values.sortedBy { it.key }
        if (editingPropertyKey !in this.properties.map { it.key }.toSet()) {
            editingPropertyKey = null
            editingPropertyValue = ""
        }
        hasPreviousPage.set(statePage.page > 1)
        hasNextPage.set(statePage.page < statePage.totalPages)
        setFetching(service.isFetching(connection))
        toggleView(showDataPanel)
        withContext(Dispatchers.EDT) {
            if (connectionChanged) {
                keyFilterField.text = statePage.keyFilter
                valueFilterField.text = statePage.valueFilter
            }
            fetchingLabel.text = "Fetching data from '${connection.shortenConnectionName}'"
            paginationLabel.text = "Page ${statePage.page} of ${statePage.totalPages} | ${statePage.totalItems} total"
            renderData()
        }

        return withContext(Dispatchers.EDT) { viewPanel }
    }

    private fun refreshDataView() {
        if (!::dataScrollPane.isInitialized) return
        renderData()
    }

    private fun onFilterChanged() {
        refreshDataView()
        filterDebounceTimer.restart()
    }

    private fun renderData() {
        val filtered = properties.filter { property ->
            val keyNeedle = keyFilterField.text.trim()
            val valueNeedle = valueFilterField.text.trim()
            (keyNeedle.isBlank() || property.key.contains(keyNeedle, ignoreCase = true)) &&
                (valueNeedle.isBlank() || property.value.contains(valueNeedle, ignoreCase = true))
        }

        val view = if (filtered.isEmpty()) {
            noDataView(
                if (properties.isEmpty()) "No properties available on this page."
                else "No properties match the current filter."
            )
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
                            setFetching(true)
                            editingPropertyKey = null
                            editingPropertyValue = ""
                            CxRemotePropertyStateService.getInstance(project).upsertProperty(currentConnection, property.key, valueField.text)
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
                            setFetching(true)
                            CxRemotePropertyStateService.getInstance(project).deleteProperty(currentConnection, property.key)
                        }
                    })
                }.layout(RowLayout.PARENT_GRID)
            }
        }

        dataScrollPane.setViewportView(view)
    }

    private fun noDataView(text: String) = panel {
        row {
            label(text)
                .align(Align.CENTER)
                .resizableColumn()
        }.resizableRow()
    }

    private fun validatePropertyKey(value: String): ValidationInfo? = when {
        value.isBlank() -> ValidationInfo("Property key is not allowed to be empty")
        value.any(Char::isWhitespace) -> ValidationInfo("Property key cannot contain whitespace")
        else -> null
    }

    private fun fetchFilteredPage() {
        val currentStatePage = statePage ?: return
        val keyFilter = keyFilterField.text.trim()
        val valueFilter = valueFilterField.text.trim()
        if (currentStatePage.keyFilter == keyFilter && currentStatePage.valueFilter == valueFilter) return

        editingPropertyKey = null
        editingPropertyValue = ""
        setFetching(true)
        CxRemotePropertyStateService.getInstance(project).fetch(
            server = currentConnection,
            page = 1,
            pageSize = currentStatePage.pageSize,
            keyFilter = keyFilter,
            valueFilter = valueFilter,
        )
    }

    private fun setFetching(fetching: Boolean) {
        showFetchingState.set(fetching)
    }

    private fun syncEditingValue(value: String) {
        editingPropertyValue = value
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showFetchProperties, showDataPanel, showFetchingState)
        .forEach { it.set(unhide.contains(it)) }

    companion object {
        private const val FILTER_DEBOUNCE_MS = 500
    }
}
