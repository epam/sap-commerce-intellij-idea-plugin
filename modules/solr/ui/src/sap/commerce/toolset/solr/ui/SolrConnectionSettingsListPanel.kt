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

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.ui.ConnectionsListPanel
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import java.io.Serial
import javax.swing.event.ListDataEvent

class SolrConnectionSettingsListPanel(
    project: Project,
    disposable: Disposable?,
    listener: (ListDataEvent) -> Unit
) : ConnectionsListPanel<SolrConnectionSettingsState.Mutable>(project, disposable, listener) {

    override fun getIcon(item: SolrConnectionSettingsState.Mutable) = HybrisIcons.Console.SOLR
    override fun newMutable() = SolrExecConnectionService.getInstance(project).default().mutable()

    override fun createDialog(mutable: SolrConnectionSettingsState.Mutable) = SolrConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = mutable,
        "Create Solr Connection Settings"
    )

    override fun editDialog(mutable: SolrConnectionSettingsState.Mutable) = SolrConnectionSettingsDialog(
        project = project,
        parentComponent = this,
        settings = mutable,
        "Edit Solr Connection Settings"
    )

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}