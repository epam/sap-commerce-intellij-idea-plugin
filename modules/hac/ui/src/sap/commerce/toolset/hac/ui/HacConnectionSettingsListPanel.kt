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

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.ConnectionsListPanel
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import java.io.Serial
import javax.swing.event.ListDataEvent

class HacConnectionSettingsListPanel(
    project: Project,
    disposable: Disposable?,
    listener: (ListDataEvent) -> Unit
) : ConnectionsListPanel<HacConnectionSettingsState.Mutable>(project, disposable, listener) {

    override fun getIcon(item: HacConnectionSettingsState.Mutable) = HybrisIcons.Y.REMOTE
    override fun newMutable() = HacExecConnectionService.getInstance(project).default().mutable()

    override fun createDialog(mutable: HacConnectionSettingsState.Mutable) = HacConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = mutable,
        "Create SAP CX Connection Settings"
    )

    override fun editDialog(mutable: HacConnectionSettingsState.Mutable) = HacConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = mutable,
        "Edit SAP CX Connection Settings"
    )

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}