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

package sap.commerce.toolset.logging.ui.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxRemoteLogAccess

class CxRemoteLogStateNode(
    val connection: HacConnectionSettingsState,
    project: Project,
) : CxLoggersNode(project) {

    override fun getName() = connection.connectionName

    override fun update(presentation: PresentationData) {
        if (myProject == null || myProject.isDisposed) return
        val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
        val active = activeConnection == connection

        val connectionIcon = if (active) HybrisIcons.Y.REMOTE else HybrisIcons.Y.REMOTE_GREEN

        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (active) {
            val tip = CxRemoteLogAccess.getInstance(project).state(connection.uuid)
                .get()
                ?.values
                ?.count { !it.inherited }
                ?.let { size -> " active | $size logger(s)" }
                ?: " (active)"
            presentation.addText(ColoredFragment(tip, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES))
        }
        presentation.setIcon(connectionIcon)
    }
}
