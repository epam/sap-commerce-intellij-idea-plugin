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
package com.intellij.idea.plugin.hybris.tools.ccv2.settings.state

import com.intellij.openapi.components.BaseState
import java.util.*

class CCv2Subscription : BaseState(), Comparable<CCv2Subscription> {
    var uuid by string(UUID.randomUUID().toString())
    var id by string()
    var name by string(null)

    override fun compareTo(other: CCv2Subscription) = toString().compareTo(other.toString())

    override fun toString() = name
        ?: id
        ?: "?"

    fun toDto() = CCv2SubscriptionDto(uuid ?: UUID.randomUUID().toString(), id, name)
}

data class CCv2SubscriptionDto(
    var uuid: String = UUID.randomUUID().toString(),
    var id: String? = null,
    var name: String? = null,
    var ccv2Token: String? = null,
) {
    fun toModel() = CCv2Subscription()
        .also {
            it.uuid = uuid
            it.id = id
            it.name = name
        }

    fun copy() = CCv2SubscriptionDto(uuid, id, name, ccv2Token)

    override fun toString() = name
        ?: id
        ?: "?"
}