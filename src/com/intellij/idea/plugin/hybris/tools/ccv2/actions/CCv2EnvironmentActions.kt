/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.ccv2.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2Settings
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2Tab

class CCv2FetchEnvironmentsAction : AbstractCCv2FetchAction<CCv2Environment>(
    tab = CCv2Tab.ENVIRONMENTS,
    text = "Fetch Environments",
    icon = HybrisIcons.CCV2_FETCH,
    fetch = { project, subscriptions, onStartCallback, onCompleteCallback ->
        CCv2Service.getInstance(project).fetchEnvironments(subscriptions, onStartCallback, onCompleteCallback)
    }
)

abstract class AbstractCCv2ShowEnvironmentWithStatusAction(status: CCv2EnvironmentStatus) : AbstractCCv2ShowWithStatusAction<CCv2EnvironmentStatus>(
    CCv2Tab.ENVIRONMENTS,
    status,
    status.title,
    status.icon
) {

    override fun getStatuses(settings: CCv2Settings) = settings.showEnvironmentStatuses
}

class CCv2ShowProvisioningEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.PROVISIONING)
class CCv2ShowAvailableEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.AVAILABLE)
class CCv2ShowTerminatingEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATING)
class CCv2ShowTerminatedEnvironmentsAction : AbstractCCv2ShowEnvironmentWithStatusAction(CCv2EnvironmentStatus.TERMINATED)