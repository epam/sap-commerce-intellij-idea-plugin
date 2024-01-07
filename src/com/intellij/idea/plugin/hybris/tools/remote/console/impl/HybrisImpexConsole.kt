/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleRootType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon
import javax.swing.JPanel

class HybrisImpexConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_IMPEX, ImpexLanguage) {

    object MyConsoleRootType : ConsoleRootType("hybris.impex.shell", null)

    private val panel = JPanel(WrappedFlowLayout(0, 0))

    private val legacyModeCheckbox = JBCheckBox()
    private val legacyModeLabel = JBLabel("Legacy mode: ")

    init {
        createUI()
        ConsoleHistoryController(MyConsoleRootType, "hybris.impex.shell", this).install()
    }

    override fun title(): String = HybrisConstants.IMPEX
    override fun tip(): String = "ImpEx Console"
    override fun icon(): Icon = HybrisIcons.IMPEX_FILE

    override fun execute(query: String): HybrisHttpResult {
        val requestParams = getRequestParams(query)
        return HybrisHacHttpClient.getInstance(project).importImpex(project, requestParams)
    }

    fun validate(query: String): HybrisHttpResult {
        val requestParams = getRequestParams(query)
        return HybrisHacHttpClient.getInstance(project).validateImpex(project, requestParams)
    }

    private fun createUI() {
        legacyModeLabel.border = JBUI.Borders.empty(0, 10, 0, 5)
        legacyModeCheckbox.border = JBUI.Borders.emptyRight(5)

        panel.add(legacyModeLabel)
        panel.add(legacyModeCheckbox)

        add(panel, BorderLayout.NORTH)
        isEditable = true
    }

    private fun getRequestParams(query: String): MutableMap<String, String> {
        val requestParams = mutableMapOf(
            "scriptContent" to query,
            "validationEnum" to "IMPORT_STRICT", // IMPORT_RELAXED
            "encoding" to "UTF-8",
            "maxThreads" to "4", // Max Threads
            "_legacyMode" to "on", // Legacy Mode
            "enableCodeExecution" to "true", // Enable code execution
            "_enableCodeExecution" to "on", // Enable code execution
            "_distributedMode" to "on", // Distributed mode
            "_sldEnabled" to "on", // Direct Persistence
        )
        if (legacyModeCheckbox.isSelected) {
            requestParams["legacyMode"] = "true"
        }
        return requestParams
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -8798339041999147739L
    }
}