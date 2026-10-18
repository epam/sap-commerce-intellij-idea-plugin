/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.exec

/**
 * Outcome of writing one property to a remote instance.
 *
 * Carries why a write failed as well as that it did: "Forbidden" and "the key has a space in it" are the same
 * non-event to a caller which only looks at a flag, and quite different things to whoever has to fix it.
 */
data class CxRemotePropertyWriteResult(
    val success: Boolean,
    val reason: String? = null,
) {

    companion object {
        val SUCCESS = CxRemotePropertyWriteResult(true)

        fun failed(reason: String?) = CxRemotePropertyWriteResult(false, reason)
    }
}
