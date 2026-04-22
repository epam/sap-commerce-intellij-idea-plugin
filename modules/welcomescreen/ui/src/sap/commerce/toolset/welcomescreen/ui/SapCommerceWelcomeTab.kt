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

package sap.commerce.toolset.welcomescreen.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.impl.welcomeScreen.TabbedWelcomeScreen.DefaultWelcomeScreenTab
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.IconUtil
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ui.font
import sap.commerce.toolset.welcomescreen.RecentSapCommerceProjectsManager
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import java.awt.Font
import javax.swing.JComponent

class SapCommerceWelcomeTab(parentDisposable: Disposable) : DefaultWelcomeScreenTab(i18n("hybris.welcometab.name")), Disposable {

    private val listModel = CollectionListModel<RecentSapCommerceProject>()
    private val projectList = SapCommerceProjectList(this, listModel)

    /** Built once and reused on every `buildComponent` call, so the hierarchy listener stays attached. */
    private val builtComponent: JComponent by lazy { createComponent() }

    init {
        Disposer.register(parentDisposable, this)
        subscribeToRecentProjectsChanges()
        RecentSapCommerceProjectsManager.getInstance().loadRecentProjects()
    }

    override fun buildComponent(): JComponent = builtComponent

    private fun createComponent(): JComponent = panel {
        row {
            icon(IconUtil.scale(HybrisIcons.WelcomeTab.PLUGIN_LOGO, null, HEADER_ICON_SCALE))

            label(i18n("hybris.welcometab.header"))
                .resizableColumn()
                .align(AlignX.LEFT)
                .font { JBFont.h2().deriveFont(Font.BOLD) }

            button(i18n("hybris.welcometab.button.import.project")) {
                invokeLater {
                    triggerAction(
                        actionId = "sap.commerce.toolset.import",
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
        border = JBUI.Borders.empty(PANEL_VERTICAL_PADDING, PANEL_HORIZONTAL_PADDING)
        background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
    }

    private fun subscribeToRecentProjectsChanges() = application.messageBus.connect(this).subscribe(
        topic = RecentSapCommerceProjectsManager.TOPIC,
        handler = object : RecentSapCommerceProjectsManager.RecentSapCommerceProjectsListener {
            override fun loading() = projectList.showLoading()

            override fun loaded(recentProjects: List<RecentSapCommerceProject>) {
                listModel.replaceAll(recentProjects)
                projectList.showLoaded()
            }

            override fun changed(recentProject: RecentSapCommerceProject) {
                val projectIndex = listModel.items
                    .indexOfFirst { it.path == recentProject.path }
                    .takeIf { it != -1 }
                    ?: return
                // triggers repaint of the recent project row
                listModel.setElementAt(recentProject, projectIndex)
            }
        })

    override fun dispose() = Unit

    companion object {
        private const val HEADER_ICON_SCALE = 3.125f
        private const val PANEL_VERTICAL_PADDING = 13
        private const val PANEL_HORIZONTAL_PADDING = 12
    }
}
