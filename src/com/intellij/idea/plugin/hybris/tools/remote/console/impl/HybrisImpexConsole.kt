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

package com.intellij.idea.plugin.hybris.tools.remote.console.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.Toggle
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ValidationMode
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

@Service(Service.Level.PROJECT)
class HybrisImpexConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<ImpExExecutionContext>(
    project,
    HybrisConstants.CONSOLE_TITLE_IMPEX,
    ImpexLanguage,
    coroutineScope
) {

    private val legacyModeCheckbox = JBCheckBox("Legacy mode")
        .also { it.border = borders10 }
    private val enableCodeExecutionCheckbox = JBCheckBox("Enable code execution", true)
        .also { it.border = borders10 }
    private val directPersistenceCheckbox = JBCheckBox("Direct persistence", true)
        .also { it.border = borders10 }
    private val maxThreadsSpinner = JSpinner(SpinnerNumberModel(1, 1, 100, 1))
        .also { it.border = borders5 }
    private val importModeComboBox = ComboBox(ValidationMode.entries.toTypedArray(), 175)
        .also {
            it.border = borders5
            it.selectedItem = ValidationMode.IMPORT_STRICT
            it.renderer = SimpleListCellRenderer.create("...") { value -> value.name }
        }

    init {
        val panel = JPanel(WrappedFlowLayout(0, 0))
        panel.add(JBLabel("UTF-8").also { it.border = borders10 })
        panel.add(JBLabel("Validation mode:").also { it.border = bordersLabel })
        panel.add(importModeComboBox)
        panel.add(JBLabel("Max threads:").also { it.border = bordersLabel })
        panel.add(maxThreadsSpinner)
        panel.add(enableCodeExecutionCheckbox)
        panel.add(directPersistenceCheckbox)
        panel.add(legacyModeCheckbox)

        add(panel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = ImpExExecutionContext(
        content = content,
        validationMode = importModeComboBox.selectedItem as ValidationMode,
        maxThreads = maxThreadsSpinner.value.toString().toInt(),
        legacyMode = if (legacyModeCheckbox.isSelected) Toggle.ON else Toggle.OFF,
        enableCodeExecution = if (enableCodeExecutionCheckbox.isSelected) Toggle.ON else Toggle.OFF,
        sldEnabled = if (directPersistenceCheckbox.isSelected) Toggle.ON else Toggle.OFF,
        distributedMode = Toggle.ON,
    )

    override fun title(): String = HybrisConstants.IMPEX
    override fun tip(): String = "ImpEx Console"

    companion object {
        @Serial
        private val serialVersionUID: Long = -8798339041999147739L
    }
}