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

package sap.commerce.toolset.ccv2.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentStatus
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SettingsState
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab

abstract class CCv2ShowEnvironmentWithStatusAction(status: CCv2EnvironmentStatus) : CCv2ShowWithStatusAction<CCv2EnvironmentStatus>(
    CCv2Tab.ENVIRONMENTS,
    status,
    status.title,
    status.icon
) {

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val developerSettings = CCv2DeveloperSettings.getInstance(project)
        val mutable = developerSettings.ccv2Settings.mutable()
        if (state) mutable.showEnvironmentStatuses.add(status)
        else mutable.showEnvironmentStatuses.remove(status)

        developerSettings.ccv2Settings = mutable.immutable()
    }

    override fun getStatuses(settings: CCv2SettingsState) = settings.showEnvironmentStatuses
}

class CCv2ShowProvisioningEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.PROVISIONING)
class CCv2ShowAvailableEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.AVAILABLE)
class CCv2ShowTerminatingEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATING)
class CCv2ShowTerminatedEnvironmentsAction : CCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATED)