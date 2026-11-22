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

package sap.commerce.toolset.impex.psi

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExRowPsiTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test value range of a group is the range of its value`() {
        val text = "INSERT_UPDATE Product;code\n;  abc"
        val group = valueGroups(text).single()

        assertEquals(TextRange(text.indexOf("abc"), text.length), group.valueRange)
    }

    fun `test value range of a group without a value is empty and right after the separator`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc;"
        val group = valueGroups(text).last()

        assertEquals(TextRange.from(text.length, 0), group.valueRange)
    }

    fun `test header parameter declaring a document id is a document id declaration`() {
        assertTrue(headerParameters("INSERT_UPDATE Product;&productId").single().isDocumentIdDeclaration)
    }

    fun `test regular header parameter is not a document id declaration`() {
        assertFalse(headerParameters("INSERT_UPDATE Product;code").single().isDocumentIdDeclaration)
    }

    private fun valueGroups(text: String) = PsiTreeUtil.findChildrenOfType(ImpExElementFactory.createPhysicalFile(project, text), ImpExValueGroup::class.java).toList()

    private fun headerParameters(text: String) = PsiTreeUtil.findChildrenOfType(ImpExElementFactory.createPhysicalFile(project, text), ImpExFullHeaderParameter::class.java).toList()
}
