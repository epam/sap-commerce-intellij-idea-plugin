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

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.properties.meta.CxPropertyTarget
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Asks which of the project's own property files the chosen properties should be declared in.
 *
 * Only `local.properties` and the `project.properties` of custom extensions are offered — writing into a platform or
 * out-of-the-box file would be lost on the next update.
 */
class CxDeclarePropertiesDialog(
    project: Project,
    private val targets: List<CxPropertyTarget>,
    private val properties: List<CxPropertyPresentation>,
) : DialogWrapper(project) {

    private val selectedTarget = AtomicProperty(targets.first())

    val target: CxPropertyTarget
        get() = selectedTarget.get()

    init {
        title = if (properties.size == 1) "Declare Property in Project" else "Declare ${properties.size} Properties in Project"
        setOKButtonText("Declare")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Declare in:") {
            comboBox(targets, targetRenderer())
                .bindItem(selectedTarget)
                .align(AlignX.FILL)
                .resizableColumn()
        }.layout(RowLayout.PARENT_GRID)

        row {
            comment("An existing declaration of the same property in the chosen file is replaced.")
        }

        group("Properties") {
            row {
                cell(JBScrollPane(propertiesPreview()))
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16)
        preferredSize = JBUI.size(DIALOG_WIDTH, DIALOG_HEIGHT)
    }

    /**
     * Two files may share a name - a project can hold more than one `local.properties` - so the path is spelled out
     * beside it, greyed, the way the platform disambiguates anything else chosen by name.
     */
    private fun targetRenderer() = object : ColoredListCellRenderer<CxPropertyTarget>() {
        override fun customizeCellRenderer(
            list: JList<out CxPropertyTarget>,
            value: CxPropertyTarget?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            val target = value ?: return

            append(target.presentableName)
            append("  ${target.path}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }

    /**
     * A table rather than a row per property: keys and values line up in their own columns as they do in the tool
     * window this selection came from, and a table builds no Swing component per row - a selection running to
     * thousands of properties would otherwise freeze the dialog before it ever appeared.
     */
    private fun propertiesPreview() = JBTable(PropertiesTableModel(properties)).apply {
        setShowGrid(false)
        rowSelectionAllowed = false
        tableHeader.reorderingAllowed = false
        columnModel.getColumn(VALUE_COLUMN).cellRenderer = DefaultTableCellRenderer()
            .apply { foreground = JBColor.GRAY }
    }

    private class PropertiesTableModel(private val properties: List<CxPropertyPresentation>) : AbstractTableModel() {

        override fun getRowCount() = properties.size

        override fun getColumnCount() = 2

        override fun getColumnName(column: Int) = if (column == VALUE_COLUMN) "Value" else "Key"

        override fun getValueAt(row: Int, column: Int) = properties[row]
            .let { if (column == VALUE_COLUMN) it.value.ifBlank { EMPTY_VALUE } else it.key }
    }

    companion object {
        private const val VALUE_COLUMN = 1
        private const val DIALOG_WIDTH = 640
        private const val DIALOG_HEIGHT = 420
        private const val EMPTY_VALUE = "<empty>"
    }
}
