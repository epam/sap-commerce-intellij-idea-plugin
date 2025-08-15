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

package sap.commerce.toolset.ccv2.settings.state

import com.intellij.util.xmlb.annotations.Tag
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.ccv2.dto.CCv2BuildStatus
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentStatus
import java.util.*

@Tag("CCv2Settings")
data class CCv2SettingsState(
    @JvmField val showBuildStatuses: Set<CCv2BuildStatus> = EnumSet.of(
        CCv2BuildStatus.BUILDING,
        CCv2BuildStatus.SUCCESS,
        CCv2BuildStatus.FAIL,
        CCv2BuildStatus.SCHEDULED,
        CCv2BuildStatus.UNKNOWN
    ),
    @JvmField val showEnvironmentStatuses: Set<CCv2EnvironmentStatus> = EnumSet.of(
        CCv2EnvironmentStatus.PROVISIONING,
        CCv2EnvironmentStatus.AVAILABLE,
    ),
    // key = S-User uid
    @JvmField val sUsers: Map<String, SUser> = mapOf(),
) {
    fun mutable() = Mutable(
        showBuildStatuses = showBuildStatuses.toMutableSet(),
        showEnvironmentStatuses = showEnvironmentStatuses.toMutableSet(),
        sUsers = sUsers.toMutableMap(),
    )

    data class Mutable(
        var showBuildStatuses: MutableSet<CCv2BuildStatus>,
        var showEnvironmentStatuses: MutableSet<CCv2EnvironmentStatus>,
        var sUsers: MutableMap<String, SUser>,
    ) {
        fun immutable() = CCv2SettingsState(
            showBuildStatuses = showBuildStatuses.toImmutableSet(),
            showEnvironmentStatuses = showEnvironmentStatuses.toImmutableSet(),
            sUsers = sUsers.toImmutableMap(),
        )
    }
}