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

package sap.commerce.toolset.project.settings.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteFoldersStateTest {

    @Test
    fun `no top level folders when there are no favorites`() {
        assertTrue(FavoriteFoldersState().topLevelFolders().isEmpty())
    }

    @Test
    fun `unrelated favorites are all top level`() {
        val state = FavoriteFoldersState(listOf("/project/a", "/project/b"))

        assertEquals(listOf("/project/a", "/project/b"), state.topLevelFolders())
    }

    @Test
    fun `favorite nested into another favorite is not top level`() {
        val state = FavoriteFoldersState(listOf("/project/a", "/project/a/b"))

        assertEquals(listOf("/project/a"), state.topLevelFolders())
    }

    @Test
    fun `nested favorite is hidden regardless of the declaration order`() {
        val state = FavoriteFoldersState(listOf("/project/a/b", "/project/a"))

        assertEquals(listOf("/project/a"), state.topLevelFolders())
    }

    @Test
    fun `deeply nested favorite is hidden by the outermost favorite`() {
        val state = FavoriteFoldersState(listOf("/project/a/b/c", "/project/a/b", "/project/a"))

        assertEquals(listOf("/project/a"), state.topLevelFolders())
    }

    @Test
    fun `folder sharing only a name prefix with a favorite is still top level`() {
        val state = FavoriteFoldersState(listOf("/project/a", "/project/ab"))

        assertEquals(listOf("/project/a", "/project/ab"), state.topLevelFolders())
    }

    @Test
    fun `siblings of a nested favorite stay top level`() {
        val state = FavoriteFoldersState(listOf("/project/a", "/project/a/b", "/project/c"))

        assertEquals(listOf("/project/a", "/project/c"), state.topLevelFolders())
    }
}
