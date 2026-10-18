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

class CxSecretKindKeyTest {

    @Test
    fun `a database password is a secret`() {
        assertEquals(CxSecretKind.PASSWORD, CxSecretKind.of("db.password"))
    }

    @Test
    fun `an OAuth client secret is a secret whatever its case`() {
        assertEquals(CxSecretKind.SECRET, CxSecretKind.of("oauth.client.CLIENTSECRET"))
    }

    @Test
    fun `an API key is a secret`() {
        assertEquals(CxSecretKind.API_KEY, CxSecretKind.of("payment.gateway.apiKey"))
    }

    @Test
    fun `a password policy setting is not a secret`() {
        assertNull(CxSecretKind.of("password.policy.minLength"))
    }

    @Test
    fun `an ordinary property is not a secret`() {
        assertNull(CxSecretKind.of("db.url"))
    }
}
