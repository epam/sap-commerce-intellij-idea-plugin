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
import com.intellij.idea.plugin.hybris.toolwindow.RemoteHacConnectionDialog
import com.intellij.openapi.project.Project
import java.io.Serial

class RemoteHacInstancesListPanel(project: Project) : RemoteInstancesListPanel(project) {

    public override fun getCellIcon() = HybrisIcons.Y_REMOTE

    public override fun saveSettings() {
        HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).saveRemoteConnectionSettingsList(HybrisRemoteConnectionSettings.Type.Hybris, getData())
    }

    public override fun addItem() {
        val item = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(myProject).getDefaultHacRemoteConnectionSettings(myProject)
        val dialog = RemoteHacConnectionDialog(myProject, this, item)
        if (dialog.showAndGet()) {
            addElement(item)
        }
    }

    override fun editSelectedItem(item: HybrisRemoteConnectionSettings): HybrisRemoteConnectionSettings? {
        val ok = RemoteHacConnectionDialog(myProject, this, item).showAndGet()
        return if (ok) item
        else null
    }

    companion object {
        @Serial
        private val serialVersionUID = -4192832265110127713L
    }
}
