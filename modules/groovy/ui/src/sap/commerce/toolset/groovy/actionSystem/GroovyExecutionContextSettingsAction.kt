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
package sap.commerce.toolset.groovy.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.groovy.editor.groovyExecContextSettings
import sap.commerce.toolset.groovy.exec.GroovyExecExceptionHandling
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.exec.groovyExecContextSettings
import sap.commerce.toolset.hac.actionSystem.ExecutionContextSettingsAction
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import javax.swing.LayoutFocusTraversalPolicy

class GroovyExecutionContextSettingsAction : ExecutionContextSettingsAction<GroovyExecContext.Settings.Mutable>() {

    override fun previewSettings(e: AnActionEvent, project: Project): String = e.groovyExecContextSettings { GroovyExecContext.defaultSettings() }
        .let {
            """<pre>
 · timeout           : ${it.timeout} ms</pre>
 · exception handling: ${it.exceptionHandling.presentationText}</pre>
                """.trimIndent()
        }

    override fun settings(e: AnActionEvent, project: Project): GroovyExecContext.Settings.Mutable {
        val settings = e.groovyExecContextSettings {
            val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection
            GroovyExecContext.defaultSettings(connectionSettings)
        }

        return settings.mutable()
    }

    override fun applySettings(virtualFile: VirtualFile, settings: GroovyExecContext.Settings.Mutable) {
        virtualFile.groovyExecContextSettings = settings.immutable()
    }

    override fun settingsPanel(e: AnActionEvent, project: Project, settings: GroovyExecContext.Settings.Mutable) = panel {
        row {
            textField()
                .align(AlignX.FILL)
                .label("Timeout:")
                .validationOnInput {
                    if (it.text.toIntOrNull() == null) error(UIBundle.message("please.enter.a.number.from.0.to.1", 1, Int.MAX_VALUE))
                    else null
                }
                .focused()
                .commentRight("(ms)")
                .bindIntText(settings::timeout)
        }.layout(RowLayout.PARENT_GRID)

        row {
            comboBox(
                model = EnumComboBoxModel(GroovyExecExceptionHandling::class.java),
                renderer = SimpleListCellRenderer.create("...") { value -> value.presentationText }
            )
                .label("Exception handling:")
                .comment("Used in case of non-default Web Context.")
                .bindItem(settings::exceptionHandling.toNullableProperty())
        }.layout(RowLayout.PARENT_GRID)
    }
        .apply {
            border = JBUI.Borders.empty(8, 16)
            focusTraversalPolicy = LayoutFocusTraversalPolicy()
            isFocusCycleRoot = true
        }
}
