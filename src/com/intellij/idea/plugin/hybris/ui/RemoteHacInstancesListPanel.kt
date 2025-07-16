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
package com.intellij.idea.plugin.hybris.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.toolwindow.RemoteHacConnectionDialog
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.io.Serial

class RemoteHacInstancesListPanel(
    project: Project,
    private val onDataChanged: (EventType, Set<RemoteConnectionSettings>) -> Unit
) : RemoteInstancesListPanel(project, RemoteConnectionType.Hybris, HybrisIcons.Y.REMOTE) {

    public override fun addItem() {
        val settings = myProject.service<RemoteConnectionService>().createDefaultRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val dialog = RemoteHacConnectionDialog(myProject, this, settings)
        if (dialog.showAndGet()) {
            addElement(settings)
        }
    }

    override fun onDataChanged(
        eventType: EventType,
        data: Set<RemoteConnectionSettings>
    ) = onDataChanged.invoke(eventType, data)

    override fun editSelectedItem(item: RemoteConnectionSettings) = if (RemoteHacConnectionDialog(myProject, this, item).showAndGet()) item
    else null

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}
