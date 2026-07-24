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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxPropertyConstants
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.exec.CxRemotePropertyStatePage
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import java.awt.event.AdjustmentEvent
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Timer

class CxRemotePropertyStateView(private val project: Project) : Disposable {
    private val showFetchProperties = AtomicBooleanProperty(false)
    private val showDataPanel = AtomicBooleanProperty(false)
    private val showFetchingState = AtomicBooleanProperty(false)
    private val canApply = AtomicBooleanProperty(false)

    private val listModel = CollectionListModel<CxPropertyPresentation>()
    private val propertyList = CxPropertyList(
        parentDisposable = this,
        model = listModel,
        onEditClicked = { startInlineEdit(it) },
        onDeleteClicked = { confirmAndDelete(it) },
    ).apply {
        putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
    }

    private lateinit var dataScrollPane: JBScrollPane
    private lateinit var keyFilterField: JBTextField
    private lateinit var valueFilterField: JBTextField
    private lateinit var addKeyField: JBTextField
    private lateinit var addValueField: JBTextField
    private lateinit var statusLabel: JLabel
    private lateinit var bottomLoadingLabel: JLabel
    private lateinit var fetchingLabel: JLabel

    private lateinit var currentConnection: HacConnectionSettingsState
    private var statePage: CxRemotePropertyStatePage? = null
    private var lastSeenLoadedCount: Int = -1
    private var lastSeenFilterSignature: String = ""

    private val filterDebounceTimer = Timer(FILTER_DEBOUNCE_MS, ActionListener { fetchFilteredPage() }).apply {
        isRepeats = false
    }

    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute(): DialogPanel {
                lateinit var dPanel: DialogPanel

                return panel {
                    // --- Fetch prompt / generic fetching banner ---
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
                        cell(fetchingLabel).align(AlignX.FILL)
                    }.visibleIf(showFetchingState)
                        .layout(RowLayout.PARENT_GRID)

                    // --- Apply Property (add new) — moved to the top of the panel ---
                    row {
                        addKeyField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .validationOnInput { validatePropertyKey(it.text) }
                            .validationOnApply { validatePropertyKey(it.text) }
                            .applyToComponent { emptyText.text = "Key" }
                            .component

                        addValueField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent { emptyText.text = "Value" }
                            .component

                        button("Apply Property") {
                            canApply.set(dPanel.validateAll().all { it.okEnabled })
                            if (!canApply.get()) return@button

                            setFetching(true)
                            CxRemotePropertyStateService.getInstance(project)
                                .upsertProperty(currentConnection, addKeyField.text.trim(), addValueField.text) { ok ->
                                    if (ok) {
                                        addKeyField.text = ""
                                        addValueField.text = ""
                                    }
                                }
                        }
                    }.visibleIf(showDataPanel)
                        .layout(RowLayout.PARENT_GRID)

                    // --- Filters: labels above their inputs ---
                    row {
                        label("Filter by key:")
                        label("Filter by value:")
                    }.visibleIf(showDataPanel)
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

                    separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                        .visibleIf(showDataPanel)

                    // --- Data table ---
                    row {
                        dataScrollPane = JBScrollPane(propertyList).apply {
                            border = null
                            background = propertyList.background
                            viewport.background = propertyList.background
                            verticalScrollBar.unitIncrement = JBUI.scale(SCROLL_UNIT_INCREMENT)
                            verticalScrollBar.addAdjustmentListener(::onScrollChanged)
                            setColumnHeaderView(buildColumnHeader(propertyList.background))
                        }
                        cell(dataScrollPane).align(Align.FILL).visibleIf(showDataPanel)
                    }.resizableRow()

                    // --- Bottom status toolbar: progress on the left, "Loaded N of M" on the right ---
                    row {
                        cell(buildBottomToolbar()).align(AlignX.FILL).resizableColumn()
                    }.visibleIf(showDataPanel)
                }.apply {
                    border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
                    dPanel = this
                }
            }
        }
    }

    override fun dispose() {
        filterDebounceTimer.stop()
        propertyList.cancelEdit()
        lazyViewPanel.drop()
    }

    suspend fun render(
        @Suppress("UNUSED_PARAMETER") coroutineScope: CoroutineScope,
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
                propertyList.cancelEdit()
                listModel.removeAll()
                lastSeenLoadedCount = -1
                lastSeenFilterSignature = ""
            }
            return viewPanel
        }

        val connectionChanged = !::currentConnection.isInitialized || currentConnection.uuid != connection.uuid
        currentConnection = connection
        this.statePage = statePage

        withContext(Dispatchers.EDT) {
            if (connectionChanged) {
                keyFilterField.text = statePage.keyFilter
                valueFilterField.text = statePage.valueFilter
                propertyList.cancelEdit()
            }
            fetchingLabel.text = "Fetching data from '${connection.shortenConnectionName}'"
            statusLabel.text = "Loaded ${statePage.loadedCount} of ${statePage.totalItems} total"
            setFetching(service.isFetching(connection))
            toggleView(showDataPanel)

            // Only adopt the snapshot if it matches what the user is currently filtering for —
            // a stale broadcast (e.g. the first publish of a still-running filter fetch) would
            // otherwise repopulate the list with results for the previous filter.
            val filtersMatch = statePage.keyFilter == keyFilterField.text.trim()
                && statePage.valueFilter == valueFilterField.text.trim()
            if (filtersMatch) syncListModel(connectionChanged, statePage)

            bottomLoadingLabel.isVisible = service.isFetching(connection)
        }

        return withContext(Dispatchers.EDT) { viewPanel }
    }

    /**
     * Syncs [listModel] with the latest server state.
     *
     * - Connection or filter change: rebuild the model from scratch.
     * - Pure append (loaded count grew, filter unchanged): add the new items at the end.
     * - Other content changes (mutations, deletes): rebuild the model.
     */
    private fun syncListModel(connectionChanged: Boolean, page: CxRemotePropertyStatePage) {
        val filterSignature = "${page.keyFilter}${page.valueFilter}"
        val canAppend = !connectionChanged
            && filterSignature == lastSeenFilterSignature
            && lastSeenLoadedCount > 0
            && page.loadedCount >= lastSeenLoadedCount
            && page.properties.size == page.loadedCount

        if (canAppend && page.loadedCount > lastSeenLoadedCount) {
            val prefixMatches = (0 until lastSeenLoadedCount).all { idx ->
                idx < listModel.size && listModel.getElementAt(idx).key == page.properties[idx].key
            }
            if (prefixMatches) {
                for (idx in lastSeenLoadedCount until page.properties.size) {
                    listModel.add(page.properties[idx])
                }
                lastSeenLoadedCount = page.loadedCount
                lastSeenFilterSignature = filterSignature
                return
            }
        }

        listModel.replaceAll(page.properties)
        lastSeenLoadedCount = page.loadedCount
        lastSeenFilterSignature = filterSignature
    }

    private fun onScrollChanged(@Suppress("UNUSED_PARAMETER") event: AdjustmentEvent) {
        val current = statePage ?: return
        if (!current.hasMore) return
        val service = CxRemotePropertyStateService.getInstance(project)
        if (service.isFetching(currentConnection)) return

        val scrollBar = dataScrollPane.verticalScrollBar
        val remaining = scrollBar.maximum - (scrollBar.value + scrollBar.visibleAmount)
        if (remaining <= JBUI.scale(SCROLL_TRIGGER_THRESHOLD)) {
            bottomLoadingLabel.isVisible = true
            service.fetchNextPage(currentConnection)
        }
    }

    private fun onFilterChanged() {
        filterDebounceTimer.restart()
    }

    private fun fetchFilteredPage() {
        val currentStatePage = statePage ?: return
        val keyFilter = keyFilterField.text.trim()
        val valueFilter = valueFilterField.text.trim()
        if (currentStatePage.keyFilter == keyFilter && currentStatePage.valueFilter == valueFilter) return

        propertyList.cancelEdit()
        listModel.removeAll()
        lastSeenLoadedCount = -1
        lastSeenFilterSignature = "$keyFilter$valueFilter"
        bottomLoadingLabel.isVisible = true
        setFetching(true)
        CxRemotePropertyStateService.getInstance(project).resetAndFetch(
            server = currentConnection,
            pageSize = currentStatePage.pageSize.takeIf { it > 0 } ?: CxPropertyConstants.DEFAULT_PAGE_SIZE,
            keyFilter = keyFilter,
            valueFilter = valueFilter,
        )
    }

    private fun startInlineEdit(property: CxPropertyPresentation) {
        propertyList.beginEdit(property) { newValue ->
            // Apply with an unchanged value would hit the backend, fire a confirmation toast,
            // and trigger a no-op refetch — skip the round-trip when nothing changed.
            if (newValue == property.value) return@beginEdit
            setFetching(true)
            CxRemotePropertyStateService.getInstance(project)
                .upsertProperty(currentConnection, property.key, newValue)
        }
    }

    private fun confirmAndDelete(property: CxPropertyPresentation) {
        val confirmed = Messages.showYesNoDialog(
            project,
            "Delete property '${property.key}' from '${currentConnection.shortenConnectionName}'?",
            "Delete Property",
            Messages.getQuestionIcon(),
        ) == Messages.YES
        if (!confirmed) return

        propertyList.cancelEdit()
        setFetching(true)
        CxRemotePropertyStateService.getInstance(project).deleteProperty(currentConnection, property.key)
    }

    private fun validatePropertyKey(value: String): ValidationInfo? = when {
        value.isBlank() -> ValidationInfo("Property key is not allowed to be empty")
        value.any(Char::isWhitespace) -> ValidationInfo("Property key cannot contain whitespace")
        else -> null
    }

    private fun setFetching(fetching: Boolean) {
        showFetchingState.set(fetching)
        if (!fetching && ::bottomLoadingLabel.isInitialized) bottomLoadingLabel.isVisible = false
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showFetchProperties, showDataPanel, showFetchingState)
        .forEach { it.set(unhide.contains(it)) }

    /**
     * Builds a column-header strip that mirrors [CxPropertyRenderer]'s GridBag layout, so the
     * "Key" and "Value" labels line up with the underlying cell columns.
     */
    private fun buildColumnHeader(bg: Color): JComponent {
        val gap = JBUI.scale(COLUMN_GAP)
        val header = JPanel(GridBagLayout()).apply {
            isOpaque = true
            background = bg
            border = JBUI.Borders.empty(HEADER_VERTICAL_PADDING, HEADER_HORIZONTAL_PADDING)
        }

        val keyHeader = JLabel("Key").apply { font = font.deriveFont(Font.BOLD) }
        val valueHeader = JLabel("Value").apply { font = font.deriveFont(Font.BOLD) }

        header.add(keyHeader, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, 0, 0, gap / 2)
        })
        header.add(valueHeader, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, gap / 2, 0, JBUI.scale(HEADER_ACTION_RESERVED_WIDTH))
        })
        header.add(Box.createHorizontalStrut(JBUI.scale(HEADER_ACTION_RESERVED_WIDTH)),
            GridBagConstraints().apply {
                gridx = 2; gridy = 0
                weightx = 0.0
                fill = GridBagConstraints.NONE
            })
        return header
    }

    /**
     * Bottom toolbar — sits below the scrolling area and shows summary status. The "Loading…"
     * label only appears while a fetch is in flight; the right-aligned label shows the
     * "Loaded N of M total" counter.
     */
    private fun buildBottomToolbar(): JComponent {
        val bg = UIUtil.getPanelBackground()
        val toolbar = JPanel(GridBagLayout()).apply {
            isOpaque = true
            background = bg
            border = JBUI.Borders.compound(
                JBUI.Borders.customLineTop(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR),
                JBUI.Borders.empty(TOOLBAR_VERTICAL_PADDING, TOOLBAR_HORIZONTAL_PADDING),
            )
        }

        bottomLoadingLabel = JLabel("Loading…").apply {
            icon = AnimatedIcon.Default.INSTANCE
            isVisible = false
        }
        statusLabel = JLabel("")

        toolbar.add(bottomLoadingLabel, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 1.0
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
        })
        toolbar.add(statusLabel, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.0
            anchor = GridBagConstraints.EAST
        })
        return toolbar
    }

    companion object {
        private const val FILTER_DEBOUNCE_MS = 500
        private const val SCROLL_TRIGGER_THRESHOLD = 96
        private const val SCROLL_UNIT_INCREMENT = 16
        private const val COLUMN_GAP = 8
        private const val HEADER_VERTICAL_PADDING = 6
        private const val HEADER_HORIZONTAL_PADDING = 12
        private const val HEADER_ACTION_RESERVED_WIDTH = 68
        private const val TOOLBAR_VERTICAL_PADDING = 6
        private const val TOOLBAR_HORIZONTAL_PADDING = 12
    }
}
