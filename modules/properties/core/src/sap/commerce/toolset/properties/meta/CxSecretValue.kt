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

package sap.commerce.toolset.properties.meta

/**
 * Whether the value written against a secret-sounding key is actually a secret.
 *
 * Most declarations of a password in a committed file are not leaks: the key is there to document that a password is
 * needed, with nothing behind it, or the value has been externalised into a placeholder. Reporting those would make
 * the real ones harder to see.
 */
object CxSecretValue {

    /** Values which say "a secret goes here" rather than being one. */
    private val PLACEHOLDERS = setOf(
        "changeme", "change-me", "change_me", "changeit", "tochange",
        "password", "passwd", "secret", "token", "apikey",
        "todo", "tbd", "none", "null", "empty", "dummy", "sample", "example",
        "xxx", "xxxx", "xxxxx", "yyy", "zzz", "***", "...",
    )

    fun isSecret(value: String): Boolean {
        val trimmed = value.trim()

        return when {
            trimmed.isEmpty() -> false
            // Already externalised - the whole point of a placeholder is that the value lives somewhere else.
            CxPropertyPlaceholder.of(trimmed).any { it.range.first == 0 && it.range.last == trimmed.lastIndex } -> false
            trimmed.lowercase() in PLACEHOLDERS -> false
            trimmed.startsWith("<") && trimmed.endsWith(">") -> false

            else -> true
        }
    }
}
