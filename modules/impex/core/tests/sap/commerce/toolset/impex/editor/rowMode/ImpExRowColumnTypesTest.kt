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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.impex.psi.ImpExValueLine
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ImpExRowColumnTypesTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test column of a document id declaration is a link`() {
        val types = resolve("INSERT_UPDATE Product;&productId;code\n;id1;abc")

        assertSame(HybrisIcons.ImpEx.DOC_ID_LINK, types[0].icon)
        assertEquals("Document ID", types[0].name)
    }

    fun `test type of a column is unknown when the type system is not ready`() {
        val types = resolve("INSERT_UPDATE Product;&productId;code\n;id1;abc")

        assertNull(types[1].name)
    }

    fun `test every column of the value line has a type`() {
        val types = resolve("INSERT_UPDATE Product;&productId;code;name\n;id1;abc;ABC")

        assertEquals(3, types.size)
    }

    fun `test every column of the header has a type even though the groups of the value line are missing`() {
        val types = resolve("INSERT_UPDATE Product;&productId;code;name\n;id1")

        assertEquals(3, types.size)
    }

    fun `test column of a document id declaration is a link even though the group of the value line is missing`() {
        val types = resolve("INSERT_UPDATE Product;code;&productId\n;abc")

        assertSame(HybrisIcons.ImpEx.DOC_ID_LINK, types[1].icon)
    }

    private fun resolve(text: String): List<ImpExRowColumnType> {
        val file = ImpExElementFactory.createPhysicalFile(project, text)

        return ImpExRowColumnTypes.resolve(checkNotNull(PsiTreeUtil.findChildOfType(file, ImpExValueLine::class.java)))
    }
}
