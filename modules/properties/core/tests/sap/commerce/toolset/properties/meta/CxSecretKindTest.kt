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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CxSecretKindTest {

    @Test
    fun `a key ending in a secret word separated by dots is recognised`() {
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore.smtp.password"))
    }

    @Test
    fun `a key ending in a secret word separated by dashes is recognised`() {
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore-smtp-password"))
    }

    @Test
    fun `a key ending in a secret word separated by underscores is recognised`() {
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore_smtp_password"))
    }

    @Test
    fun `an API key is recognised whatever its case`() {
        assertEquals(CxSecretKind.API_KEY, CxSecretKind.of("payment.gateway.apiKey"))
    }

    @Test
    fun `a client secret is recognised whatever its case`() {
        assertEquals(CxSecretKind.SECRET, CxSecretKind.of("oauth.client.CLIENTSECRET"))
    }

    @Test
    fun `a token is named so a message can say what was found`() {
        assertEquals("a token", CxSecretKind.of("sso.accessToken")?.title)
    }

    @Test
    fun `a connection string is named so a message can say what was found`() {
        assertEquals("a connection string", CxSecretKind.of("storage.connectionString")?.title)
    }

    @Test
    fun `a credential is named so a message can say what was found`() {
        assertEquals("a credential", CxSecretKind.of("vault.credentials")?.title)
    }

    @Test
    fun `a secret word which is not the last segment is not what the property holds`() {
        assertNull(CxSecretKind.of("password.policy.minLength"))
    }

    @Test
    fun `an ambiguous key word is left alone rather than guessed at`() {
        assertNull(CxSecretKind.of("license.key"))
    }

    @Test
    fun `a connection without a string is not a secret`() {
        assertNull(CxSecretKind.of("mystore.connection"))
    }

    @Test
    fun `an ordinary property is not a secret`() {
        assertNull(CxSecretKind.of("storefrontContextRoot"))
    }

    @Test
    fun `an empty key is not a secret`() {
        assertNull(CxSecretKind.of(""))
    }
}
