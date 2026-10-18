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

/**
 * Unit tests for the catalogue of properties SAP Commerce Cloud documents as not being the project's to declare.
 */
class CxPropertyRuleTest {

    @Test
    fun `a database connection property is recognised as set by automation`() {
        assertEquals(CxPropertyRule.MANAGED_BY_AUTOMATION, CxPropertyRule.of("db.url"))
        assertEquals(CxPropertyRule.MANAGED_BY_AUTOMATION, CxPropertyRule.of("db.password"))
    }

    @Test
    fun `a property of an automation managed family is recognised by its prefix`() {
        assertEquals(CxPropertyRule.MANAGED_BY_AUTOMATION, CxPropertyRule.of("modelt.environment.code"))
        assertEquals(CxPropertyRule.MANAGED_BY_AUTOMATION, CxPropertyRule.of("businessmetrics.edp.token"))
        assertEquals(CxPropertyRule.MANAGED_BY_AUTOMATION, CxPropertyRule.of("ccv2.services.backoffice.url.0"))
    }

    @Test
    fun `a connector port is recognised as set by the build process`() {
        assertEquals(CxPropertyRule.MANAGED_BY_BUILD, CxPropertyRule.of("tomcat.http.port"))
        assertEquals(CxPropertyRule.MANAGED_BY_BUILD, CxPropertyRule.of("standalone.javaoptions"))
    }

    @Test
    fun `a custom index property is recognised as editable in the Cloud Portal only`() {
        assertEquals(
            CxPropertyRule.CLOUD_PORTAL_ONLY,
            CxPropertyRule.of("bootstrap.init.type.system.custom.indices.use.items.definitions"),
        )
    }

    @Test
    fun `a property the documentation says nothing about has no rule`() {
        assertNull(CxPropertyRule.of("my.custom.property"))
        assertNull(CxPropertyRule.of("db.tableprefix"))
    }

    @Test
    fun `a key merely starting like a managed one is not matched`() {
        assertNull(CxPropertyRule.of("db.urlsuffix"))
        assertNull(CxPropertyRule.of("tomcat.http.portable"))
    }

    @Test
    fun `every rule points at the documentation it was transcribed from`() {
        CxPropertyRule.entries.forEach {
            assertEquals(true, it.documentationUrl.startsWith("https://help.sap.com/"), it.name)
        }
    }
}
