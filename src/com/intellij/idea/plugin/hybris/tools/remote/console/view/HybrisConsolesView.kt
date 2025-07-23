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

package com.intellij.idea.plugin.hybris.tools.remote.console.view

import com.intellij.idea.plugin.hybris.actions.HybrisActionPlaces
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.*
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBTabsPaneImpl
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.intellij.util.asSafely
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.SwingConstants
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class HybrisConsolesView(val project: Project) : SimpleToolWindowPanel(true), Disposable {

    override fun dispose() {
        //NOP
    }

    private val actionToolbar: ActionToolbar
    private val tabsPanel = JBTabsPaneImpl(project, SwingConstants.TOP, this)
    private val consoles = arrayOf(
        HybrisImpexConsole.getInstance(project),
        HybrisGroovyConsole.getInstance(project),
        HybrisFlexibleSearchConsole.getInstance(project),
        HybrisPolyglotQueryConsole.getInstance(project),
        HybrisSQLConsole.getInstance(project),
        HybrisSolrSearchConsole.getInstance(project),
        HybrisImpexMonitorConsole.getInstance(project)
    )

    init {
        layout = BorderLayout()

        val actionManager = ActionManager.getInstance()
        val toolbarActions = actionManager.getAction("hybris.console.actionGroup") as ActionGroup
        actionToolbar = actionManager.createActionToolbar(HybrisActionPlaces.CONSOLE_TOOLBAR, toolbarActions, false)

        val rootPanel = JPanel(BorderLayout())

        consoles.forEachIndexed { index, console ->
            Disposer.register(this, console)
            tabsPanel.insertTab(console.title(), console.icon(), console.component, console.tip(), index)
        }

        tabsPanel.addChangeListener { event ->
            val console = event.source.asSafely<JBEditorTabs>()
                ?.selectedInfo
                ?.component
                ?.asSafely<HybrisConsole<in ExecutionContext>>()
                ?: return@addChangeListener


            console.onSelection()
        }

        actionToolbar.targetComponent = tabsPanel.component

        rootPanel.add(tabsPanel.component, BorderLayout.CENTER)
        rootPanel.add(actionToolbar.component, BorderLayout.WEST)

        add(rootPanel)
    }

    var activeConsole: HybrisConsole<out ExecutionContext>
        set(console) {
            tabsPanel.selectedIndex = consoles.indexOf(console)
        }
        get() = consoles[tabsPanel.selectedIndex]

    fun <C : HybrisConsole<out ExecutionContext>> findConsole(consoleClass: KClass<C>): C? = consoles
        .firstNotNullOfOrNull { consoleClass.safeCast(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5761094275961283320L
    }
}

