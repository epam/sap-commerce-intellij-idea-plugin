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

package sap.commerce.toolset.solr.ui

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.RemoteInstancesListPanel
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import java.io.Serial

class SolrConnectionSettingsListPanel(
    project: Project,
    private val onDataChanged: (EventType, Set<SolrConnectionSettingsState>) -> Unit = { _, _ -> }
) : RemoteInstancesListPanel<SolrConnectionSettingsState>(project, HybrisIcons.Console.SOLR) {

    override fun editSelectedItem(item: SolrConnectionSettingsState): SolrConnectionSettingsState? {
        val mutableSettings = item.mutable()
        return if (SolrConnectionSettingsDialog(myProject, this, mutableSettings).showAndGet()) mutableSettings.immutable()
        else null
    }

    override fun addItem() {
        val mutableSettings = SolrExecConnectionService.getInstance(myProject).default().mutable()
        if (SolrConnectionSettingsDialog(myProject, this, mutableSettings).showAndGet()) {
            addElement(mutableSettings.immutable())
        }
    }

    override fun onDataChanged(
        eventType: EventType,
        data: Set<SolrConnectionSettingsState>
    ) = onDataChanged.invoke(eventType, data)

    companion object {
        @Serial
        private val serialVersionUID = -6666004870055817895L
    }
}