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

package com.intellij.idea.plugin.hybris.tools.remote.http

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.idea.plugin.hybris.tools.remote.ReplicaType

data class Replica(
    val type: ReplicaType,
    val id: String,
    val subscription: CCv2Subscription? = null,
    val environment: CCv2EnvironmentDto? = null,
    val service: CCv2ServiceDto? = null,
    val replica: CCv2ServiceReplicaDto? = null,
    val cookieName: String = "ROUTE"
) {
    override fun toString() = when (type) {
        ReplicaType.AUTO -> "Auto-discover replica"
        ReplicaType.MANUAL -> id
        ReplicaType.CCV2 -> id
    }

    val description
        get() = when (type) {
            ReplicaType.CCV2 -> listOfNotNull(
                "- CCv2 specific replica -",
                "",
                subscription?.name?.let { "Subscription: $it" },
                environment?.name?.let { "Environment: $it" },
                service?.name?.let { "Service: $it" },
            ).joinToString("\n")

            ReplicaType.MANUAL -> listOfNotNull(
                "- Manually configured replica -",
                "",
                "Cookie: $cookieName"
            ).joinToString("\n")

            else -> null
        }
}
