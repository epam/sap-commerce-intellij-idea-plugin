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

/**
 * Unit tests for [HacConnectionSettingsState.Mutable.immutable].
 *
 * Credentials of a connection are loaded lazily by the connection dialog, therefore an untouched `Mutable`
 * carries blank credentials which must never reach the credential store.
 */
class HacConnectionSettingsStateTest {

    private fun fixture(host: String = "localhost") = HacConnectionSettingsState(host = host).mutable()
//
//    @Test
//    fun immutable_notModified_hasNoCredentials() {
//        val (_, credentials) = fixture().immutable(it.mutation)
//
//        assertNull(credentials)
//    }
//
//    @Test
//    fun immutable_notModified_stillHasSettings() {
//        val (settings, _) = fixture(host = "hac.example.com").immutable(it.mutation)
//
//        assertEquals("hac.example.com", settings.host)
//    }
//
//    @Test
//    fun immutable_modified_hasCredentials() {
//        val fixture = fixture().apply {
//            credentials.username.set("admin")
//            credentials.password.set("nimda")
//            modified = true
//        }
//
//        val execCredentials = assertNotNull(fixture.immutable(it.mutation).execCredentials)
//        val credentials = execCredentials.credentials
//
//        assertEquals("admin", credentials.userName)
//        assertEquals("nimda", credentials.getPasswordAsString())
//    }
//
//    @Test
//    fun immutable_modified_hasProxyCredentials() {
//        val fixture = fixture().apply {
//            proxyCredentials.username.set("proxyUser")
//            proxyCredentials.password.set("proxyPass")
//            modified = true
//        }
//
//        val execCredentials = assertNotNull(fixture.immutable(it.mutation).execCredentials)
//        val proxyCredentials = assertNotNull(execCredentials.proxyCredentials)
//
//        assertEquals("proxyUser", proxyCredentials.userName)
//        assertEquals("proxyPass", proxyCredentials.getPasswordAsString())
//    }
//
//    @Test
//    fun immutable_modifiedWithBlankPassword_hasCredentials() {
//        val fixture = fixture().apply {
//            credentials.username.set("admin")
//            modified = true
//        }
//
//        val credentials = assertNotNull(fixture.immutable(it.mutation).execCredentials).credentials
//
//        assertEquals("admin", credentials.userName)
//        assertEquals("", credentials.getPasswordAsString())
//    }
}
