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

import com.intellij.credentialStore.Credentials
import sap.commerce.toolset.settings.state.Mutation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for [HacConnectionSettingsState.Mutable.snapshot].
 *
 * Credentials of a connection are loaded lazily by the connection dialog, therefore an untouched `Mutable`
 * carries blank credentials which must never reach the credential store.
 */
class HacConnectionSettingsStateTest {

    private fun fixture(host: String = "localhost") = HacConnectionSettingsState(host = host).mutable()

    @Test
    fun immutable_notModified_hasNoCredentials() {
        val fixture = fixture().snapshot()

        assertEquals(Mutation.NONE, fixture.mutation)
        assertEquals(Mutation.NONE, fixture.credentials.mutation)
    }

    @Test
    fun immutable_notModified_stillHasSettings() {
        val fixture = fixture(host = "hac.example.com").snapshot()

        assertEquals("hac.example.com", fixture.state.host)
    }

    @Test
    fun immutable_modified_hasCredentials() {
        val fixture = fixture().apply {
            credentials.apply("admin", "nimda")
        }

        val execCredentials = assertNotNull(fixture.snapshot().credentials)
        val credentials = execCredentials.credentials

        assertEquals(Mutation.SAVE, execCredentials.mutation)
        assertEquals("admin", credentials.userName)
        assertEquals("nimda", credentials.getPasswordAsString())
    }

    @Test
    fun immutable_modified_hasProxyCredentials() {
        val fixture = fixture().apply {
            proxyCredentials.apply("proxyUser", "proxyPass")
        }

        val execCredentials = assertNotNull(fixture.snapshot().proxyCredentials)
        val credentials = execCredentials.credentials

        assertEquals(Mutation.SAVE, execCredentials.mutation)
        assertEquals("proxyUser", credentials.userName)
        assertEquals("proxyPass", credentials.getPasswordAsString())
    }

    @Test
    fun immutable_modifiedWithBlankPassword_hasCredentials() {
        val fixture = fixture().apply {
            credentials.username.set("admin")
        }

        val execCredentials = assertNotNull(fixture.snapshot().credentials)
        val credentials = execCredentials.credentials

        assertEquals(Mutation.NONE, execCredentials.mutation)
        assertEquals("admin", credentials.userName)
        assertEquals("", credentials.getPasswordAsString())
    }

    @Test
    fun `save only modified credentials`() {
        val fixture = fixture().apply {
            credentials.load(Credentials("admin", "nimda"))
        }

        val execCredentials = assertNotNull(fixture.snapshot().credentials)
        val credentials = execCredentials.credentials

        assertEquals(Mutation.NONE, execCredentials.mutation)
        assertEquals("admin", credentials.userName)
        assertEquals("nimda", credentials.getPasswordAsString())

        fixture.credentials.apply("newAdmin", "nimda")

        val modifiedExecCredentials = assertNotNull(fixture.snapshot().credentials)
        val modifiedCredentials = modifiedExecCredentials.credentials

        assertEquals(Mutation.SAVE, modifiedExecCredentials.mutation)
        assertEquals("newAdmin", modifiedCredentials.userName)
        assertEquals("nimda", modifiedCredentials.getPasswordAsString())
    }
}
