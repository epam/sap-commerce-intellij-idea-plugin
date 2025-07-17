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
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.SwingConstants.TOP
import kotlin.reflect.KClass
import kotlin.reflect.cast

class HybrisConsolesView(val project: Project) : SimpleToolWindowPanel(true), Disposable {

    override fun dispose() {
        //NOP
    }

    private val actionToolbar: ActionToolbar
    private val hybrisTabs: HybrisConsoleTabs

    init {
        layout = BorderLayout()

        val actionManager = ActionManager.getInstance()
        val toolbarActions = actionManager.getAction("hybris.console.actionGroup") as ActionGroup
        actionToolbar = actionManager.createActionToolbar(HybrisActionPlaces.CONSOLE_TOOLBAR, toolbarActions, false)

        val panel = JPanel(BorderLayout())

        val consoles = arrayOf(
            project.service<HybrisImpexConsole>(),
            project.service<HybrisGroovyConsole>(),
            project.service<HybrisFlexibleSearchConsole>(),
            project.service<HybrisPolyglotQueryConsole>(),
            project.service<HybrisSolrSearchConsole>(),
            project.service<HybrisImpexMonitorConsole>(),
            project.service<HybrisSQLConsole>()
        )
        consoles.forEach { Disposer.register(this, it) }

        hybrisTabs = HybrisConsoleTabs(project, TOP, consoles, this)
        actionToolbar.targetComponent = hybrisTabs.component

        panel.add(hybrisTabs.component, BorderLayout.CENTER)
        panel.add(actionToolbar.component, BorderLayout.WEST)

        add(panel)
    }

    fun setActiveConsole(console: HybrisConsole<out ExecutionContext>) {
        hybrisTabs.setActiveConsole(console)
    }

    fun getActiveConsole(): HybrisConsole<out ExecutionContext> {
        return hybrisTabs.activeConsole()
    }

    fun <C : HybrisConsole<out ExecutionContext>> findConsole(consoleClass: KClass<C>): C? {
        for (index in 0 until hybrisTabs.tabCount) {
            val c = hybrisTabs.getComponentAt(index)

            if (consoleClass.isInstance(c)) return consoleClass.cast(c)
        }
        return null
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5761094275961283320L
    }
}

