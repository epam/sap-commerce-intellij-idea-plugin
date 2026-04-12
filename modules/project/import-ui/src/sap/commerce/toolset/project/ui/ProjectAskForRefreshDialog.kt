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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.project.ProjectState

class ProjectAskForRefreshDialog(
    private val project: Project,
    private val projectState: ProjectState.Refresh,
) : DialogWrapper(project, false, IdeModalityType.IDE) {

    init {
        title = "Project Refresh Required"

        super.init()
    }

    override fun createCenterPanel() = panel {
        projectState.pullRequests.groupBy { it.milestone }
            .forEach { (milestone, prs) ->
                group("Release: $milestone") {
                    prs.forEach { pr ->
                        row {
                            browserLink("#${pr.number}", "https://github.com/epam/sap-commerce-intellij-idea-plugin/pull/${pr.number}")
                                .comment("PR")

                            label(pr.title)
                                .comment("by ${pr.author}")
                        }.layout(RowLayout.PARENT_GRID)
                    }
                }
            }
    }
}
