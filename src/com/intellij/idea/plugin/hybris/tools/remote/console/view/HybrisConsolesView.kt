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
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.HybrisClearAllAction
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.HybrisExecuteImmediatelyAction
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.HybrisImpexValidateAction
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.HybrisSuspendAction
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.HybrisConsoleExecuteActionHandler
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.HybrisConsoleExecuteValidateActionHandler
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.SwingConstants.TOP

class HybrisConsolesView(val project: Project) : SimpleToolWindowPanel(true), Disposable {

    override fun dispose() {
        //NOP
    }

    private val impexConsole = HybrisImpexConsole(project)
    private val groovyConsole = HybrisGroovyConsole(project)
    private val monitorConsole = HybrisImpexMonitorConsole(project)
    private val flexibleSearchConsole = HybrisFlexibleSearchConsole(project)
    private val polyglotQueryConsole = HybrisPolyglotQueryConsole(project)
    private val solrSearchConsole = HybrisSolrSearchConsole(project)

    private val actionToolbar: ActionToolbar
    private val hybrisTabs: HybrisConsoleTabs

    init {
        layout = BorderLayout()

        val toolbarActions = DefaultActionGroup()
        val actionManager = ActionManager.getInstance()
        actionToolbar = actionManager.createActionToolbar(HybrisActionPlaces.CONSOLE_TOOLBAR, toolbarActions, false)

        val panel = JPanel(BorderLayout())

        val consoles = arrayOf(flexibleSearchConsole, impexConsole, groovyConsole, polyglotQueryConsole, monitorConsole, solrSearchConsole)
        consoles.forEach { Disposer.register(this, it) }
        hybrisTabs = HybrisConsoleTabs(project, TOP, consoles, this)

        panel.add(hybrisTabs.component, BorderLayout.CENTER)
        actionToolbar.targetComponent = hybrisTabs.component
        panel.add(actionToolbar.component, BorderLayout.WEST)

        val actionHandler = HybrisConsoleExecuteActionHandler(project, false)
        val validateHandler = HybrisConsoleExecuteValidateActionHandler(project, false)
        val executeAction = HybrisExecuteImmediatelyAction(actionHandler)
        executeAction.registerCustomShortcutSet(CommonShortcuts.ALT_ENTER, this.component)

        with(toolbarActions) {
            add(ActionManager.getInstance().getAction("hybris.hac.chooseConnection"))
            add(executeAction)
            add(HybrisSuspendAction(actionHandler))
            add(HybrisImpexValidateAction(validateHandler))
        }

        val actions = impexConsole.createConsoleActions()
        actions[5] = HybrisClearAllAction()
        toolbarActions.addAll(*actions)
        add(panel)
    }

    fun setActiveConsole(console: HybrisConsole) {
        hybrisTabs.setActiveConsole(console)
    }

    fun getActiveConsole(): HybrisConsole {
        return hybrisTabs.activeConsole()
    }

    fun findConsole(consoleTitle: String): HybrisConsole? {
        for (index in 0 until hybrisTabs.tabCount) {
            val component = hybrisTabs.getComponentAt(index) as HybrisConsole
            if (component.title == consoleTitle) {
                return component
            }
        }
        return null
    }

    fun validateImpex() = performAction(HybrisImpexValidateAction::class.java)
    fun execute() = performAction(HybrisExecuteImmediatelyAction::class.java)

    private fun performAction(clazz: Class<out AnAction>) {
        val action = actionToolbar.actions.firstOrNull { clazz.isInstance(it) } ?: return
        val event = AnActionEvent.createEvent(actionToolbar.toolbarDataContext, action.templatePresentation, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null)
        ActionUtil.performActionDumbAwareWithCallbacks(action, event)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5761094275961283320L
    }
}

