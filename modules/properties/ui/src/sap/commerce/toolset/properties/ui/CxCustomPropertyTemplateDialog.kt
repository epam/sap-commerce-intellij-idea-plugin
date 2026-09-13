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

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyState
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addDocumentListener
import sap.commerce.toolset.ui.event.documentListener
import javax.swing.JComponent

/**
 * Names a property template and, when there is something to pick from, chooses what goes into it.
 *
 * A template built from a remote instance would otherwise take every property that instance has — thousands of them,
 * of which a handful are usually what was meant. The picker is the same virtualized list the tool window uses, so the
 * size of the source costs nothing to display.
 */
class CxCustomPropertyTemplateDialog(
    private val context: PropertyTemplateDialogContext,
) : DialogWrapper(context.project) {

    private val selecting = context.selectableProperties.isNotEmpty()
    private val hasSelection = AtomicBooleanProperty(selecting)

    private lateinit var nameTextField: JBTextField
    private lateinit var filterField: JBTextField

    private val listModel = CollectionListModel(context.selectableProperties)
    private val propertyList = CxPropertyList(
        parentDisposable = disposable,
        model = listModel,
        onReportClicked = { },
        onEditClicked = { },
        onDeleteClicked = { },
    ).apply {
        // Nothing here is acted upon - the rows are only there to be picked.
        availableActions = emptySet()
        selectable = true
        onSelectionChanged = { hasSelection.set(it.isNotEmpty()) }
        checkAll()
    }

    /** Properties the user kept, in the order the source listed them. */
    val selectedProperties: List<CxPropertyPresentation>
        get() = if (selecting) context.selectableProperties.filter { propertyList.isChecked(it.key) }
        else context.selectableProperties

    init {
        title = context.title
        isResizable = selecting
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            nameTextField = textField()
                .label("Name:")
                .bindText(context.mutable.name)
                .align(AlignX.FILL)
                .validationOnInput { validateName(it.text) }
                .validationOnApply { validateName(it.text) }
                .component
        }.layout(RowLayout.PARENT_GRID)

        if (context.showRemoveSourceTemplates) {
            row {
                checkBox("Remove source template")
                    .bindSelected(context.removeSourceTemplates)
            }.layout(RowLayout.PARENT_GRID)
        }

        if (selecting) {
            row {
                filterField = textField()
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .applyToComponent {
                        emptyText.text = "Filter by key or value"
                        document.addDocumentListener(disposable, documentListener { applyFilter() })
                    }
                    .component

                link("Select all") { propertyList.checkAll() }
                link("Clear") { propertyList.clearChecked() }
            }.layout(RowLayout.PARENT_GRID)

            row {
                cell(JBScrollPane(propertyList))
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16)
        if (selecting) preferredSize = JBUI.size(DIALOG_WIDTH, DIALOG_HEIGHT)
    }

    // The name is validated by the panel's own validators; only the selection needs saying here.
    override fun doValidate(): ValidationInfo? = ValidationInfo("Select at least one property", propertyList)
        .takeIf { selecting && !hasSelection.get() }

    override fun getPreferredFocusedComponent(): JComponent = nameTextField

    /**
     * Narrows what is listed without touching what is checked — a filter is a way of finding properties to pick, not a
     * way of unpicking the ones scrolled out of sight.
     */
    private fun applyFilter() {
        val needle = filterField.text.trim()

        listModel.replaceAll(
            if (needle.isEmpty()) context.selectableProperties
            else context.selectableProperties.filter {
                it.key.contains(needle, ignoreCase = true) || it.value.contains(needle, ignoreCase = true)
            }
        )
    }

    private fun validateName(value: String) = when {
        value.isBlank() -> ValidationInfo("Template name cannot be blank", nameTextField)
        value.length > MAX_NAME_LENGTH -> ValidationInfo("Template name cannot exceed $MAX_NAME_LENGTH characters", nameTextField)
        else -> null
    }

    /** Replaces the template's properties with the ones the user kept. */
    fun applySelection() {
        if (!selecting) return

        context.mutable.properties.set(
            selectedProperties.map { CxCustomPropertyState(it.key, it.value).mutable() }
        )
    }

    companion object {
        private const val MAX_NAME_LENGTH = 255
        private const val DIALOG_WIDTH = 640
        private const val DIALOG_HEIGHT = 520
    }
}
