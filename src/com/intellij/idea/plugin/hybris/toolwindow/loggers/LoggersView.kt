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

package com.intellij.idea.plugin.hybris.toolwindow.loggers

import com.intellij.idea.plugin.hybris.actions.HybrisActionPlaces.LOGGERS_TOOLBAR
import com.intellij.idea.plugin.hybris.toolwindow.loggers.tree.LoggersTreePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import java.io.Serial

class LoggersView(
    val project: Project,
    coroutineScope: CoroutineScope
) : SimpleToolWindowPanel(false), Disposable {

    val treePane: LoggersTreePanel

    override fun dispose() = Unit

    init {
        installToolbar()
        treePane = LoggersTreePanel(project, coroutineScope)
        setContent(treePane)

        Disposer.register(this, treePane)
    }


    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            val actionManager = ActionManager.getInstance()

            add(actionManager.getAction("loggers.toolbar.actions"))

            actionManager.createActionToolbar(LOGGERS_TOOLBAR, this, false)
        }

        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7345745538412361349L
    }
}