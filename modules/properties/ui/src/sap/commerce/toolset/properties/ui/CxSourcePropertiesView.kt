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
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.CxRemotePropertyStateService
import sap.commerce.toolset.properties.exec.CxRemotePropertyStatePage
import sap.commerce.toolset.properties.exec.event.CxRemotePropertyStateListener
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyScope
import sap.commerce.toolset.properties.meta.CxPropertySource
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addDocumentListener
import sap.commerce.toolset.ui.event.documentListener
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Lists the properties the project's own files declare — either the chain as a whole, or the declarations of one
 * extension on its own.
 *
 * Rows behave as they do for a remote instance: the report, the context menu and the filters all work the same. What
 * changes is the side they are held against. The project as a whole is compared to the remote instance currently
 * fetched, if there is one; a single extension is compared to the value the whole chain ends up resolving to, so a
 * declaration another file overrides stands out.
 */
internal class CxSourcePropertiesView(private val project: Project) : Disposable {

    private val showData = AtomicBooleanProperty(false)

    private val job = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.Default + job)

    private val listModel = CollectionListModel<CxPropertyPresentation>()
    private val propertyList = CxPropertyList(
        parentDisposable = this,
        model = listModel,
        onReportClicked = { showReport(it) },
        onEditClicked = { },
        onDeleteClicked = { },
    ).apply {
        // Nothing here lives on a remote instance, so there is nothing to edit or delete.
        editable = false
    }

    private lateinit var keyFilterField: JBTextField
    private lateinit var valueFilterField: JBTextField
    private lateinit var statusLabel: JLabel
    private lateinit var titleLabel: JLabel

    private var rows: List<CxPropertyPresentation> = emptyList()
    private var currentSelection: CxSourceSelection? = null

    init {
        // The instance is compared against whatever has been fetched of it, so fetching more has to redraw this view.
        project.messageBus.connect(this).subscribe(CxRemotePropertyStateListener.TOPIC, object : CxRemotePropertyStateListener {
            override fun onPropertiesStateChanged(remoteConnection: HacConnectionSettingsState) {
                val selection = currentSelection ?: return

                viewScope.launch { render(selection) }
            }
        })
    }

    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute(): DialogPanel = panel {
                row {
                    titleLabel = label("").component
                    cell(titleLabel).align(AlignX.FILL)
                }.visibleIf(showData)

                row {
                    label("Filter by key:")
                    label("Filter by value:")
                }.visibleIf(showData)
                    .layout(RowLayout.PARENT_GRID)

                row {
                    keyFilterField = textField()
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .applyToComponent {
                            emptyText.text = "Filter by key"
                            document.addDocumentListener(this@CxSourcePropertiesView, documentListener { applyFilter() })
                        }
                        .component

                    valueFilterField = textField()
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .applyToComponent {
                            emptyText.text = "Filter by value"
                            document.addDocumentListener(this@CxSourcePropertiesView, documentListener { applyFilter() })
                        }
                        .component
                }.visibleIf(showData)
                    .layout(RowLayout.PARENT_GRID)

                separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                    .visibleIf(showData)

                row {
                    cell(
                        JBScrollPane(propertyList).apply {
                            border = null
                            background = propertyList.background
                            viewport.background = propertyList.background
                            setColumnHeaderView(buildColumnHeader(propertyList.background))
                        }
                    ).align(Align.FILL).visibleIf(showData)
                }.resizableRow()

                row {
                    statusLabel = label("").component
                    cell(statusLabel).align(AlignX.FILL)
                }.visibleIf(showData)
            }.apply {
                border = JBUI.Borders.empty(JBUI.insets(10, 16, 8, 16))
            }
        }
    }

    override fun dispose() {
        job.cancel()
        lazyViewPanel.drop()
    }

    suspend fun render(selection: CxSourceSelection): JComponent {
        val viewPanel = lazyViewPanel.value

        val chain = smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }
        val source = (selection as? CxSourceSelection.Extension)?.let { sourceOf(it.name, chain) }
        val properties = when (selection) {
            is CxSourceSelection.Project -> chain.resolveAll()
                .map { (key, value) -> CxPropertyPresentation(key, value) }

            is CxSourceSelection.Extension -> source
                ?.let { chain.declarationsIn(it) }
                ?.map { CxPropertyPresentation(it.key, it.value) }
                ?: emptyList()
        }
        val counterpart = counterpartFor(selection, chain, properties)
        val remote = remoteSnapshot()

        withContext(Dispatchers.EDT) {
            currentSelection = selection
            rows = properties
            titleLabel.text = titleFor(selection, chain, source)
            // Only a file the running system never reads is worth flagging - the rest speak for themselves.
            titleLabel.icon = HybrisIcons.Property.IGNORED.takeIf { source != null && !source.active }
            propertyList.emptyText.text = emptyTextFor(selection, source)
            propertyList.counterpart = counterpart
            applyFilter()
            statusLabel.text = statusFor(selection, properties.size, counterpart.countDisagreements(properties), remote)
            showData.set(true)
        }

        return withContext(Dispatchers.EDT) { viewPanel }
    }

    /** The `project.properties` of one extension, or `null` when the chain holds none for it. */
    private fun sourceOf(extension: String, chain: CxPropertyModel) = chain.sources
        .find { it.scope == CxPropertyScope.PROJECT && it.extension == extension }

    private fun counterpartFor(
        selection: CxSourceSelection,
        chain: CxPropertyModel,
        properties: List<CxPropertyPresentation>,
    ) = when (selection) {
        is CxSourceSelection.Project -> CxPropertyCounterpart.remote(fetchedRemoteValues())
        is CxSourceSelection.Extension -> CxPropertyCounterpart.effective(
            chain.resolveProperties(properties.map { it.key })
        )
    }

    /** What has been fetched of the active instance so far; no request is made just to draw this list. */
    private fun remoteSnapshot() = HacExecConnectionService.getInstance(project)
        .activeConnection
        .let { CxRemotePropertyStateService.getInstance(project).state(it.uuid).get() }

    private fun fetchedRemoteValues(): Map<String, String> = remoteSnapshot()
        ?.properties
        ?.associate { it.key to it.value }
        ?: emptyMap()

    private fun titleFor(selection: CxSourceSelection, chain: CxPropertyModel, source: CxPropertySource?) = when {
        selection is CxSourceSelection.Project -> "Properties declared by ${chain.sources.size} file(s) of this project"
        source == null -> "${(selection as CxSourceSelection.Extension).name} has no project.properties"
        !source.active -> "${source.presentableName} — not listed in localextensions.xml, so the running system never reads it"
        else -> "Properties declared by ${source.presentableName}"
    }

    private fun emptyTextFor(selection: CxSourceSelection, source: CxPropertySource?) = when {
        selection is CxSourceSelection.Project -> "This project declares no properties"
        source == null -> "${(selection as CxSourceSelection.Extension).name} has no project.properties file"
        else -> "${source.presentableName} declares no properties"
    }

    /**
     * Says what was compared as well as what differs. A project is held against however much of the instance has been
     * fetched, and saying so is the difference between "nothing differs" and "almost nothing was looked at".
     */
    private fun statusFor(
        selection: CxSourceSelection,
        total: Int,
        disagreements: Int,
        remote: CxRemotePropertyStatePage?,
    ): String {
        val prefix = "$total propert${if (total == 1) "y" else "ies"}"

        if (selection !is CxSourceSelection.Project) {
            return if (disagreements == 0) prefix else "$prefix | $disagreements overridden further down the chain"
        }

        if (remote == null) return "$prefix | fetch a remote instance to compare these against it"

        return "$prefix | $disagreements differ from ${comparedAgainst(remote)}"
    }

    /** Names how much of the instance the comparison actually covers, filters included. */
    private fun comparedAgainst(remote: CxRemotePropertyStatePage): String {
        val filtered = remote.keyFilter.isNotBlank() || remote.valueFilter.isNotBlank()

        return when {
            remote.hasMore -> "the ${remote.loadedCount} of ${remote.totalItems} remote properties fetched so far"
            filtered -> "the ${remote.loadedCount} remote properties matching the fetched filter"
            else -> "the remote instance"
        }
    }

    private fun applyFilter() {
        val keyFilter = keyFilterField.text.trim()
        val valueFilter = valueFilterField.text.trim()

        listModel.replaceAll(
            rows.filter { property ->
                (keyFilter.isEmpty() || property.key.contains(keyFilter, ignoreCase = true))
                    && (valueFilter.isEmpty() || property.value.contains(valueFilter, ignoreCase = true))
            }
        )
    }

    private fun showReport(property: CxPropertyPresentation) {
        viewScope.launch {
            val chain = smartReadAction(project) { CxPropertyCollector.getInstance(project).collect() }

            withContext(Dispatchers.EDT) {
                CxPropertyReportDialog(project, property, chain[property.key], chain.resolve(property.key)).show()
            }
        }
    }

    /** Mirrors [CxPropertyRenderer]'s layout so the headings line up with the columns underneath. */
    private fun buildColumnHeader(bg: Color): JComponent {
        val gap = JBUI.scale(COLUMN_GAP)
        val header = JPanel(GridBagLayout()).apply {
            isOpaque = true
            background = bg
            border = JBUI.Borders.empty(HEADER_VERTICAL_PADDING, HEADER_HORIZONTAL_PADDING)
        }

        header.add(JLabel("Key").apply { font = font.deriveFont(Font.BOLD) }, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, 0, 0, gap / 2)
        })
        header.add(JLabel("Value").apply { font = font.deriveFont(Font.BOLD) }, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(0, gap / 2, 0, JBUI.scale(CxPropertyRowAction.REPORT.hitWidth))
        })
        header.add(Box.createHorizontalStrut(JBUI.scale(CxPropertyRowAction.REPORT.hitWidth)), GridBagConstraints().apply {
            gridx = 2; gridy = 0
            weightx = 0.0
            fill = GridBagConstraints.NONE
        })

        return header
    }

    companion object {
        private const val COLUMN_GAP = 8
        private const val HEADER_VERTICAL_PADDING = 6
        private const val HEADER_HORIZONTAL_PADDING = 12
    }
}
