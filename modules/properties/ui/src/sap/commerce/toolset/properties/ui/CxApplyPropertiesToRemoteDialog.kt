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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import javax.swing.JComponent

/**
 * Confirms writing the chosen project properties into the configuration store of a remote instance.
 *
 * The values shown are the ones the project's chain resolves to, placeholders already expanded, because that is what
 * the remote instance will be given.
 */
class CxApplyPropertiesToRemoteDialog(
    project: Project,
    private val connectionName: String,
    private val properties: List<CxPropertyPresentation>,
) : DialogWrapper(project) {

    init {
        title = if (properties.size == 1) "Apply Property to Remote" else "Apply ${properties.size} Properties to Remote"
        setOKButtonText("Apply")
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            label("These properties will be stored on '$connectionName'.")
        }

        row {
            comment("A property which the instance already has is overwritten. The change is not persisted to any file.")
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

    private fun propertiesPreview() = panel {
        properties.forEach { property ->
            row(property.key) {
                label(property.value.ifBlank { EMPTY_VALUE })
            }.layout(RowLayout.PARENT_GRID)
        }
    }.apply {
        border = JBUI.Borders.empty(4)
    }

    companion object {
        private const val DIALOG_WIDTH = 640
        private const val DIALOG_HEIGHT = 420
        private const val EMPTY_VALUE = "<empty>"
    }
}
