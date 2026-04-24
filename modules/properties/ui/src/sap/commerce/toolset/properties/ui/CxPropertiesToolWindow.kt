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

package sap.commerce.toolset.properties.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import sap.commerce.toolset.ui.toolwindow.CxToolWindow
import java.io.Serial

class CxPropertiesToolWindow(project: Project, parentDisposable: Disposable) : CxToolWindow() {
    private val splitView = CxPropertiesSplitView(project)

    init {
        installToolbar()
        setContent(splitView)

        Disposer.register(parentDisposable, this)
        Disposer.register(this, splitView)
    }

    override fun onActivated() = splitView.onActivated()
    override fun dispose() = Unit

    private fun installToolbar() {
        val toolbar = with(DefaultActionGroup()) {
            add(ActionManager.getInstance().getAction("sap.cx.properties.toolbar.actions"))
            ActionManager.getInstance().createActionToolbar("Sap.Cx.PropertiesToolbar", this, false)
        }

        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 753769362491852936L
    }
}
