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

package sap.commerce.toolset.properties

/**
 * What the `configstore` accepts as a property key.
 *
 * Checked before a request is spent on a key the instance would reject, and by every field which lets a key be typed in.
 */
object CxPropertyKey {

    fun isValid(key: String) = validate(key) == null

    /** Why [key] would be rejected, or `null` when it is acceptable. */
    fun validate(key: String): String? = when {
        key.isBlank() -> "Property key is not allowed to be empty"
        key.any(Char::isWhitespace) -> "Property key cannot contain whitespace"
        else -> null
    }
}
