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

package sap.commerce.toolset.project.ui

import com.intellij.CommonBundle
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.path
import sap.commerce.toolset.ui.banner
import javax.swing.Action

class ProjectReImportDialog(
    private val project: Project,
) : DialogWrapper(project) {

    init {
        title = "Reimport the Project"
        isResizable = false
        super.init()

        setCancelButtonText(CommonBundle.getCloseButtonText())
    }

    override fun createActions(): Array<out Action?> {
        val helpAction = getHelpAction()

        return if (helpAction === myHelpAction && helpId == null) arrayOf(cancelAction)
        else arrayOf(cancelAction, helpAction)
    }

    override fun createNorthPanel() = banner(
        text = "RE-IMPORT REQUIRED! Project refresh is not supported.",
        status = EditorNotificationPanel.Status.Error
    )

    override fun createCenterPanel() = panel {
        row {
            cell(onlyImportUi())
        }
    }

    private fun onlyImportUi() = panel {
        row {
            text(
                """
This project cannot be refreshed using the current version of the plugin.
<br>
The plugin’s project import and refresh implementation has changed in a way that is not compatible with projects imported by earlier plugin versions.
<br><br>
Due to internal API and project model changes in the plugin, existing project configurations cannot be safely updated through a Refresh Project operation.
<br>
Attempting to refresh may result in an incomplete project model, incorrect indexing, or build configuration issues.
<br><br>
You must re-import it from scratch using the current plugin version.
<br>
This will recreate the IntelliJ IDEA project model using the updated import logic.
<br><br>
If you encounter any issues, please submit <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/issues">new bug-report on GitHub</a> or
<br>
contact <a href="https://www.linkedin.com/in/michaellytvyn/">Mykhailo Lytvyn</a> in the project <a href="https://join.slack.com/t/sapcommercede-0kz9848/shared_invite/zt-29gnz3fd2-mz_69mla52NOFqGGsG1Zjw">Slack</a>.
""".trimIndent()
            )
                .align(Align.FILL)
        }

        separator()

        row {
            text(
                """
                    Technical references:<br>
                    - Migration to Workspace API  <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1699">Project Import & Refresh 3.0</a> | ~533 changed files<br>
                    - Enhanced project refresh <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/1704">Project Import & Refresh 3.1</a> | ~200 changed files
                """.trimIndent()
            )
                .align(Align.FILL)
        }

        separator()

        row {
            link("Re-import the project...") {
                this@ProjectReImportDialog.doCancelAction()

                val projectDirectory = project.path
                    ?.let { path -> VfsUtil.findFile(path, true) }
                    ?: return@link

                project.triggerAction("CloseProject")

                invokeLater {
                    triggerAction(
                        actionId = "sap.commerce.toolset.reimport",
                        place = ActionPlaces.NEW_PROJECT_WIZARD,
                        uiKind = ActionUiKind.POPUP,
                        dataContextProvider = {
                            SimpleDataContext.builder()
                                .add(CommonDataKeys.VIRTUAL_FILE, projectDirectory)
                                .build()
                        })
                }
            }
                .align(Align.CENTER)
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16, 32, 16)
    }

    override fun getStyle(): DialogStyle = DialogStyle.COMPACT
}