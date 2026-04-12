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

package sap.commerce.toolset.project.ui

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.project.ProjectState
import sap.commerce.toolset.ui.banner

class ProjectAskForForcedReimportDialog(
    private val project: Project,
    private val projectState: ProjectState.ForceReimport,
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    init {
        title = "Project Reimport Required"
        isResizable = false

        setOKButtonText("Reimport Project")
        super.init()
    }

    override fun getStyle() = DialogStyle.COMPACT

    override fun applyFields() {
        project.triggerAction("sap.commerce.toolset.reimport")
    }

    override fun doCancelAction() {
        invokeLater { project.triggerAction("CloseProject") }
    }

    override fun createNorthPanel() = banner(
        text = """
            This project is in a state created by an unsupported or outdated plugin version.<br>
            Project refresh is not supported.<br>
            A full project reimport is mandatory.<br>
            Continuing without reimport may lead to unpredictable plugin behavior.<br>
        """.trimIndent(),
        status = EditorNotificationPanel.Status.Error
    )


    override fun createCenterPanel() = panel {
        row {
            text(
                """
                Keeping the plugin up to date helps maintain compatibility and prevent issues like this.<br>
                Older project states are difficult to support as the plugin evolves.<br>
                Unfortunately, a full project reimport is required to restore proper functionality.<br>
                If you have any questions, feel free to discuss them in the project’s Slack or submit an issue via the project’s GitHub:<br>
                <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/issues/new">Submit an issue</a><br>
            """.trimIndent()
            )
        }
    }.apply {
        border = JBUI.Borders.empty(16)
    }
}
