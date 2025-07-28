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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.ide.BrowserUtil
import com.intellij.idea.plugin.hybris.actions.ExecutionContextSettingsAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.editor.impexExecutionContextSettings
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import javax.swing.LayoutFocusTraversalPolicy

class ImpExExecutionContextSettingsAction : ExecutionContextSettingsAction<ImpExExecutionContext.ModifiableSettings>() {

    override fun previewSettings(e: AnActionEvent, project: Project): String = e.impexExecutionContextSettings { ImpExExecutionContext.DEFAULT_SETTINGS }
        .let {
            """<pre>
 · validation mode:       ${it.validationMode.title}
 · max threads:           ${it.maxThreads}
 · encoding:              ${it.encoding}
 · legacy mode:           ${it.legacyMode.value}
 · enable code execution: ${it.enableCodeExecution.value}
 · direct persistence:    ${it.sldEnabled.value}
 · distributed mode:      ${it.distributedMode.value}</pre>
                """.trimIndent()
        }

    override fun settings(e: AnActionEvent, project: Project) = e
        .impexExecutionContextSettings() { ImpExExecutionContext.DEFAULT_SETTINGS }
        .modifiable()

    override fun applySettings(editor: Editor, settings: ImpExExecutionContext.ModifiableSettings) {
        editor.putUserData(HybrisConstants.KEY_IMPEX_EXECUTION_SETTINGS, settings.immutable())
    }

    override fun settingsPanel(e: AnActionEvent, project: Project, settings: ImpExExecutionContext.ModifiableSettings) = panel {
        row {
            comboBox(
                EnumComboBoxModel(ImpExExecutionContext.ValidationMode::class.java),
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    label.text = value.title
                })
                .align(AlignX.FILL)
                .label("Validation mode:")
                .comment("Read more about ImpEx validation modes <a href='link'>here</a>.")
                { BrowserUtil.browse("https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/c703c0bd88bd4281a09163658c66fac8.html?locale=en-US") }
                .bindItem({ settings.validationMode }, { value -> settings.validationMode = value ?: ImpExExecutionContext.ValidationMode.IMPORT_STRICT })
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .align(AlignX.FILL)
                .label("Max threads:")
                .validationOnInput {
                    if (it.text.toIntOrNull() == null) error(UIBundle.message("please.enter.a.number.from.0.to.1", 1, Int.MAX_VALUE))
                    else null
                }
                .bindIntText({ settings.maxThreads }, { value -> settings.maxThreads = value })
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .align(AlignX.FILL)
                .label("Encoding:")
                .validationOnInput {
                    if (it.text.isBlank()) error("Please enter an encoding")
                    else null
                }
                .bindText({ settings.encoding }, { value -> settings.encoding = value })
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Enable code execution")
                .align(AlignX.FILL)
                .bindSelected({ settings.enableCodeExecution.booleanValue }, { value -> settings.enableCodeExecution = ImpExExecutionContext.Toggle.of(value) })
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Legacy mode")
                .align(AlignX.FILL)
                .bindSelected({ settings.legacyMode.booleanValue }, { value -> settings.legacyMode = ImpExExecutionContext.Toggle.of(value) })
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Direct persistence")
                .align(AlignX.FILL)
                .comment("Enables the <a href='link'>Service Layer Direct</a> mode.")
                { BrowserUtil.browse("https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/ccf4dd14636b4f7eac2416846ffd5a70.html?locale=en-US") }
                .bindSelected({ settings.sldEnabled.booleanValue }, { value -> settings.sldEnabled = ImpExExecutionContext.Toggle.of(value) })
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Distributed mode")
                .align(AlignX.FILL)
                .comment("Read more about distributed ImpEx <a href='link'>here</a>.")
                { BrowserUtil.browse("https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/f849db85d68740ed870e729a3688a19d.html?locale=en-US") }
                .bindSelected({ settings.distributedMode.booleanValue }, { value -> settings.distributedMode = ImpExExecutionContext.Toggle.of(value) })
        }.layout(RowLayout.PARENT_GRID)
    }
        .apply {
            border = JBUI.Borders.empty(8, 16)
            focusTraversalPolicy = LayoutFocusTraversalPolicy()
            isFocusCycleRoot = true
        }
}
