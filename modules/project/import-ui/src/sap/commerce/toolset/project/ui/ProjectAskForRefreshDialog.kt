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
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.project.ProjectState
import sap.commerce.toolset.settings.WorkspaceSettings
import sap.commerce.toolset.ui.banner
import sap.commerce.toolset.ui.browserLink
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.ScrollPaneConstants

class ProjectAskForRefreshDialog(
    private val project: Project,
    private val projectState: ProjectState.Refresh,
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    private val skip = object : DialogWrapperAction("Don't Ask Again") {
        @Serial
        private val serialVersionUID: Long = -1963011685030505631L

        override fun doAction(e: ActionEvent) {
            val decision = MessageDialogBuilder.yesNo(
                title = "Ignore Project Refresh",
                message = "Do not show this dialog again for the plugin '${projectState.currentVersion}' version. It may appear again after updating the plugin.",
                icon = HybrisIcons.Project.REFRESH,
            ).ask(project)

            if (decision) with(WorkspaceSettings.getInstance(project)) {
                doNotAskForProjectImport = doNotAskForProjectImport.toMutableMap()
                    .apply { put(projectState.currentVersion, true) }

                this@ProjectAskForRefreshDialog.close(CLOSE_EXIT_CODE)
            }
        }
    }

    init {
        title = "Project Refresh Required"
        isResizable = false

        setCancelButtonText("Remind Me Later")
        setOKButtonText("Refresh Project...")
        super.init()
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun createLeftSideActions() = arrayOf(skip)

    override fun applyFields() {
        invokeLater { project.triggerAction("sap.commerce.toolset.yRefresh") }
    }

    override fun createNorthPanel() = banner(
        text = """
            Your project was imported with older version of the plugin <strong>${projectState.importedByVersion}</strong>.<br>
            It is highly advisable to refresh the project to align with the following changes:
        """.trimIndent(),
        status = EditorNotificationPanel.Status.Warning
    )

    override fun createCenterPanel() = panel {
        row {
            scrollCell(detailsPanel())
                .align(Align.FILL)
                .applyToComponent {
                    parent.parent.asSafely<JBScrollPane>()?.apply {
                        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                        border = JBEmptyBorder(0)
                        preferredSize = Dimension(JBUIScale.scale(600), JBUIScale.scale(400))
                    }
                }
        }.resizableRow()
    }

    private fun detailsPanel() = panel {
        projectState.refreshRequests
            .sortedByDescending { it.milestone }
            .groupBy { it.milestone }
            .forEach { (milestone, prs) ->
                group("Release: $milestone") {
                    row {
                        icon(HybrisIcons.Project.CONTRIBUTORS)
                        comment("Contributors: ")
                        prs.map { it.author }.distinct()
                            .forEach { author -> browserLink(author, "https://github.com/$author") }
                    }

                    prs.forEach { pr ->
                        row {
                            browserLink(
                                text = "#${pr.number}",
                                tooltip = "Refresh request",
                                url = "https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/${pr.number}"
                            )
                            text(pr.title)
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }
            }
    }.apply { border = JBUI.Borders.empty(16) }

}
