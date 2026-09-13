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
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.exec.CxRemotePropertyClient
import sap.commerce.toolset.properties.meta.CxPropertyComparison
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addDocumentListener
import sap.commerce.toolset.ui.event.documentListener
import javax.swing.Action
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * Reports what two SAP Commerce instances disagree about.
 *
 * Comparing an instance with the project answers "is this deployed?"; comparing two instances answers "why does this
 * one behave differently?", which is a question the tool window could not ask at all. Both instances are read in full
 * before anything is shown - a comparison over the pages somebody happened to scroll to would report most of an
 * instance as missing.
 */
internal class CxCompareInstancesDialog(
    private val project: Project,
    private val first: HacConnectionSettingsState,
    private val candidates: List<HacConnectionSettingsState>,
) : DialogWrapper(project), Disposable {

    private val job = SupervisorJob()
    private val dialogScope = CoroutineScope(Dispatchers.Default + job)

    private val showData = AtomicBooleanProperty(false)

    private var second: HacConnectionSettingsState? = candidates.firstOrNull()
    private var section = CxInstanceComparisonSection.DIFFERING
    private var comparison: CxInstanceComparison? = null

    private val listModel = CollectionListModel<CxPropertyPresentation>()
    private val propertyList = CxPropertyList(
        parentDisposable = disposable,
        model = listModel,
        onReportClicked = { },
        onEditClicked = { },
        onDeleteClicked = { },
    ).apply {
        // Neither instance is being edited here; the comparison is something to read.
        availableActions = emptySet()
    }

    private lateinit var sectionLabel: JLabel
    private lateinit var statusLabel: JLabel
    private lateinit var filterField: JBTextField

    init {
        title = "Compare Properties of Two Instances"
        init()
        reload()
    }

    override fun dispose() {
        job.cancel()
        super.dispose()
    }

    /** Read-only, so there is nothing to confirm - only a way out. */
    override fun createActions(): Array<Action> = arrayOf(cancelAction.apply { putValue(Action.NAME, "Close") })

    override fun createCenterPanel(): JComponent = panel {
        row("Compare:") {
            label(first.connectionName)
            label("with")
            comboBox(candidates)
                .align(AlignX.FILL)
                .resizableColumn()
                .applyToComponent {
                    renderer = ConnectionRenderer()
                    selectedItem = second
                    addActionListener {
                        second = selectedItem as? HacConnectionSettingsState
                        reload()
                    }
                }
        }

        row("Show:") {
            comboBox(CxInstanceComparisonSection.entries.toList())
                .align(AlignX.FILL)
                .resizableColumn()
                .applyToComponent {
                    renderer = SectionRenderer { comparison }
                    selectedItem = section
                    addActionListener {
                        section = selectedItem as? CxInstanceComparisonSection ?: section
                        render()
                    }
                }
        }

        row("Filter:") {
            filterField = textField()
                .align(AlignX.FILL)
                .resizableColumn()
                .applyToComponent {
                    emptyText.text = "Filter by key or value"
                    document.addDocumentListener(disposable, documentListener { render() })
                }
                .component
        }

        row {
            sectionLabel = label("").component
            cell(sectionLabel).align(AlignX.FILL)
        }.visibleIf(showData)

        row {
            cell(
                JBScrollPane(propertyList).apply {
                    border = null
                    background = propertyList.background
                    viewport.background = propertyList.background
                    setColumnHeaderView(propertyColumnHeader(propertyList.background, 0))
                }
            ).align(Align.FILL).resizableColumn()
        }.resizableRow()

        row {
            statusLabel = label("Reading both instances...").component
            cell(statusLabel).align(AlignX.FILL)
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16)
        preferredSize = JBUI.size(DIALOG_WIDTH, DIALOG_HEIGHT)
    }

    /** Reads both instances in full and works out what they disagree about. */
    private fun reload() {
        val other = second ?: return

        comparison = null
        showData.set(false)
        listModel.replaceAll(emptyList())
        statusLabel.text = "Reading ${first.connectionName} and ${other.connectionName}..."

        dialogScope.launch {
            val client = CxRemotePropertyClient.getInstance(project)
            val loaded = runCatching {
                val firstPage = async { client.fetchAll(first) }
                val secondPage = async { client.fetchAll(other) }

                (firstPage.await()?.properties ?: error("${first.connectionName} returned no properties.")) to
                    (secondPage.await()?.properties ?: error("${other.connectionName} returned no properties."))
            }

            val (firstProperties, secondProperties) = loaded.getOrElse {
                withContext(Dispatchers.EDT) { statusLabel.text = it.message ?: "Unable to read both instances." }
                return@launch
            }

            val computed = CxInstanceComparison(
                first = first.connectionName,
                second = other.connectionName,
                firstTotal = firstProperties.size,
                secondTotal = secondProperties.size,
                onlyInFirst = CxPropertyComparison.onlyIn(firstProperties, secondProperties),
                onlyInSecond = CxPropertyComparison.onlyIn(secondProperties, firstProperties),
                differing = CxPropertyComparison.differing(firstProperties, secondProperties),
                secondValues = secondProperties.associate { it.key to it.value },
            )

            withContext(Dispatchers.EDT) {
                comparison = computed
                showData.set(true)
                render()
            }
        }
    }

    /** Draws the chosen section of an already computed comparison, narrowed by the filter. */
    private fun render() {
        val computed = comparison ?: return
        val filter = filterField.text.trim()
        val rows = computed.rowsOf(section)
            .filter { filter.isEmpty() || it.key.contains(filter, true) || it.value.contains(filter, true) }

        propertyList.counterpart = computed.counterpartOf(section)
        listModel.replaceAll(rows)
        propertyList.emptyText.text = emptyTextFor(computed)
        sectionLabel.text = section.titleFor(computed.first, computed.second)
        statusLabel.text = statusFor(computed, rows.size, filter.isNotEmpty())
    }

    private fun emptyTextFor(computed: CxInstanceComparison) = when (section) {
        CxInstanceComparisonSection.DIFFERING -> "The two instances agree on every property they share"
        CxInstanceComparisonSection.ONLY_IN_FIRST -> "${computed.second} has everything ${computed.first} has"
        CxInstanceComparisonSection.ONLY_IN_SECOND -> "${computed.first} has everything ${computed.second} has"
    }

    private fun statusFor(computed: CxInstanceComparison, shown: Int, filtered: Boolean): String {
        val total = computed.rowsOf(section).size
        val counts = "${computed.first}: ${computed.firstTotal} properties | ${computed.second}: ${computed.secondTotal}"

        return if (filtered) "$shown of $total shown | $counts" else "$total | $counts"
    }

    private class ConnectionRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            selected: Boolean,
            focused: Boolean,
        ) = super.getListCellRendererComponent(list, value, index, selected, focused).also {
            text = (value as? HacConnectionSettingsState)?.connectionName.orEmpty()
        }
    }

    private class SectionRenderer(private val comparison: () -> CxInstanceComparison?) : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            selected: Boolean,
            focused: Boolean,
        ) = super.getListCellRendererComponent(list, value, index, selected, focused).also {
            val section = value as? CxInstanceComparisonSection ?: return@also
            val computed = comparison()

            text = computed
                ?.let { "${section.titleFor(it.first, it.second)} (${it.rowsOf(section).size})" }
                ?: section.title
        }
    }

    companion object {
        private const val DIALOG_WIDTH = 900
        private const val DIALOG_HEIGHT = 600
    }
}
