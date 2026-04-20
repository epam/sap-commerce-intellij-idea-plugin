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
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
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

    /**
     * Disposable scoped to the lifetime of the *currently displayed* list of projects.
     * Each reload disposes this and allocates a fresh one: all per-project property
     * subscriptions for the previous set are removed in a single step, and the new set
     * attaches its own. Always a child of `this`, so plugin teardown cleans everything up.
     *
     */
    private var projectsDisposable: Disposable

    init {
        Disposer.register(parentDisposable, this)
        projectsDisposable = newProjectsDisposable()
        startRepaintCollector()
        subscribeToRecentProjectsChanges()
        loadProjects()
    }

    override fun buildComponent(): JComponent = panel {
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
        currentLoadJob = scope.launch {
            // Allocate a fresh disposable *before* building new project instances, and
            // dispose the previous one so its observable-property subscriptions are
            // detached in one shot. Projects created by the previous reload are about
            // to become unreachable via the list model; their background coroutines
            // simply finish and their results are discarded.
            val newDisposable = swapProjectsDisposable()

            val projects = runCatching {
                RecentProjectsManager.getInstance()
                    .asSafely<RecentProjectsManagerBase>()
                    ?.getRecentPaths()
                    ?.asSequence()
                    ?.filter { isSapCommerceProject(it) }
                    ?.map { RecentSapCommerceProject.of(it, scope) }
                    ?.toList()
                    ?: emptyList()
            }.getOrElse { emptyList() }

            // Wire each new project's observable properties to the shared repaint signal.
            // Subscriptions are tied to `newDisposable`, which lives until the next reload.
            for (project in projects) {
                project.hybrisVersionProperty.afterChange(newDisposable) { repaintSignal.tryEmit(Unit) }
                project.hostingEnvironmentProperty.afterChange(newDisposable) { repaintSignal.tryEmit(Unit) }
                project.gitBranchProperty.afterChange(newDisposable) { repaintSignal.tryEmit(Unit) }
                project.settingsLoadedProperty.afterChange(newDisposable) { repaintSignal.tryEmit(Unit) }
            }

            withContext(Dispatchers.EDT) {
                listModel.replaceAll(projects)
                projectList.showLoaded()
            }
        }
    }

    /**
     * Atomically replaces [projectsDisposable] with a fresh one, disposing the previous.
     * `@Synchronized` because [loadProjects]'s launch-and-cancel flow means the outgoing
     * reload's coroutine and the incoming one may overlap briefly at this point; the
     * lock ensures the swap is observed as a single step.
     */
    @Synchronized
    private fun swapProjectsDisposable(): Disposable {
        Disposer.dispose(projectsDisposable)
        val next = newProjectsDisposable()
        projectsDisposable = next
        return next
    }

    private fun newProjectsDisposable(): Disposable = Disposer.newDisposable(this, "SapCommerceProjectsDisposable")

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