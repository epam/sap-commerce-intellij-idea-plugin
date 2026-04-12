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

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
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
import sap.commerce.toolset.ui.banner
import sap.commerce.toolset.ui.browserLink
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.ScrollPaneConstants

class ProjectAskForReimportDialog(
    private val project: Project,
    private val projectState: ProjectState.Reimport,
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    private val skip = object : DialogWrapperAction("Don't Ask Again") {
        @Serial
        private val serialVersionUID: Long = -1963011685030505631L

        override fun doAction(e: ActionEvent) {
            // TODO: save skip flag
            this@ProjectAskForReimportDialog.close(CLOSE_EXIT_CODE)
        }
    }

    init {
        title = "Project Reimport Required"
        isResizable = false

        setCancelButtonText("Remind Me Later")
        setOKButtonText("Reimport Project...")
        super.init()
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun createLeftSideActions() = arrayOf(skip)

    override fun applyFields() {
        invokeLater {
            triggerAction(
                actionId = "sap.commerce.toolset.reimport",
                place = ActionPlaces.NEW_PROJECT_WIZARD,
                uiKind = ActionUiKind.POPUP,
                dataContextProvider = {
                    SimpleDataContext.builder()
                        .add(CommonDataKeys.VIRTUAL_FILE, projectState.projectDirectory)
                        .build()
                })
        }
    }

    override fun createNorthPanel() = banner(
        text = """
            Your project was imported with an older version of the plugin <strong>${projectState.importedByVersion}</strong>.<br>
            This version does not support project refresh. A full project reimport is required.<br>
            Without reimport, plugin behavior may be unpredictable.<br>
            It is strongly recommended to reimport the project to apply related changes:
        """.trimIndent(),
        status = EditorNotificationPanel.Status.Error
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
        val combinedRequests = projectState.reimportRequests + projectState.refreshRequests
        combinedRequests
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

                    prs
                        .sortedBy { it.type }
                        .forEach { pr ->
                            row {
                                browserLink(
                                    pr.type.icon,
                                    "#${pr.number} ",
                                    pr.type.presentationTitle,
                                    "https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/${pr.number}"
                                )
                                text(pr.title)
                            }.layout(RowLayout.PARENT_GRID)
                        }
                }
            }
    }.apply { border = JBUI.Borders.empty(16) }
}
