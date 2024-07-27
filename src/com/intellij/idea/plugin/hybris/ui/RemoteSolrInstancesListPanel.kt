/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.toolwindow.RemoteSolrConnectionDialog
import com.intellij.openapi.project.Project
import java.io.Serial

class RemoteSolrInstancesListPanel(
    project: Project,
    private val onDataChanged: (EventType, Set<RemoteConnectionSettings>) -> Unit = { _, _ -> }
) : RemoteInstancesListPanel(project, RemoteConnectionType.SOLR, HybrisIcons.Console.SOLR) {

    override fun editSelectedItem(item: RemoteConnectionSettings): RemoteConnectionSettings? {
        val ok = RemoteSolrConnectionDialog(myProject, this, item).showAndGet()
        return if (ok) item
        else null
    }

    public override fun addItem() {
        val item = RemoteConnectionUtil.createDefaultRemoteConnectionSettings(myProject, RemoteConnectionType.SOLR)
        val dialog = RemoteSolrConnectionDialog(myProject, this, item)
        if (dialog.showAndGet()) {
            addElement(item)
        }
    }

    override fun onDataChanged(
        eventType: EventType,
        data: Set<RemoteConnectionSettings>
    ) = onDataChanged.invoke(eventType, data)

    companion object {
        @Serial
        private val serialVersionUID = -6666004870055817895L
    }
}
