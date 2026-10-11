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

package sap.commerce.toolset.logging.codeInsight.hints

import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CxLoggerIdentifierResolverTest : LightJavaCodeInsightFixtureTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.addClass(
            """
                package org.apache.logging.log4j;
                public class LogManager {
                    public static Logger getLogger(Class<?> clazz) { return null; }
                    public static Logger getLogger(String name) { return null; }
                }
            """.trimIndent()
        )
        myFixture.addClass("package org.apache.logging.log4j; public interface Logger {}")
    }

    fun `test logger initialised from a factory call resolves to the class name`() {
        val identifier = resolveField("LOG", "private static final Logger LOG = LogManager.getLogger(Sample.class);")

        assertEquals("sample.Sample", identifier)
    }

    fun `test logger initialised from a factory call with a string name resolves to that name`() {
        val identifier = resolveField("LOG", "private static final Logger LOG = LogManager.getLogger(\"custom.logger\");")

        assertEquals("custom.logger", identifier)
    }

    fun `test logger initialised from another logger field resolves to that logger`() {
        val identifier = resolveField(
            "ALIAS",
            """
                private static final Logger LOG = LogManager.getLogger(Sample.class);
                private static final Logger ALIAS = LOG;
            """.trimIndent()
        )

        assertEquals("sample.Sample", identifier)
    }

    fun `test string constant is not treated as a logger`() {
        val identifier = resolveField("DUMMY", "public static final String DUMMY = \"DELETE ME\";")

        assertNull(identifier)
    }

    fun `test class literal constant is not treated as a logger`() {
        val identifier = resolveField("TYPE", "public static final Class<?> TYPE = Sample.class;")

        assertNull(identifier)
    }

    fun `test field referencing a string constant is not treated as a logger`() {
        val identifier = resolveField(
            "ALIAS",
            """
                public static final String NAME = "custom.logger";
                public static final String ALIAS = NAME;
            """.trimIndent()
        )

        assertNull(identifier)
    }

    private fun resolveField(fieldName: String, body: String): String? {
        val file = myFixture.configureByText(
            "Sample.java",
            """
                package sample;
                import org.apache.logging.log4j.LogManager;
                import org.apache.logging.log4j.Logger;
                public class Sample {
                $body
                }
            """.trimIndent()
        ) as PsiJavaFile
        val field = file.classes.single().findFieldByName(fieldName, false)!!

        return CxLoggerIdentifierResolver.getInstance(project).resolve(field)
    }
}
