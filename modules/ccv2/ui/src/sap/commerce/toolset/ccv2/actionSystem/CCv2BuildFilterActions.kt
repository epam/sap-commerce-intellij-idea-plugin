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
import sap.commerce.toolset.ccv2.dto.CCv2BuildStatus
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2SettingsState
import sap.commerce.toolset.ccv2.toolwindow.CCv2Tab

abstract class CCv2ShowBuildWithStatusAction(status: CCv2BuildStatus) : CCv2ShowWithStatusAction<CCv2BuildStatus>(
    CCv2Tab.BUILDS,
    status,
    status.title,
    status.icon
) {

    override fun getStatuses(settings: CCv2SettingsState) = settings.showBuildStatuses

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        val ccv2DeveloperSettings = CCv2DeveloperSettings.getInstance(project)
        val mutable = ccv2DeveloperSettings.ccv2Settings.mutable()
        if (state) mutable.showBuildStatuses.add(status)
        else mutable.showBuildStatuses.remove(status)

        ccv2DeveloperSettings.ccv2Settings = mutable.immutable()
    }
}

class CCv2ShowDeletedBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.DELETED)
class CCv2ShowFailedBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.FAIL)
class CCv2ShowUnknownBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.UNKNOWN)
class CCv2ShowScheduledBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.SCHEDULED)
class CCv2ShowBuildingBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.BUILDING)
class CCv2ShowSuccessBuildsAction : CCv2ShowBuildWithStatusAction(CCv2BuildStatus.SUCCESS)
