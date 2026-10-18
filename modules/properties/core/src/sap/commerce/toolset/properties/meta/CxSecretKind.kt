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
 * Kinds of property whose value is a secret, recognised by what the key is called.
 *
 * Naming is all there is to go on: a value is just a string, and nothing about `hunter2` says whether it opens a
 * payment gateway or is somebody's example. The last segment of the key is what developers use to say what a property
 * holds, so that is what this reads.
 *
 * Ambiguous words are deliberately left out. A bare `key` is as often an identifier or a file name as it is a
 * credential - `license.key`, `cache.key` - and an inspection which cries wolf gets switched off.
 */
enum class CxSecretKind(val title: String, private val segments: Set<String>) {

    PASSWORD("a password", setOf("password", "passwd", "pwd")),
    SECRET("a secret", setOf("secret", "clientsecret")),
    TOKEN("a token", setOf("token", "accesstoken", "apitoken", "refreshtoken")),
    API_KEY("an API key", setOf("apikey", "accesskey", "secretkey", "privatekey", "signingkey", "encryptionkey")),
    CREDENTIAL("a credential", setOf("credential", "credentials")),
    CONNECTION_STRING("a connection string", setOf("connectionstring")),
    ;

    fun matches(key: String) = lastSegmentOf(key) in segments

    companion object {
        private val SEPARATORS = charArrayOf('.', '-', '_')

        fun of(key: String) = entries.find { it.matches(key) }

        private fun lastSegmentOf(key: String) = key
            .trim()
            .lowercase()
            .split(*SEPARATORS)
            .lastOrNull()
            .orEmpty()
    }
}
