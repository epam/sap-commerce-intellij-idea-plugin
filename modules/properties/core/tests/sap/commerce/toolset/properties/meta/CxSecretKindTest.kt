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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for recognising a property whose value is a secret.
 */
class CxSecretKindTest {

    @Test
    fun `a key ending in a secret word is recognised whichever separator is used`() {
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore.smtp.password"))
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore-smtp-password"))
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("mystore_smtp_password"))
    }

    @Test
    fun `case is not what tells a secret apart`() {
        assertEquals(CxSecretKind.API_KEY, CxSecretKind.of("payment.gateway.apiKey"))
        assertEquals(CxSecretKind.SECRET, CxSecretKind.of("oauth.client.CLIENTSECRET"))
    }

    @Test
    fun `each kind is named so a message can say what was found`() {
        assertEquals("a token", CxSecretKind.of("sso.accessToken")?.title)
        assertEquals("a connection string", CxSecretKind.of("storage.connectionString")?.title)
        assertEquals("a credential", CxSecretKind.of("vault.credentials")?.title)
    }

    @Test
    fun `a secret word which is not the last segment is not what the property holds`() {
        assertNull(CxSecretKind.of("password.policy.minLength"))
        assertNull(CxSecretKind.of("token.expiry.seconds"))
    }

    @Test
    fun `an ambiguous word is left alone rather than guessed at`() {
        assertNull(CxSecretKind.of("license.key"))
        assertNull(CxSecretKind.of("cache.key"))
        assertNull(CxSecretKind.of("mystore.connection"))
    }

    @Test
    fun `an ordinary property is not a secret`() {
        assertNull(CxSecretKind.of("storefrontContextRoot"))
        assertNull(CxSecretKind.of(""))
    }

    @Test
    fun `a value which is actually there is a secret`() {
        assertTrue(CxSecretValue.isSecret("hunter2"))
        assertTrue(CxSecretValue.isSecret("AKIAIOSFODNN7EXAMPLEKEY"))
    }

    @Test
    fun `an empty value is a key documenting that something is needed`() {
        assertFalse(CxSecretValue.isSecret(""))
        assertFalse(CxSecretValue.isSecret("   "))
    }

    @Test
    fun `a value which is entirely a placeholder has already been externalised`() {
        assertFalse(CxSecretValue.isSecret("\${SMTP_PASSWORD}"))
        assertFalse(CxSecretValue.isSecret("  \${SMTP_PASSWORD}  "))
    }

    @Test
    fun `a placeholder which is only part of the value leaves the rest written down`() {
        assertTrue(CxSecretValue.isSecret("\${prefix}-actual-secret"))
    }

    @Test
    fun `an obvious stand-in is not a secret`() {
        assertFalse(CxSecretValue.isSecret("changeme"))
        assertFalse(CxSecretValue.isSecret("CHANGEME"))
        assertFalse(CxSecretValue.isSecret("TODO"))
        assertFalse(CxSecretValue.isSecret("xxx"))
        assertFalse(CxSecretValue.isSecret("<your api key here>"))
    }
}
