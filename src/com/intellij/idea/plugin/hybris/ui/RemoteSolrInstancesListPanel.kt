/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.toolwindow.RemoteSolrConnectionDialog
import com.intellij.openapi.project.Project
import java.io.Serial

class RemoteSolrInstancesListPanel(project: Project) : RemoteInstancesListPanel(project) {

    override fun editSelectedItem(item: HybrisRemoteConnectionSettings): HybrisRemoteConnectionSettings? {
        val ok = RemoteSolrConnectionDialog(myProject, this, item).showAndGet()
        return if (ok) item
        else null
    }

    public override fun getCellIcon() = HybrisIcons.CONSOLE_SOLR

    public override fun saveSettings() {
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).saveRemoteConnectionSettingsList(HybrisRemoteConnectionSettings.Type.SOLR, getData())
    }

    public override fun addItem() {
        val item = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).getDefaultSolrRemoteConnectionSettings(myProject)
        val dialog = RemoteSolrConnectionDialog(myProject, this, item)
        if (dialog.showAndGet()) {
            addElement(item)
        }
    }

    companion object {
        @Serial
        private val serialVersionUID = -6666004870055817895L
    }
}
