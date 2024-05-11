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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2BuildStatus
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentStatus
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Tag
import java.util.*

@Tag("HybrisDeveloperSpecificProjectSettings")
class DeveloperSettings : BaseState() {
    var activeRemoteConnectionID by string(null)
    var activeSolrConnectionID by string(null)
    var activeCCv2SubscriptionID by string(null)
    var remoteConnectionSettingsList by list<RemoteConnectionSettings>()
    var typeSystemDiagramSettings by property(TypeSystemDiagramSettings()) { false }
    var beanSystemSettings by property(BeanSystemSettings()) { false }
    var typeSystemSettings by property(TypeSystemSettings()) { false }
    var cngSettings by property(CngSettings()) { false }
    var bpSettings by property(BpSettings()) { false }
    var flexibleSearchSettings by property(FlexibleSearchSettings()) { false }
    var polyglotQuerySettings by property(PolyglotQuerySettings()) { false }
    var impexSettings by property(ImpexSettings()) { false }
    var groovySettings by property(GroovySettings()) { false }
    var ccv2Settings by property(CCv2Settings()) { false }
}

data class CCv2Settings(
    var showBuildStatuses: EnumSet<CCv2BuildStatus> = EnumSet.of(
        CCv2BuildStatus.BUILDING,
        CCv2BuildStatus.SUCCESS,
        CCv2BuildStatus.FAIL,
        CCv2BuildStatus.SCHEDULED,
        CCv2BuildStatus.UNKNOWN
    ),
    var showEnvironmentStatuses: EnumSet<CCv2EnvironmentStatus> = EnumSet.of(
        CCv2EnvironmentStatus.PROVISIONING,
        CCv2EnvironmentStatus.AVAILABLE,
    ),
)
