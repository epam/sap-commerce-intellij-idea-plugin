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

package sap.commerce.toolset.hac.exec.settings.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [HacConnectionSettingsState.Mutable.immutable].
 *
 * Credentials of a connection are loaded lazily by the connection dialog, therefore an untouched `Mutable`
 * carries blank credentials which must never reach the credential store.
 */
class HacConnectionSettingsStateTest {

    private fun mutable(host: String = "localhost") = HacConnectionSettingsState(host = host).mutable()

    @Test
    fun immutable_notModified_hasNoCredentials() {
        val (_, credentials) = mutable().immutable()

        assertNull(credentials)
    }

    @Test
    fun immutable_notModified_stillHasSettings() {
        val (settings, _) = mutable(host = "hac.example.com").immutable()

        assertEquals("hac.example.com", settings.host)
    }

    @Test
    fun immutable_modified_hasCredentials() {
        val mutable = mutable().apply {
            username.set("admin")
            password.set("nimda")
            modified = true
        }

        val credentials = assertNotNull(mutable.immutable().second).credentials

        assertEquals("admin", credentials.userName)
        assertEquals("nimda", credentials.getPasswordAsString())
    }

    @Test
    fun immutable_modified_hasProxyCredentials() {
        val mutable = mutable().apply {
            proxyUsername.set("proxyUser")
            proxyPassword.set("proxyPass")
            modified = true
        }

        val proxyCredentials = assertNotNull(assertNotNull(mutable.immutable().second).proxyCredentials)

        assertEquals("proxyUser", proxyCredentials.userName)
        assertEquals("proxyPass", proxyCredentials.getPasswordAsString())
    }

    @Test
    fun immutable_modifiedWithBlankPassword_hasCredentials() {
        val mutable = mutable().apply {
            username.set("admin")
            modified = true
        }

        val credentials = assertNotNull(mutable.immutable().second).credentials

        assertEquals("admin", credentials.userName)
        assertEquals("", credentials.getPasswordAsString())
    }
}
