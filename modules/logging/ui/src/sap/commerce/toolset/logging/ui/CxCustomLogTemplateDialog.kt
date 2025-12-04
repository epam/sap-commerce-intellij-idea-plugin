/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.logging.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.logging.custom.settings.state.CxCustomLogTemplateState
import javax.swing.JComponent

class CxCustomLogTemplateDialog(
    project: Project,
    private val mutable: CxCustomLogTemplateState.Mutable,
    private val title: String
) : DialogWrapper(project) {

    private lateinit var nameTextField: JBTextField

    init {
        super.title = title
        isResizable = false
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            nameTextField = textField()
                .label("Name:")
                .bindText(mutable.name)
                .align(AlignX.FILL)
                .addValidationRule("Template Name cannot be blank") { it.text.isNullOrBlank() }
                .addValidationRule("Template Name cannot exceed 255 characters") { it.text.length > 255 }
                .component

        }.layout(RowLayout.PARENT_GRID)
    }

    override fun getPreferredFocusedComponent(): JComponent = nameTextField

}