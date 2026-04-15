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
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
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
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.welcomescreen.presentation.SapCommerceProject
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addMouseMotionListener
import sap.commerce.toolset.util.fileExists
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.plaf.FontUIResource

class SapCommerceWelcomeTab(
    parentDisposable: Disposable
) : DefaultWelcomeScreenTab("SAP Commerce"), Disposable {

    private val listModel = CollectionListModel<SapCommerceProject>()
    private val projectList = SapCommerceProjectList(this, listModel)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var currentLoadJob: Job? = null

    init {
        Disposer.register(parentDisposable, this)
        initList()
        subscribeToRecentProjectsChanges()
        loadProjects()
    }

    override fun buildComponent(): JComponent = panel {
        row {
            icon(IconUtil.scale(HybrisIcons.PLUGIN_SETTINGS, null, HEADER_ICON_SCALE))

            label(i18n("hybris.welcometab.text"))
                .bold()
                .resizableColumn()
                .align(AlignX.LEFT)
                .applyToComponent {
                    font = FontUIResource(
                        font.deriveFont(font.size2D + JBUIScale.scale(HEADER_FONT_BUMP))
                    )
                }

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

    private fun initList() {
        //TODO move inside projectList
        val mouseHandler = SapCommerceProjectMouseHandler(projectList, listModel)
        projectList
            .addMouseListener(this, mouseHandler)
            .addMouseMotionListener(this, mouseHandler)
            .cellRenderer = SapCommerceProjectRenderer()
    }

    private fun subscribeToRecentProjectsChanges() = ApplicationManager.getApplication().messageBus
        .connect(this)
        .subscribe(
            RecentProjectsManager.RECENT_PROJECTS_CHANGE_TOPIC,
            object : RecentProjectsManager.RecentProjectsChange {
                override fun change() {
                    loadProjects()
                }
            }
        )

    private fun loadProjects() {
        currentLoadJob?.cancel()
        currentLoadJob = scope.launch {
            val projects = RecentProjectsManager.getInstance()
                .asSafely<RecentProjectsManagerBase>()
                ?.getRecentPaths()
                ?.asSequence()
                ?.filter { isSapCommerceProject(it) }
                ?.map { SapCommerceProject.of(it) }
                ?.toList()
                ?: emptyList()

            withContext(Dispatchers.EDT) {
                listModel.replaceAll(projects)
            }
        }
    }

    private fun isSapCommerceProject(location: String): Boolean = runCatching {
        Path.of(location)
            .resolve(ProjectConstants.Directory.IDEA)
            .resolve("hybrisProjectSettings.xml")
            .fileExists
    }
        .getOrElse { false }

    override fun dispose() = scope.cancel()

    companion object {
        private const val HEADER_ICON_SCALE = 3.125f
        private const val HEADER_FONT_BUMP = 3
        private const val PANEL_VERTICAL_PADDING = 13
        private const val PANEL_HORIZONTAL_PADDING = 12
    }
}