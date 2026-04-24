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

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

class CxCustomPropertyTemplateDialog(
    private val context: PropertyTemplateDialogContext,
) : DialogWrapper(context.project) {

    private lateinit var nameTextField: JBTextField

    init {
        title = context.title
        isResizable = false
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            nameTextField = textField()
                .label("Name:")
                .bindText(context.mutable.name)
                .align(AlignX.FILL)
                .validationOnInput {
                    when {
                        it.text.isBlank() -> error("Template name cannot be blank")
                        it.text.length > 255 -> error("Template name cannot exceed 255 characters")
                        else -> null
                    }
                }
                .component
        }.layout(RowLayout.PARENT_GRID)

        if (context.showRemoveSourceTemplates) {
            row {
                checkBox("Remove source template")
                    .bindSelected(context.removeSourceTemplates)
            }.layout(RowLayout.PARENT_GRID)
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16)
    }

    override fun getPreferredFocusedComponent(): JComponent = nameTextField
}
