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

package sap.commerce.toolset.impex.console

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.impex.exec.context.ImpExToggle
import sap.commerce.toolset.impex.exec.context.ImpExValidationMode
import java.awt.BorderLayout
import java.io.Serial

class ImpExConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<ImpExExecContext>(
    project,
    "[y] ImpEx Console",
    ImpExLanguage,
    coroutineScope
) {

    private lateinit var legacyModeCheckbox: JBCheckBox
    private lateinit var enableCodeExecutionCheckbox: JBCheckBox
    private lateinit var directPersistenceCheckbox: JBCheckBox
    private lateinit var maxThreadsSpinner: JBIntSpinner
    private lateinit var importModeComboBox: ComboBox<ImpExValidationMode>

    init {
        val myPanel = panel {
            row {
                label("UTF-8")

                importModeComboBox = comboBox(
                    model = EnumComboBoxModel(ImpExValidationMode::class.java),
                    renderer = SimpleListCellRenderer.create("...") { value -> value.name }
                )
                    .label("Validation mode:")
                    .component
                    .apply { selectedItem = ImpExValidationMode.IMPORT_STRICT }

                maxThreadsSpinner = spinner(1..Int.MAX_VALUE)
                    .label("Max threads:")
                    .component
                    .apply { value = 1 }

                enableCodeExecutionCheckbox = checkBox("Enable code execution")
                    .selected(true)
                    .component

                directPersistenceCheckbox = checkBox("Direct persistence")
                    .selected(true)
                    .component

                legacyModeCheckbox = checkBox("Legacy mode")
                    .component
            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = ImpExExecContext(
        connection = activeConnection(),
        content = content,
        settings = ImpExExecContext.defaultSettings(activeConnection()).mutable()
            .apply {
                validationMode = importModeComboBox.selectedItem as ImpExValidationMode
                maxThreads = maxThreadsSpinner.number
                legacyMode = if (legacyModeCheckbox.isSelected) ImpExToggle.ON else ImpExToggle.OFF
                enableCodeExecution = if (enableCodeExecutionCheckbox.isSelected) ImpExToggle.ON else ImpExToggle.OFF
                sldEnabled = if (directPersistenceCheckbox.isSelected) ImpExToggle.ON else ImpExToggle.OFF
                distributedMode = ImpExToggle.ON
            }.immutable()
    )

    override fun title(): String = ImpExConstants.IMPEX
    override fun tip(): String = "ImpEx Console"
    override fun execute() = ImpExExecClient.getInstance(project).execute(
        context = context,
        beforeCallback = { _ -> beforeExecution() },
        resultCallback = { _, result -> print(result) }
    )

    override fun activeConnection() = HacExecConnectionService.getInstance(project).activeConnection

    companion object {
        @Serial
        private val serialVersionUID: Long = -8798339041999147739L
    }
}