/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.settings

import junit.framework.TestCase
import sap.commerce.toolset.settings.state.ImpExInEditorSettingsState
import sap.commerce.toolset.settings.state.ImpExSettingsState
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExInEditorSettingsStateTest : TestCase() {

    fun `test in-editor results are enabled by default`() {
        assertTrue(ImpExInEditorSettingsState().results)
    }

    fun `test in-editor row mode is disabled by default`() {
        assertFalse(ImpExInEditorSettingsState().rowMode)
    }

    fun `test in-editor settings keep the values after the change in the mutable form`() {
        val state = ImpExInEditorSettingsState().mutable()
            .apply {
                results = false
                rowMode = true
            }
            .immutable()

        assertEquals(ImpExInEditorSettingsState(results = false, rowMode = true), state)
    }

    fun `test ImpEx settings use the defaults for the in-editor settings`() {
        assertEquals(ImpExInEditorSettingsState(), ImpExSettingsState().inEditor)
    }

    fun `test ImpEx settings keep the in-editor settings after the change in the mutable form`() {
        val state = ImpExSettingsState().mutable()
            .apply { inEditor.rowMode = true }
            .immutable()

        assertTrue(state.inEditor.rowMode)
        assertTrue(state.inEditor.results)
    }
}
