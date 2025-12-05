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

package sap.commerce.toolset.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import javax.swing.Icon
import javax.swing.JComponent

abstract class CxToolWindowContentProvider(
    private val displayName: String,
    private val icon: Icon,
    val order: Int,
) {

    protected abstract fun createComponent(project: Project, parentDisposable: Disposable): JComponent

    fun create(project: Project, toolWindow: ToolWindow) = toolWindow.contentManager.factory.createContent(
        createComponent(project, toolWindow.disposable),
        displayName,
        true
    ).also {
        it.isCloseable = false
        it.icon = icon
        it.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    }

    companion object {
        val EP = ExtensionPointName.create<CxToolWindowContentProvider>("sap.commerce.toolset.ui.toolWindowProvider")
    }
}
