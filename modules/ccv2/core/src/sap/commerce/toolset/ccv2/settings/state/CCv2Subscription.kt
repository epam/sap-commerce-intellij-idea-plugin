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

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import java.util.*

data class CCv2Subscription(
    @JvmField @OptionTag val uuid: String = UUID.randomUUID().toString(),
    @JvmField @OptionTag val id: String? = null,
    @JvmField @OptionTag val name: String? = null,
) : Comparable<CCv2Subscription> {

    fun mutable() = Mutable(
        uuid,
        id,
        name
    )

    val presentableName
        @Transient
        get() = name
            ?.takeIf { it.isNotEmpty() }
            ?: id ?: "?"

    override fun compareTo(other: CCv2Subscription) = presentableName.compareTo(other.presentableName)

    data class Mutable(
        var uuid: String,
        var id: String?,
        var name: String?,
        var ccv2Token: String? = null,
    ) {
        fun immutable() = CCv2Subscription(
            uuid = uuid,
            id = id,
            name = name,
        )

        val presentableName
            @Transient
            get() = name
                ?.takeIf { it.isNotEmpty() }
                ?: id ?: "?"
    }
}