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

package sap.commerce.toolset.logging.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.actionSystem.HybrisActionPlaces
import sap.commerce.toolset.ui.toolwindow.ContentActivationAware
import java.io.Serial

class LoggersView(
    val project: Project,
    coroutineScope: CoroutineScope
) : SimpleToolWindowPanel(false), ContentActivationAware, Disposable {

    var activated = false;
    val treePane: LoggersSplitView

    override fun dispose() = Unit

    init {
        installToolbar()
        treePane = LoggersSplitView(project, coroutineScope)
        setContent(treePane)
        //todo add a listener for project import completion event

        Disposer.register(this, treePane)
    }

    override fun onActivated() {
        activated = true
        //todo refresh tree only if project import has been completed
        treePane.updateTree()
    }

    override fun onDeactivated() {
        activated = false
    }

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(actionManager.getAction("sap.cx.loggers.toolbar.actions"))

            actionManager.createActionToolbar(HybrisActionPlaces.LOGGERS_TOOLBAR, this, false)
        }

        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7345745538412361349L

        const val ID = "Loggers"
    }
}