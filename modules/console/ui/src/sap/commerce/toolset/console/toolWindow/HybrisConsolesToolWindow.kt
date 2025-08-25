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

package sap.commerce.toolset.console.toolWindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBTabsPaneImpl
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.console.ConsoleUiConstants
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.console.HybrisConsoleProvider
import sap.commerce.toolset.exec.context.ExecContext
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.SwingConstants
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

@Service(Service.Level.PROJECT)
class HybrisConsolesToolWindow(project: Project, coroutineScope: CoroutineScope) : SimpleToolWindowPanel(true), Disposable {

    override fun dispose() {
        //NOP
    }

    private val actionToolbar: ActionToolbar
    private val tabsPanel = JBTabsPaneImpl(project, SwingConstants.TOP, this)

    // TODO: refresh on plugin reloads, f.e. Groovy
    private val consoles = HybrisConsoleProvider.EP.extensionList
        .mapNotNull { it.console(project, coroutineScope) }

    init {
        layout = BorderLayout()

        val actionManager = ActionManager.getInstance()
        val toolbarActions = actionManager.getAction("hybris.console.actionGroup") as ActionGroup
        actionToolbar = actionManager.createActionToolbar(ConsoleUiConstants.PLACE_TOOLBAR, toolbarActions, false)

        val rootPanel = JPanel(BorderLayout())

        consoles.forEachIndexed { index, console ->
            Disposer.register(this, console)
            tabsPanel.insertTab(console.title(), console.icon(), console.component, console.tip(), index)
        }

        tabsPanel.addChangeListener { event ->
            val console = event.source.asSafely<JBEditorTabs>()
                ?.selectedInfo
                ?.component
                ?.asSafely<HybrisConsole<in ExecContext>>()
                ?: return@addChangeListener


            console.onSelection()
        }

        actionToolbar.targetComponent = tabsPanel.component

        rootPanel.add(tabsPanel.component, BorderLayout.CENTER)
        rootPanel.add(actionToolbar.component, BorderLayout.WEST)

        add(rootPanel)
    }

    var activeConsole: HybrisConsole<out ExecContext>
        set(console) {
            tabsPanel.selectedIndex = consoles.indexOf(console)
        }
        get() = consoles[tabsPanel.selectedIndex]

    fun <C : HybrisConsole<out ExecContext>> findConsole(consoleClass: KClass<C>): C? = consoles
        .firstNotNullOfOrNull { consoleClass.safeCast(it) }

    companion object {
        @Serial
        private val serialVersionUID: Long = 5761094275961283320L

        const val ID = "Consoles"

        fun getInstance(project: Project): HybrisConsolesToolWindow = project.service()
    }
}