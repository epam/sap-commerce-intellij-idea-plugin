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

package sap.commerce.toolset.properties.codeInsight.hints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CxPropertyHintTest {

    @Test
    fun `a declaration another file overrides names the winning file`() {
        assertEquals("overridden by local.properties", CxPropertyHint.of("/local.properties", "local.properties", "1", "/project.properties", "1"))
    }

    @Test
    fun `a declaration carrying a placeholder shows what it expands to`() {
        assertEquals("= /opt/data", CxPropertyHint.of("/project.properties", "project.properties", "/opt/data", "/project.properties", "\${HYBRIS_DATA_DIR}"))
    }

    @Test
    fun `a plain winning declaration gets no hint`() {
        assertNull(CxPropertyHint.of("/project.properties", "project.properties", "1", "/project.properties", "1"))
    }

    @Test
    fun `a declaration whose winner has no path is not reported as overridden`() {
        assertNull(CxPropertyHint.of(null, "environment", "1", "/project.properties", "1"))
    }
}
