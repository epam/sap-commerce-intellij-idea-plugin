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

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.InlineBanner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import sap.commerce.toolset.logging.CxLogLevel
import javax.swing.JComponent

open class CxCustomLogTemplateDialog(
//    project: Project,
//    private val mutable: CxCustomLogTemplateState.Mutable,
//    private val title: String
    protected val context: LogTemplateDialogContext
) : DialogWrapper(context.project) {

    private lateinit var nameTextField: JBTextField

    init {
        super.title = title
        isResizable = false
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        beforePanel()

        row {
            nameTextField = textField()
                .label("Name:")
                .bindText(context.mutable.name)
                .align(AlignX.FILL)
                .addValidationRule("Template Name cannot be blank") { it.text.isNullOrBlank() }
                .addValidationRule("Template Name cannot exceed 255 characters") { it.text.length > 255 }
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            comboBox(
                model = EnumComboBoxModel(CxLogLevel::class.java),
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = value.icon
                        label.text = value.name
                    }
                }
            )
                .label("Default level:")
                .bindItem(context.mutable.defaultEffectiveLevel)
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)

        afterPanel()
    }

    open fun Panel.beforePanel() {

    }

    open fun Panel.afterPanel() {

    }

    override fun getPreferredFocusedComponent(): JComponent = nameTextField

}

class CxAdvancedCustomLogTemplateDialog(
    context: LogTemplateDialogContext
) : CxCustomLogTemplateDialog(context) {

    override fun Panel.beforePanel() {
        if (context.duplicatedSourceTemplates) {
            row {
                cell(
                    InlineBanner(
                        """
                            Some loggers are defined more than once.
                            <br/>
                            For each duplicated logger, only the logger <br/>
                            from the last template will be kept and <br/>
                            earlier ones will be overwritten.
                        """.trimIndent(),
                        EditorNotificationPanel.Status.Warning
                    )
                        .showCloseButton(false)
                )
                    .align(AlignX.FILL)
                    .resizableColumn()
            }.resizableRow()
        }

    }


    override fun Panel.afterPanel() {
        group("Additional Parameters") {
            row {
                checkBox("Remove source templates")
                    .bindSelected(context.removeSourceTemplates)
            }
        }
    }
}