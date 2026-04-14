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

package sap.commerce.toolset.project.welcomescreen.ui

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
import com.intellij.openapi.wm.impl.welcomeScreen.TabbedWelcomeScreen.DefaultWelcomeScreenTab
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.welcomescreen.presentation.SapCommerceProject
import java.awt.BorderLayout
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

    private val listModel = CollectionListModel<SapCommerceProject>()
    private val projectList = JBList(listModel)

    init {
        Disposer.register(parentDisposable, this)
        initList()
        loadProjects()
    }

    override fun buildComponent(): JComponent {
        return panel {
            row {
                icon(
                    IconUtil.scale(HybrisIcons.PLUGIN_SETTINGS, null, 3.125f)
                )

                label(i18n("hybris.welcometab.text"))
                    .bold()
                    .resizableColumn()
                    .align(AlignX.LEFT)
                    .applyToComponent {
                        font = FontUIResource(
                            font.deriveFont(font.size2D + JBUIScale.scale(3))
                        )
                    }

                button(i18n("hybris.welcometab.button.import.project")) {
                    invokeLater {
                        triggerAction(
                            actionId = "ImportProject",
                            place = ActionPlaces.WELCOME_SCREEN,
                            uiKind = ActionUiKind.POPUP,
                        )
                    }
                }.align(AlignX.RIGHT)
            }.bottomGap(BottomGap.MEDIUM)

            separator(WelcomeScreenUIManager.getSeparatorColor())

            row {
                val scrollPane = JBScrollPane(projectList).apply {
                    border = JBUI.Borders.empty()
                    background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
                    viewport.background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
                }
                cell(scrollPane)
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()

        }.apply {
            border = JBUI.Borders.empty(13, 12)
            background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        }
    }

    private fun initList() {
        projectList.apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = SapCommerceProjectRenderer()
            background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()

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
        val projects = RecentProjectsManager.getInstance()
            .asSafely<RecentProjectsManagerBase>()
            ?.getRecentPaths()
            ?.asSequence()
            ?.filter { isSapCommerceProject(it) }
            ?.map { SapCommerceProject.of(it) }
            ?.toList()
            ?: emptyList()

        listModel.replaceAll(projects)
    }

    private fun isSapCommerceProject(path: String): Boolean =
        File(File(path), ".idea/hybrisProjectSettings.xml").exists()

    private fun openSelectedProject() {
        val selected = projectList.selectedValue ?: run {
            Messages.showWarningDialog(i18n("hybris.welcometab.message.select.project"), i18n("hybris.welcometab.message.no.selection"))
            return
        }

        ProjectManagerEx.getInstanceEx().openProject(Path.of(selected.path), OpenProjectTask())
    }


    internal class SapCommerceProjectRenderer : JPanel(), ListCellRenderer<SapCommerceProject> {

        private val iconLabel = JLabel()
        private val nameLabel = JLabel()
        private val pathLabel = JLabel()

        init {
            layout = BorderLayout(JBUI.scale(8), 0)
            border = JBUI.Borders.empty(6, 8)
            isFocusable = false

            pathLabel.foreground = JBColor.GRAY
            pathLabel.font = JBUI.Fonts.smallFont()

            val textPanel = JPanel(VerticalFlowLayout(0, 2)).apply {
                isOpaque = false
                add(nameLabel)
                add(pathLabel)
            }

            add(iconLabel, BorderLayout.WEST)
            add(textPanel, BorderLayout.CENTER)
        }

        override fun getListCellRendererComponent(
            list: JList<out SapCommerceProject>,
            value: SapCommerceProject,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {

            iconLabel.icon = value.projectIcon
            nameLabel.text = value.displayName
            pathLabel.text = value.locationRelativeToUserHome

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