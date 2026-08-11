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

package sap.commerce.toolset.exec.settings.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [obsoleteFor] — no IntelliJ platform required.
 *
 * The credentials of a connection are keyed by [ConnectionSettingsState.uuid], therefore saving the connection
 * settings may purge the credential store only for the connections which are not persisted anymore. Purging
 * anything else wipes the credentials of the connections the user did not open before saving the settings.
 */
class UtilTest {

    private fun connection(uuid: String, scope: ExecConnectionScope = ExecConnectionScope.PROJECT_PERSONAL) = TestConnectionSettingsState(uuid, scope)

    @Test
    fun obsoleteFor_allRetained_isEmpty() {
        val persisted = listOf(connection("a"), connection("b"))

        assertTrue(persisted.obsoleteFor(persisted).isEmpty())
    }

    @Test
    fun obsoleteFor_removedConnection_isReported() {
        val persisted = listOf(connection("a"), connection("b"))

        val obsolete = persisted.obsoleteFor(listOf(connection("a")))

        assertEquals(listOf("b"), obsolete.map { it.uuid })
    }

    @Test
    fun obsoleteFor_nothingRetained_reportsEverything() {
        val persisted = listOf(connection("a"), connection("b"))

        val obsolete = persisted.obsoleteFor(emptyList())

        assertEquals(listOf("a", "b"), obsolete.map { it.uuid })
    }

    @Test
    fun obsoleteFor_changedScope_isRetained() {
        val persisted = listOf(connection("a", ExecConnectionScope.PROJECT))

        val obsolete = persisted.obsoleteFor(listOf(connection("a", ExecConnectionScope.PROJECT_PERSONAL)))

        assertTrue(obsolete.isEmpty())
    }

    @Test
    fun obsoleteFor_newConnection_isNotReported() {
        val persisted = listOf(connection("a"))

        val obsolete = persisted.obsoleteFor(listOf(connection("a"), connection("new")))

        assertTrue(obsolete.isEmpty())
    }

    @Test
    fun obsoleteFor_noPersistedConnections_isEmpty() {
        val persisted = emptyList<TestConnectionSettingsState>()

        assertTrue(persisted.obsoleteFor(listOf(connection("a"))).isEmpty())
    }
}

private data class TestConnectionSettingsState(
    override val uuid: String,
    override val scope: ExecConnectionScope,
    override val name: String? = null,
    override val host: String = "localhost",
    override val port: String? = null,
    override val webroot: String = "",
    override val ssl: Boolean = true,
    override val timeout: Int = 0,
) : ExecConnectionSettingsState {

    override fun mutable() = error("mutable() is not exercised by these tests")
}
