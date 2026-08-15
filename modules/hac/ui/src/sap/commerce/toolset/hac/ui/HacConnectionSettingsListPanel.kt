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

package sap.commerce.toolset.hac.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.ui.list.AddEditDeleteList
import java.io.Serial
import javax.swing.event.ListDataEvent

class HacConnectionSettingsListPanel(
    private val project: Project,
    disposable: Disposable?,
    private val activeConnection: () -> HacConnectionSettingsState?,
    listener: (ListDataEvent) -> Unit
) : AddEditDeleteList<HacConnectionSettingsState.Mutable>(disposable, listener) {

    override fun getName(element: HacConnectionSettingsState.Mutable) = element.presentationName
    override fun getIcon(element: HacConnectionSettingsState.Mutable) = if (activeConnection()?.uuid == element.uuid) HybrisIcons.Y.REMOTE
    else HybrisIcons.Y.REMOTE_GREEN

    override fun newItem(element: HacConnectionSettingsState.Mutable?) = element
        ?.copy()
        ?: HacExecConnectionService.getInstance(project).default().mutable()

    override fun createDialog(item: HacConnectionSettingsState.Mutable) = HacConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = item,
        "Create SAP CX Connection Settings"
    )

    override fun editDialog(item: HacConnectionSettingsState.Mutable) = HacConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = item,
        "Edit SAP CX Connection Settings"
    )

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}