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

package com.intellij.idea.plugin.hybris.startup

import com.intellij.ide.plugins.StandalonePluginUpdateChecker
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class HybrisStandalonePluginUpdateChecker : StandalonePluginUpdateChecker(
    PluginId.getId(HybrisConstants.PLUGIN_ID),
    HybrisConstants.UPDATE_TIMESTAMP_PROPERTY,
    NotificationGroupManager.getInstance().getNotificationGroup(HybrisConstants.NOTIFICATION_GROUP_HYBRIS),
    HybrisIcons.Y_LOGO_BLUE
) {

    companion object {
        fun getInstance(project: Project): HybrisStandalonePluginUpdateChecker = project.getService(HybrisStandalonePluginUpdateChecker::class.java)
    }
}