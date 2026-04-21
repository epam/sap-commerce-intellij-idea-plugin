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

import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ui.addHierarchyListener
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import java.awt.event.HierarchyEvent
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.plaf.FontUIResource
import kotlin.time.Duration.Companion.milliseconds

class SapCommerceWelcomeTab(
    parentDisposable: Disposable
) : DefaultWelcomeScreenTab("SAP Commerce"), Disposable {

    private val listModel = CollectionListModel<RecentSapCommerceProject>()
    private val projectList = SapCommerceProjectList(this, listModel)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * A "something changed" signal fed by every `afterChange` callback on every project
     * currently in the list. The collector below debounces bursts (typical at startup,
     * when many projects resolve near-simultaneously) into a single EDT repaint.
     * [BufferOverflow.DROP_OLDEST] keeps this strictly non-blocking on the emitter side —
     * property setters never wait on UI.
     */
    private val repaintSignal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @Volatile
    private var currentLoadJob: Job? = null

    init {
        Disposer.register(parentDisposable, this)
        startRepaintCollector()
        subscribeToRecentProjectsChanges()
        loadProjects()
    }

    /**
     * `true` between the constructor-time [loadProjects] call and the *first*
     * `SHOWING_CHANGED` event. Suppresses the redundant reload that would otherwise
     * fire when the tab's panel becomes showing for the first time — construction
     * has already kicked off a load and we don't need a second one 50 ms later.
     */
    private var suppressNextShowReload: Boolean = true

    override fun buildComponent(): JComponent = builtComponent

    /** Built once and reused on every `buildComponent` call, so the hierarchy listener stays attached. */
    private val builtComponent: JComponent by lazy { createComponent() }

    private fun createComponent(): JComponent = panel {
        row {
            icon(IconUtil.scale(HybrisIcons.WelcomeTab.PLUGIN_LOGO, null, HEADER_ICON_SCALE))

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

        // Reload project settings whenever the welcome screen switches to this tab.
        // TabbedWelcomeScreen uses a card layout that toggles panel visibility via
        // setVisible(true/false); HierarchyEvent.SHOWING_CHANGED is the corresponding
        // signal. The first such event after construction is suppressed because
        // `init` already triggered a load.
        addHierarchyListener(this@SapCommerceWelcomeTab) { e ->
            if ((e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong()) == 0L) return@addHierarchyListener
            if (!isShowing) return@addHierarchyListener
            if (suppressNextShowReload) {
                suppressNextShowReload = false
                return@addHierarchyListener
            }
            loadProjects()
        }
    }

    private fun startRepaintCollector() {
        scope.launch {
            repaintSignal
                .debounce(REPAINT_DEBOUNCE_MS.milliseconds)
                .collect {
                    withContext(Dispatchers.EDT) { projectList.repaint() }
                }
        }
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
        invokeLater { projectList.showLoading() }

        val localDisposable = Disposer.newDisposable(this)

        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            localDisposable.dispose()
        }

        currentLoadJob = scope.launch(exceptionHandler) {
            val projects = runCatching {
                RecentProjectsManager.getInstance()
                    .asSafely<RecentProjectsManagerBase>()
                    ?.getRecentPaths()
                    ?.asSequence()
                    ?.filter { isSapCommerceProject(it) }
                    ?.map { RecentSapCommerceProject.of(it, scope, localDisposable) { repaintSignal.tryEmit(Unit) } }
                    ?.toList()
                    ?: emptyList()
            }.getOrElse { emptyList() }

            withContext(Dispatchers.EDT) {
                listModel.replaceAll(projects)
                projectList.showLoaded()
            }
        }
    }


    private fun isSapCommerceProject(location: String): Boolean = runCatching {
        Path.of(location)
            .resolve(Project.DIRECTORY_STORE_FOLDER)
            .resolve(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS)
            .fileExists
    }
        .getOrElse { false }

    override fun dispose() = scope.cancel()

    companion object {
        private const val HEADER_ICON_SCALE = 3.125f
        private const val HEADER_FONT_BUMP = 3
        private const val PANEL_VERTICAL_PADDING = 13
        private const val PANEL_HORIZONTAL_PADDING = 12
        private const val REPAINT_DEBOUNCE_MS = 50L
    }
}