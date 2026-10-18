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
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.AnimatedIcon
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.actionSystem.HybrisActionPlaces
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxPropertyConstants
import sap.commerce.toolset.properties.CxPropertyKey
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.exec.CxRemotePropertyStatePage
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyComparison
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyScope
import sap.commerce.toolset.properties.meta.CxPropertyWriteService
import sap.commerce.toolset.properties.meta.CxSecretKind
import sap.commerce.toolset.properties.settings.CxPropertyViewSettings
import sap.commerce.toolset.properties.settings.event.CxPropertyViewSettingsListener
import sap.commerce.toolset.properties.settings.state.CxPropertyViewMode
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addDocumentListener
import sap.commerce.toolset.ui.event.documentListener
import java.awt.Color
import java.awt.Dimension
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
    private val showSelectionActions = AtomicBooleanProperty(false)
    private val hasSelection = AtomicBooleanProperty(false)

    private val viewScope = CxPropertiesViewScopeService.getInstance(project).childScope()

    private val listModel = CollectionListModel<CxPropertyPresentation>()
    private val propertyList = CxPropertyList(
        parentDisposable = this,
        model = listModel,
        onReportClicked = { showReport(it) },
        onEditClicked = { startInlineEdit(it) },
        onDeleteClicked = { confirmAndDelete(it) },
    ).apply {
        putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        onSelectionChanged = { hasSelection.set(it.isNotEmpty()) }
    }

    private lateinit var dataScrollPane: JBScrollPane
    private lateinit var keyFilterField: JBTextField
    private lateinit var valueFilterField: JBTextField
    private lateinit var addKeyField: JBTextField
    private lateinit var addValueField: JBTextField
    private lateinit var statusLabel: JLabel
    private lateinit var bottomLoadingLabel: JLabel
    private lateinit var differingLabel: JLabel

    /** Keeps the "Key" header aligned with the key column once the selection boxes take the leading column. */
    private val headerLeadingSpacer = JPanel().apply { isOpaque = false }
    private lateinit var fetchingLabel: JLabel

    private lateinit var currentConnection: HacConnectionSettingsState
    @Volatile
    private var statePage: CxRemotePropertyStatePage? = null
    private var lastSeenLoadedCount: Int = -1
    private var lastSeenFilterSignature: String = ""

    /** Rows of the current report, before the client side filter is applied. Empty in [CxPropertyViewMode.ALL]. */
    private var reportRows: List<CxPropertyPresentation> = emptyList()

    /** The connection a full fetch was already asked of for the current report, so a failed or partial one is not retried forever. */
    @Volatile
    private var fullFetchRequestedFor: String? = null

    private val viewMode
        get() = CxPropertyViewSettings.getInstance(project).viewMode

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
                            if (!dPanel.validateAll().all { it.okEnabled }) return@button

                            setFetching(true)
                            CxRemotePropertyStateService.getInstance(project)
                                .upsertProperty(currentConnection, addKeyField.text.trim(), addValueField.text) { ok ->
                                    if (ok) {
                                        addKeyField.text = ""
                                        addValueField.text = ""
                                    }
                                }
                        }.applyToComponent {
                            toolTipText = "Stored in the runtime configuration of the node serving the request, until it restarts"
                        }
                    }.visibleIf(showDataPanel)
                        .layout(RowLayout.PARENT_GRID)

                    // --- Filters: labels above their inputs, view options on the right ---
                    row {
                        label("Filter by key:")
                        label("Filter by value:")
                        cell(buildViewOptionsToolbar())
                            .align(AlignX.RIGHT)
                    }.visibleIf(showDataPanel)
                        .layout(RowLayout.PARENT_GRID)

                    row {
                        keyFilterField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent {
                                emptyText.text = "Filter by key"
                                document.addDocumentListener(this@CxRemotePropertyStateView, documentListener { onFilterChanged() })
                            }
                            .component

                        valueFilterField = textField()
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .applyToComponent {
                                emptyText.text = "Filter by value"
                                document.addDocumentListener(this@CxRemotePropertyStateView, documentListener { onFilterChanged() })
                            }
                            .component
                    }.visibleIf(showDataPanel)
                        .layout(RowLayout.PARENT_GRID)

                    // --- Selection actions, only while a report is listed ---
                    row {
                        link("Select all") { propertyList.checkAll() }
                        link("Clear selection") { propertyList.clearChecked() }

                        button("Declare in Project…") { declareSelectedInProject() }
                            .enabledIf(hasSelection)
                            .align(AlignX.RIGHT)
                    }.visibleIf(showSelectionActions)
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

    init {
        project.messageBus.connect(this).subscribe(CxPropertyViewSettingsListener.TOPIC, object : CxPropertyViewSettingsListener {
            override fun onViewModeChanged(viewMode: CxPropertyViewMode) {
                val connection = currentConnection.takeIf { ::currentConnection.isInitialized } ?: return
                val page = statePage ?: return

                fullFetchRequestedFor = null
                viewScope.launch { applyViewMode(connection, page) }
            }
        })
    }

    override fun dispose() {
        viewScope.cancel()
        filterDebounceTimer.stop()
        propertyList.cancelEdit()
        lazyViewPanel.drop()
    }

    suspend fun render(
        coroutineScope: CoroutineScope,
        connection: HacConnectionSettingsState,
        statePage: CxRemotePropertyStatePage?,
    ): JComponent {
        val viewPanel = withContext(Dispatchers.EDT) { lazyViewPanel.value }
        val service = CxRemotePropertyStateService.getInstance(project)

        if (statePage == null) {
            currentConnection = connection
            this.statePage = null
            withContext(Dispatchers.EDT) {
                fetchingLabel.text = "Fetching data from '${connection.shortenConnectionName}'"
                setFetching(service.isFetching(connection))
                toggleView(if (service.isFetching(connection)) showFetchingState else showFetchProperties)
                propertyList.cancelEdit()
                listModel.removeAll()
                propertyList.localProperties = emptyMap()
                updateDifferingStatus(0)
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
            setFetching(service.isFetching(connection))
            toggleView(showDataPanel)
            bottomLoadingLabel.isVisible = service.isFetching(connection)
        }

        if (viewMode.report) {
            coroutineScope.launch { applyViewMode(connection, statePage) }
            return withContext(Dispatchers.EDT) { viewPanel }
        }

        var adoptedSnapshot = false

        withContext(Dispatchers.EDT) {
            reportRows = emptyList()
            propertyList.editable = true
            setSelectable(false)
            statusLabel.text = "Loaded ${statePage.loadedCount} of ${statePage.totalItems} total"

            // Only adopt the snapshot if it matches what the user is currently filtering for —
            // a stale broadcast (e.g. the first publish of a still-running filter fetch) would
            // otherwise repopulate the list with results for the previous filter.
            val filtersMatch = statePage.keyFilter == keyFilterField.text.trim()
                && statePage.valueFilter == valueFilterField.text.trim()
            if (filtersMatch) {
                syncListModel(connectionChanged, statePage)
                adoptedSnapshot = true
            }
        }

        // Resolving the project chain walks the indexes, so it must not hold up the rows: the
        // comparison lands in a follow-up repaint once the model is available.
        if (adoptedSnapshot) coroutineScope.launch { highlightDifferingProperties(statePage.properties) }

        return withContext(Dispatchers.EDT) { viewPanel }
    }

    /**
     * Builds the rows of a report mode by comparing the remote instance against the project's property chain.
     *
     * A report is only meaningful over the complete, unfiltered remote set — the page the user happened to scroll to
     * would report every unloaded property as missing — so an incomplete snapshot is re-fetched in full first and the
     * report is built by the broadcast that follows.
     */
    private suspend fun applyViewMode(connection: HacConnectionSettingsState, statePage: CxRemotePropertyStatePage) {
        val mode = viewMode

        if (!mode.report) {
            CxRemotePropertyStateService.getInstance(project).fetch(connection)
            return
        }

        val service = CxRemotePropertyStateService.getInstance(project)

        if (statePage.hasMore || statePage.keyFilter.isNotEmpty() || statePage.valueFilter.isNotEmpty()) {
            // The broadcast that starts a fetch still carries the old page - the report waits for the one that ends it.
            if (service.isFetching(connection)) return

            if (fullFetchRequestedFor == connection.uuid) {
                // The full fetch has already ended without everything in it; asking again would only loop.
                withContext(Dispatchers.EDT) {
                    setFetching(false)
                    statusLabel.text = "Unable to load every property of the instance"
                }
                return
            }

            fullFetchRequestedFor = connection.uuid
            withContext(Dispatchers.EDT) {
                keyFilterField.text = ""
                valueFilterField.text = ""
                setFetching(true)
            }
            // The script pages from offset 0, so an unbounded page is every property - totalItems may be a filtered count.
            service.resetAndFetch(server = connection, pageSize = Int.MAX_VALUE)
            return
        }

        fullFetchRequestedFor = null

        val chain = smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }
        val rows = when (mode) {
            CxPropertyViewMode.MISSING_IN_PROJECT -> CxPropertyComparison.missingInProject(statePage.properties, chain)
            CxPropertyViewMode.MISSING_ON_REMOTE -> CxPropertyComparison.missingOnRemote(statePage.properties, chain)
            CxPropertyViewMode.ALL -> statePage.properties
        }

        withContext(Dispatchers.EDT) {
            reportRows = rows
            propertyList.cancelEdit()
            // A property the remote instance does not have cannot be edited or deleted there.
            propertyList.editable = mode != CxPropertyViewMode.MISSING_ON_REMOTE
            // Only the properties the project is missing can be declared into it.
            setSelectable(mode == CxPropertyViewMode.MISSING_IN_PROJECT)
            propertyList.retainCheckedWithin(rows.map { it.key })
            lastSeenLoadedCount = -1
            lastSeenFilterSignature = ""
            applyClientFilter()
            statusLabel.text = reportStatus(mode, rows.size, statePage.totalItems)
        }

        highlightDifferingProperties(rows, chain)
    }

    private fun setSelectable(selectable: Boolean) {
        propertyList.selectable = selectable
        showSelectionActions.set(selectable)

        headerLeadingSpacer.preferredSize = Dimension(
            if (selectable) JBUI.scale(CxPropertyRenderer.CHECKBOX_HIT_WIDTH) else 0,
            0,
        )
        headerLeadingSpacer.revalidate()
        headerLeadingSpacer.parent?.revalidate()
    }

    private fun reportStatus(mode: CxPropertyViewMode, rows: Int, remoteTotal: Int) = when (mode) {
        CxPropertyViewMode.MISSING_IN_PROJECT -> "$rows of $remoteTotal remote properties are not declared by the project"
        CxPropertyViewMode.MISSING_ON_REMOTE -> "$rows project properties are missing on the remote instance"
        CxPropertyViewMode.ALL -> "Loaded $rows of $remoteTotal total"
    }

    /** Narrows the current report by the filter fields. Report rows are already complete, so no round-trip is needed. */
    private fun applyClientFilter() {
        val keyFilter = keyFilterField.text.trim()
        val valueFilter = valueFilterField.text.trim()
        val filtered = reportRows.filter { property ->
            (keyFilter.isEmpty() || property.key.contains(keyFilter, ignoreCase = true))
                && (valueFilter.isEmpty() || property.value.contains(valueFilter, ignoreCase = true))
        }

        listModel.replaceAll(filtered)
    }

    /**
     * Compares every loaded remote property against the value the project's own property files resolve to and hands
     * the result to the list, which repaints the disagreeing rows.
     */
    private suspend fun highlightDifferingProperties(
        properties: List<CxPropertyPresentation>,
        resolvedChain: CxPropertyModel? = null,
    ) {
        val chain = resolvedChain ?: smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }
        val localProperties = chain.resolveProperties(properties.map { it.key })
        val differingCount = properties.count { localProperties[it.key]?.value?.equals(it.value) == false }

        withContext(Dispatchers.EDT) {
            propertyList.localProperties = localProperties
            updateDifferingStatus(differingCount)
        }
    }

    private fun updateDifferingStatus(differingCount: Int) {
        if (!::differingLabel.isInitialized) return

        differingLabel.text = when (differingCount) {
            0 -> ""
            1 -> "1 value differs from the project files"
            else -> "$differingCount values differ from the project files"
        }
        differingLabel.isVisible = differingCount > 0
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
        if (viewMode.report) return

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
        if (viewMode.report) applyClientFilter()
        else filterDebounceTimer.restart()
    }

    private fun fetchFilteredPage() {
        if (viewMode.report) return

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

    /**
     * Opens the report for a property. The project chain is resolved off the EDT, since the first resolution walks
     * the indexes; the dialog only opens once the answer is in.
     */
    private fun showReport(property: CxPropertyPresentation) {
        viewScope.launch {
            val chain = smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }
            val declared = chain[property.key]
            val resolvedValue = chain.resolve(property.key)

            withContext(Dispatchers.EDT) {
                CxPropertyReportDialog(project, property, declared, resolvedValue).show()
            }
        }
    }

    /**
     * Declares the checked properties in one of the project's own property files. The targets are read off the
     * collected chain, so the dialog only offers files the running system would actually read.
     */
    private fun declareSelectedInProject() {
        val properties = propertyList.checkedProperties.takeIf { it.isNotEmpty() } ?: return

        viewScope.launch {
            val writeService = CxPropertyWriteService.getInstance(project)
            val targets = smartReadAction(project) { writeService.targets() }

            if (targets.isEmpty()) {
                Notifications.create(
                    NotificationType.WARNING,
                    "Nowhere to declare the properties",
                    "The project has neither a local.properties nor a custom extension with a project.properties.",
                ).notify(project)
                return@launch
            }

            val dialog = withContext(Dispatchers.EDT) {
                CxDeclarePropertiesDialog(project, targets, properties).takeIf { it.showAndGet() }
            } ?: return@launch

            val target = dialog.target
            // A committed file is no place for a secret: its key is declared there, its value is left to local.properties.
            val withheld = if (target.scope == CxPropertyScope.LOCAL) emptySet()
            else properties.filter { CxSecretKind.of(it.key) != null }.mapTo(HashSet()) { it.key }
            val toWrite = properties.map { if (it.key in withheld) it.copy(value = "") else it }

            val written = writeService.write(target, toWrite)
            if (written.isEmpty()) {
                Notifications.create(
                    NotificationType.WARNING,
                    "Nothing declared",
                    "<p>${StringUtil.escapeXmlEntities(target.presentableName)} is not a properties file.</p>",
                ).notify(project)
                return@launch
            }

            val declared = written.mapTo(HashSet(written.size)) { it.key }

            withContext(Dispatchers.EDT) {
                propertyList.clearChecked()
                // They are no longer missing from the project, so they leave the report at once rather than waiting
                // for the chain to be read again - the file has only just been written and may not have been saved yet.
                dropFromReport { it.key in declared }
            }

            Notifications.create(
                NotificationType.INFORMATION,
                "Properties declared",
                "<p>Declared ${written.size} propert${if (written.size == 1) "y" else "ies"} in " +
                    "<a href=\"$OPEN_TARGET\">${StringUtil.escapeXmlEntities(target.presentableName)}</a></p>" +
                    if (withheld.isEmpty()) ""
                    else "<p>${withheld.size} secret${if (withheld.size == 1) " was" else "s were"} declared without a value</p>",
            )
                // The file is rarely the one on screen, so the one named in the message opens it.
                .onClick { PsiNavigationSupport.getInstance().createNavigatable(project, target.file, 0).navigate(true) }
                .notify(project)

        }
    }

    /** Takes rows out of the current report, keeping the list and its count in step. */
    private fun dropFromReport(gone: (CxPropertyPresentation) -> Boolean) {
        val remaining = reportRows.filterNot(gone)
        if (remaining.size == reportRows.size) return

        reportRows = remaining
        propertyList.retainCheckedWithin(remaining.map { it.key })
        applyClientFilter()
        statusLabel.text = reportStatus(viewMode, remaining.size, statePage?.totalItems ?: remaining.size)
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
        val confirmed = MessageDialogBuilder
            .yesNo("Delete Property", "Delete property '${property.key}' from '${currentConnection.shortenConnectionName}'?")
            .ask(project)
        if (!confirmed) return

        propertyList.cancelEdit()
        setFetching(true)
        CxRemotePropertyStateService.getInstance(project).deleteProperty(currentConnection, property.key)
    }

    private fun validatePropertyKey(value: String) = CxPropertyKey.validate(value)?.let { ValidationInfo(it) }

    private fun setFetching(fetching: Boolean) {
        showFetchingState.set(fetching)
        if (!fetching && ::bottomLoadingLabel.isInitialized) bottomLoadingLabel.isVisible = false
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showFetchProperties, showDataPanel, showFetchingState)
        .forEach { it.set(unhide.contains(it)) }

    /**
     * View options button — one toolbar button opening the mode popup, the way the Commit tool window presents its own
     * grouping options. Wrapping the popup group in a plain group is what renders it as a single button.
     */
    private fun buildViewOptionsToolbar(): JComponent {
        val group = ActionManager.getInstance()
            .getAction("sap.cx.properties.remote.viewOptions")
            .asSafely<ActionGroup>()
            ?: return JPanel()

        return ActionManager.getInstance()
            .createActionToolbar(HybrisActionPlaces.PROPERTIES_VIEW_OPTIONS, DefaultActionGroup(group), true)
            .also { it.targetComponent = propertyList }
            .component
    }

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

        header.add(headerLeadingSpacer, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.0
            fill = GridBagConstraints.NONE
        })
        header.add(keyHeader, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, 0, 0, gap / 2)
        })
        header.add(valueHeader, GridBagConstraints().apply {
            gridx = 2; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, gap / 2, 0, JBUI.scale(HEADER_ACTION_RESERVED_WIDTH))
        })
        header.add(Box.createHorizontalStrut(JBUI.scale(HEADER_ACTION_RESERVED_WIDTH)),
            GridBagConstraints().apply {
                gridx = 3; gridy = 0
                weightx = 0.0
                fill = GridBagConstraints.NONE
            })
        return header
    }

    /**
     * Bottom toolbar — sits below the scrolling area and shows summary status. The "Loading…"
     * label only appears while a fetch is in flight, the middle label legends the highlighted
     * rows, and the right-aligned label shows the "Loaded N of M total" counter.
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
        differingLabel = JLabel("").apply {
            foreground = CxPropertyRenderer.VALUE_DIFFERS_COLOR
            isVisible = false
        }

        toolbar.add(bottomLoadingLabel, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.0
            anchor = GridBagConstraints.WEST
        })
        toolbar.add(differingLabel, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 1.0
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(0, JBUI.scale(COLUMN_GAP), 0, 0)
        })
        toolbar.add(statusLabel, GridBagConstraints().apply {
            gridx = 2; gridy = 0
            weightx = 0.0
            anchor = GridBagConstraints.EAST
        })
        return toolbar
    }

    companion object {
        private const val OPEN_TARGET = "open-target"
        private const val FILTER_DEBOUNCE_MS = 500
        private const val SCROLL_TRIGGER_THRESHOLD = 96
        private const val SCROLL_UNIT_INCREMENT = 16
        private const val COLUMN_GAP = 8
        private const val HEADER_VERTICAL_PADDING = 6
        private const val HEADER_HORIZONTAL_PADDING = 12
        private val HEADER_ACTION_RESERVED_WIDTH = CxPropertyRowAction.totalHitWidth + 12
        private const val TOOLBAR_VERTICAL_PADDING = 6
        private const val TOOLBAR_HORIZONTAL_PADDING = 12
    }
}
