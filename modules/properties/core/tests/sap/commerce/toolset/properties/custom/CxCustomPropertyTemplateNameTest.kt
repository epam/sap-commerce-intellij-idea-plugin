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

package sap.commerce.toolset.properties.custom

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for numbering a template name until it is free.
 */
class CxCustomPropertyTemplateNameTest {

    @Test
    fun `a name nothing else uses is kept as it is`() {
        assertEquals("Template", unique("Template", "Other"))
    }

    @Test
    fun `a name already taken is numbered`() {
        assertEquals("Template (1)", unique("Template", "Template"))
    }

    @Test
    fun `numbering counts up past the numbers already taken`() {
        assertEquals("Template (3)", unique("Template", "Template", "Template (1)", "Template (2)"))
    }

    @Test
    fun `numbering fills the first gap it finds`() {
        assertEquals("Template (2)", unique("Template", "Template", "Template (1)", "Template (3)"))
    }

    @Test
    fun `a name which is already numbered counts up instead of nesting`() {
        assertEquals("Copy of 'X' (2)", unique("Copy of 'X' (1)", "Copy of 'X'", "Copy of 'X' (1)"))
    }

    @Test
    fun `a numbered name nothing else uses is kept as it is`() {
        assertEquals("Template (1)", unique("Template (1)", "Template"))
    }

    @Test
    fun `numbering is not confused by a name merely containing brackets`() {
        assertEquals("Remote 'hAC' | template (1)", unique("Remote 'hAC' | template", "Remote 'hAC' | template"))
    }

    @Test
    fun `a name is free when no template exists yet`() {
        assertEquals("Template", unique("Template"))
    }

    private fun unique(name: String, vararg existing: String) = CxCustomPropertyTemplateService
        .uniqueName(name, existing.toList())
}
