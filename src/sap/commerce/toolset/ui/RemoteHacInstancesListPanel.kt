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
package sap.commerce.toolset.ui

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.RemoteConnectionService
import sap.commerce.toolset.exec.settings.state.RemoteConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.RemoteConnectionType
import sap.commerce.toolset.exec.ui.RemoteInstancesListPanel
import sap.commerce.toolset.toolwindow.RemoteHacConnectionDialog
import java.io.Serial

class RemoteHacInstancesListPanel(
    project: Project,
    private val onDataChanged: (EventType, Set<RemoteConnectionSettingsState>) -> Unit
) : RemoteInstancesListPanel(project, RemoteConnectionType.Hybris, HybrisIcons.Y.REMOTE) {

    override fun addItem() {
        val settings = RemoteConnectionService.getInstance(myProject).createDefaultRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val dialog = RemoteHacConnectionDialog(myProject, this, settings)
        if (dialog.showAndGet()) {
            addElement(settings)
        }
    }

    override fun onDataChanged(
        eventType: EventType,
        data: Set<RemoteConnectionSettingsState>
    ) = onDataChanged.invoke(eventType, data)

    override fun editSelectedItem(item: RemoteConnectionSettingsState) = if (RemoteHacConnectionDialog(myProject, this, item).showAndGet()) item
    else null

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}
