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

package sap.commerce.toolset.project.welcomescreen

import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.wm.impl.welcomeScreen.TabbedWelcomeScreen.DefaultWelcomeScreenTab
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.Serial
import java.nio.file.Path
import javax.swing.*
import javax.swing.plaf.FontUIResource

class SapCommerceWelcomeTab(parentDisposable: Disposable) :
    DefaultWelcomeScreenTab("SAP Commerce"), Disposable {

    private val listModel = CollectionListModel<SapProject>()
    private val projectList = JBList(listModel)

    init {
        Disposer.register(parentDisposable, this)
        initList()
        loadProjects()
    }

    override fun buildComponent(): JComponent {
        return panel {
            row {
                icon(HybrisIcons.PLUGIN_SETTINGS)

                label("SAP Commerce Projects")
                    .bold()
                    .applyToComponent {
                        font = FontUIResource(
                            font.deriveFont(font.size2D + JBUIScale.scale(3))
                        )
                    }
            }.bottomGap(BottomGap.MEDIUM)

            separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)

            row {
                scrollCell(projectList)
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()

            row {
                button("Refresh") { loadProjects() }
                button("Open") { openSelectedProject() }
                button("Import Project") {

                    invokeLater {
                        triggerAction(
                            actionId = "ImportProject",
                            place = ActionPlaces.WELCOME_SCREEN,
                            uiKind = ActionUiKind.POPUP,
                            )
                    }
                }
            }
                //.align(AlignX.RIGHT)
        }.apply {
            border = JBUI.Borders.empty(16)
            background = UIUtil.getPanelBackground()
        }
    }

    private fun initList() {
        projectList.apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = SapProjectRenderer()

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        openSelectedProject()
                    }
                }
            })
        }
    }

    private fun loadProjects() {
        val recentProjectManager = RecentProjectsManager.getInstance() as RecentProjectsManagerBase
        val paths = recentProjectManager.getRecentPaths()

        val sapProjects = paths
            .asSequence()
            .filter { isSapCommerceProject(it) }
            .map { SapProject.of(it) }
            .toList()

        listModel.removeAll()
        listModel.add(sapProjects)
    }

    private fun isSapCommerceProject(path: String): Boolean =
        File(File(path), ".idea/hybrisProjectSettings.xml").exists()

    private fun openSelectedProject() {
        val selected = projectList.selectedValue ?: run {
            Messages.showWarningDialog("Please select a project", "No Selection")
            return
        }

        ProjectManagerEx.getInstanceEx().openProject(Path.of(selected.path), OpenProjectTask())
    }

    // ========================= MODEL =========================

    private data class SapProject(
        val path: String,
        val displayName: String,
        val projectName: String,
        val projectIcon: Icon
    ) {
        companion object {
            fun of(path: String): SapProject {
                val manager = RecentProjectsManager.getInstance() as RecentProjectsManagerBase
                val projectName = manager.getProjectName(path)
                val displayName = manager.getDisplayName(path) ?: projectName
                val icon = RecentProjectsManagerBase.getInstanceEx().getProjectIcon(path, true)

                return SapProject(path, displayName, projectName, icon)
            }
        }
    }

    // ========================= RENDERER =========================

    private class SapProjectRenderer : JPanel(), ListCellRenderer<SapProject> {

        private val nameLabel = JLabel()
        private val pathLabel = JLabel()

        init {
            layout = VerticalFlowLayout()
            border = JBUI.Borders.empty(6)
            isFocusable = false

            pathLabel.foreground = JBColor.GRAY

            add(nameLabel)
            add(pathLabel)
        }

        override fun getListCellRendererComponent(
            list: JList<out SapProject>,
            value: SapProject,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {

            nameLabel.icon = value.projectIcon
            nameLabel.text = value.displayName
            pathLabel.text = FileUtil.getLocationRelativeToUserHome(value.path)

            background = if (isSelected) list.selectionBackground else list.background

            return this
        }

        companion object {
            @Serial
            private const val serialVersionUID: Long = 8828202110264000188L
        }
    }

    override fun dispose() = Unit
}