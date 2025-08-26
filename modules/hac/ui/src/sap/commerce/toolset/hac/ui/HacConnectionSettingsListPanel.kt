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

package sap.commerce.toolset.hac.ui

import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.RemoteInstancesListPanel
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.io.Serial

class HacConnectionSettingsListPanel(
    project: Project,
    private val onDataChanged: (EventType, Set<HacConnectionSettingsState>) -> Unit
) : RemoteInstancesListPanel<HacConnectionSettingsState>(project, HybrisIcons.Y.REMOTE) {

    override fun addItem() {
        val settings = HacExecConnectionService.getInstance(myProject).default()
        val mutableSettings = settings.mutable()
        val dialog = HacConnectionSettingsDialog(myProject, this, mutableSettings)
        if (dialog.showAndGet()) {
            addElement(mutableSettings.immutable())
        }
    }

    override fun onDataChanged(
        eventType: EventType,
        data: Set<HacConnectionSettingsState>
    ) = onDataChanged.invoke(eventType, data)

    override fun editSelectedItem(item: HacConnectionSettingsState): HacConnectionSettingsState? {
        val mutableSettings = item.mutable()
        return if (HacConnectionSettingsDialog(myProject, this, mutableSettings).showAndGet()) mutableSettings.immutable()
        else null
    }

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}