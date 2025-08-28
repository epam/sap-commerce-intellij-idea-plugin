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
import sap.commerce.toolset.exec.settings.state.connectionName
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxLoggerAccess

class LoggersHacConnectionNode(
    val connectionSettings: HacConnectionSettingsState,
    project: Project
) : LoggersNode(project) {

    val activeConnection: Boolean
        get() = connectionSettings.uuid == HacExecConnectionService.getInstance(project).activeConnection.uuid

    override fun getName() = connectionSettings.connectionName

    override fun update(presentation: PresentationData) {
        if (myProject == null || myProject.isDisposed) return

        val connectionIcon = if (activeConnection) HybrisIcons.Y.REMOTE else HybrisIcons.Y.REMOTE_GREEN

        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        if (activeConnection) {
            val tip = CxLoggerAccess.getInstance(project).state(connectionSettings)
                .get()
                ?.size
                ?.let { size -> " active | $size logger(s)"}
                ?: " (active)"
            presentation.addText(ColoredFragment(tip, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES))
        }
        presentation.setIcon(connectionIcon)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoggersHacConnectionNode

        if (activeConnection != other.activeConnection) return false
        if (connectionSettings != other.connectionSettings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activeConnection.hashCode()
        result = 31 * result + connectionSettings.hashCode()
        return result
    }


}