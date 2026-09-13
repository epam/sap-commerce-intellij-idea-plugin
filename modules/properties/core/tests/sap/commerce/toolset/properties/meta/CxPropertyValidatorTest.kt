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
import kotlin.test.assertTrue

/**
 * Unit tests for holding a property chain against the SAP Commerce Cloud rule catalogue.
 */
class CxPropertyValidatorTest {

    private val extension = CxPropertySource("project.properties", CxPropertyScope.PROJECT, extension = "mystore")
    private val otherExtension = CxPropertySource("project.properties", CxPropertyScope.PROJECT, rank = 1, extension = "mycore")
    private val local = CxPropertySource("local.properties", CxPropertyScope.LOCAL)
    private val advanced = CxPropertySource("advanced.properties", CxPropertyScope.ADVANCED)
    private val environment = CxPropertySource("System Environment", CxPropertyScope.ENVIRONMENT)

    @Test
    fun `a property outside the catalogue is not reported`() {
        assertEquals(emptyList(), validate(CxPropertyDeclaration("mystore.feature.enabled", "true", extension)))
    }

    @Test
    fun `an environment property declared in an extension is an error`() {
        val violations = validate(CxPropertyDeclaration("db.url", "jdbc:mine", extension))

        assertEquals(listOf("db.url"), violations.map { it.key })
        assertEquals(listOf(CxPropertyRuleSeverity.ERROR), violations.map { it.severity })
        assertEquals(listOf(CxPropertyRule.MANAGED_BY_AUTOMATION), violations.map { it.rule })
    }

    @Test
    fun `an environment property declared in local properties is left alone`() {
        assertEquals(emptyList(), validate(CxPropertyDeclaration("db.url", "jdbc:mine", local)))
    }

    @Test
    fun `a build-managed property is reported wherever the project declares it`() {
        val violations = validate(
            CxPropertyDeclaration("tomcat.jmx.port", "1099", extension),
            CxPropertyDeclaration("tomcat.jmx.port", "1100", local),
        )

        assertEquals(listOf("local.properties", "mystore/project.properties"), violations.map { it.source.presentableName }.sorted())
        assertTrue(violations.all { it.severity == CxPropertyRuleSeverity.WARNING })
    }

    @Test
    fun `a value injected by the platform itself is not the project's doing`() {
        assertEquals(emptyList(), validate(CxPropertyDeclaration("db.url", "jdbc:injected", environment)))
        assertEquals(emptyList(), validate(CxPropertyDeclaration("java.mem", "512M", advanced)))
    }

    @Test
    fun `a prefixed key matches the catalogue`() {
        assertEquals(listOf("ccv2.services.api.url"), validate(CxPropertyDeclaration("ccv2.services.api.url", "x", extension)).map { it.key })
    }

    @Test
    fun `every declaration of one property is reported, not only the winning one`() {
        val violations = validate(
            CxPropertyDeclaration("db.username", "a", extension),
            CxPropertyDeclaration("db.username", "b", otherExtension),
        )

        assertEquals(2, violations.size)
    }

    @Test
    fun `errors are reported before warnings`() {
        val violations = validate(
            CxPropertyDeclaration("tomcat.jmx.port", "1099", extension),
            CxPropertyDeclaration("db.url", "jdbc:mine", extension),
        )

        assertEquals(listOf("db.url", "tomcat.jmx.port"), violations.map { it.key })
    }

    @Test
    fun `the message of an error names the file the declaration lives in`() {
        val violation = validate(CxPropertyDeclaration("db.password", "secret", extension)).single()

        assertEquals(
            "'db.password' is set by SAP Commerce Cloud automation for each environment, " +
                "declaring it in project.properties hard-codes one environment into every build",
            violation.message,
        )
    }

    private fun validate(vararg declarations: CxPropertyDeclaration) = CxPropertyValidator.validate(
        CxPropertyModel.of(declarations.map { it.source }, declarations.toList())
    )
}
