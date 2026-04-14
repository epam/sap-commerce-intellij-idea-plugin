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

import com.intellij.codeInsight.hints.presentation.MouseButton
import com.intellij.codeInsight.hints.presentation.mouseButton
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
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
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addMouseMotionListener
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.Serial
import java.nio.file.Path
import javax.swing.*
import javax.swing.plaf.FontUIResource

class SapCommerceWelcomeTab(parentDisposable: Disposable) : DefaultWelcomeScreenTab("SAP Commerce"), Disposable {

    private val listModel = CollectionListModel<SapCommerceProject>()
    private val projectList = object : JBList<SapCommerceProject>(listModel) {
        @Serial
        private val serialVersionUID: Long = 2925119990657205456L

        init {
            addListSelectionListener {
                if (this.selectedIndex != -1) {
                    SwingUtilities.invokeLater { this.clearSelection() }
                }
            }
        }

        override fun processMouseEvent(e: MouseEvent) {
            val index = locationToIndex(e.point)
            val onRow = index >= 0 && getCellBounds(index, index)?.contains(e.point) == true
            if (!onRow) {
                // Swallow the event entirely — no selection change, no click dispatched
                return
            }
            super.processMouseEvent(e)
        }

        override fun processMouseMotionEvent(e: MouseEvent) {
            val index = locationToIndex(e.point)
            val onRow = index >= 0 && getCellBounds(index, index)?.contains(e.point) == true
            if (!onRow) {
                // Manually deliver to custom listeners only — skip super entirely
                // Don't forward to BasicListUI, but still deliver to registered listeners
                // so hover-out logic in mouseMoved can clear the hover state.
                for (listener in mouseMotionListeners) {
                    if (e.id == MouseEvent.MOUSE_MOVED) listener.mouseMoved(e)
                    else if (e.id == MouseEvent.MOUSE_DRAGGED) listener.mouseDragged(e)
                }
                return
            }
            super.processMouseMotionEvent(e)
        }
    }

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
            }.bottomGap(BottomGap.SMALL)

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
            background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
            selectionBackground = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
            border = JBUI.Borders.empty(0, 4)
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = SapCommerceProjectRenderer()

            val mouseHandler = object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.mouseButton == MouseButton.Left && e.clickCount == 1) {
                        val index = locationToIndex(e.point)
                        if (index >= 0 && getCellBounds(index, index)?.contains(e.point) == true) {
                            val project = listModel.getElementAt(index)
                            ProjectManagerEx.getInstanceEx().openProject(Path.of(project.path), OpenProjectTask())
                        }
                    }
                }

                override fun mouseMoved(e: MouseEvent) {
                    val index = locationToIndex(e.point)
                    val validIndex = if (index >= 0 && getCellBounds(index, index)?.contains(e.point) == true) index else -1
                    val previous = getClientProperty(SapCommerceProjectRenderer.HOVERED_INDEX_KEY) as? Int ?: -1
                    if (previous != validIndex) {
                        putClientProperty(SapCommerceProjectRenderer.HOVERED_INDEX_KEY, validIndex)
                        repaint()
                    }
                }

                override fun mouseExited(e: MouseEvent) {
                    if (getClientProperty(SapCommerceProjectRenderer.HOVERED_INDEX_KEY) != -1) {
                        putClientProperty(SapCommerceProjectRenderer.HOVERED_INDEX_KEY, -1)
                        repaint()
                    }
                }
            }
            addMouseListener(this@SapCommerceWelcomeTab, mouseHandler)
            addMouseMotionListener(this@SapCommerceWelcomeTab, mouseHandler)
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

    private class SapCommerceProjectRenderer : JPanel(), ListCellRenderer<SapCommerceProject> {

        private val iconLabel = JLabel().apply {
            verticalAlignment = SwingConstants.TOP
        }
        private val nameLabel = JLabel()
        private val pathLabel = JLabel()

        private val pillColor: Color = UIManager.getColor("List.selectionBackground")
            ?: JBUI.CurrentTheme.List.Hover.background(true)

        private var selected = false
        private var hovered = false

        init {
            layout = BorderLayout(JBUI.scale(12), 0)
            border = JBUI.Borders.empty(10, 20)
            isFocusable = false
            isOpaque = false

            pathLabel.foreground = JBColor.GRAY
            pathLabel.font = JBUI.Fonts.smallFont()

            val textPanel = JPanel(VerticalFlowLayout(0, 4)).apply {
                isOpaque = false
                add(nameLabel)
                add(pathLabel)
            }

            val iconWrapper = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(iconLabel, BorderLayout.NORTH)
            }

            add(iconWrapper, BorderLayout.WEST)
            add(textPanel, BorderLayout.CENTER)
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Always cover the whole cell with the panel background to hide BasicListUI's selection paint
                g2.color = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
                g2.fillRect(0, 0, width, height)

                // Draw the rounded pill on top
                if (selected || hovered) {
                    g2.color = pillColor
                    val arc = JBUI.scale(12)
                    val inset = JBUI.scale(8)
                    g2.fillRoundRect(inset, 0, width - 2 * inset, height, arc, arc)
                }
            } finally {
                g2.dispose()
            }
            super.paintComponent(g)
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
            pathLabel.text = FileUtil.getLocationRelativeToUserHome(value.path)

            selected = isSelected
            hovered = !isSelected && index == (list.getClientProperty(HOVERED_INDEX_KEY) as? Int ?: -1)

            return this
        }

        companion object {
            const val HOVERED_INDEX_KEY = "SapCommerceProjectRenderer.hoveredIndex"

            @Serial
            private const val serialVersionUID: Long = 8828202110264000188L
        }
    }

    override fun dispose() = Unit
}