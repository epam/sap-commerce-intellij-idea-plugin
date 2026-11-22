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

package sap.commerce.toolset.impex.editor

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExValueLine
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImpExRowResolverTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test value line is found when the caret is inside a value`() {
        val file = file(TWO_ROWS)

        assertNotNull(ImpExRowResolver.findValueLine(file, TWO_ROWS.indexOf("abc") + 1))
    }

    fun `test value line is found when the caret is at the end of the line`() {
        val file = file(TWO_ROWS)

        assertNotNull(ImpExRowResolver.findValueLine(file, TWO_ROWS.indexOf(";abc;ABC") + ";abc;ABC".length))
    }

    fun `test value line is found when the caret is after trailing whitespaces`() {
        val text = "INSERT_UPDATE Product;code\n;abc   \n"
        val file = file(text)

        assertNotNull(ImpExRowResolver.findValueLine(file, text.indexOf("abc") + "abc   ".length))
    }

    fun `test value line is found when the caret is at the end of the file`() {
        val text = "INSERT_UPDATE Product;code\n;abc"
        val file = file(text)

        assertNotNull(ImpExRowResolver.findValueLine(file, text.length))
    }

    fun `test value line is found for the line under the caret`() {
        val file = file(TWO_ROWS)

        val valueLine = ImpExRowResolver.findValueLine(file, TWO_ROWS.indexOf("def") + 1)

        assertEquals(TWO_ROWS.indexOf(";def"), valueLine?.textRange?.startOffset)
    }

    fun `test value line is not found on the header line`() {
        val file = file(TWO_ROWS)

        assertNull(ImpExRowResolver.findValueLine(file, TWO_ROWS.indexOf("code") + 1))
    }

    fun `test value line is not found on an empty line`() {
        val text = "INSERT_UPDATE Product;code\n;abc\n\n;def"
        val file = file(text)

        assertNull(ImpExRowResolver.findValueLine(file, text.indexOf(";abc") + ";abc\n".length))
    }

    fun `test row exposes the header type`() {
        val row = resolveRow(TWO_ROWS, "abc")

        assertEquals("Product", row.headerType)
    }

    fun `test row exposes the sub type`() {
        val row = resolveRow("INSERT_UPDATE Product;code\nBaseProduct;abc", "abc")

        assertEquals("BaseProduct", row.subType)
    }

    fun `test row has no sub type when the value line does not declare it`() {
        val row = resolveRow(TWO_ROWS, "abc")

        assertNull(row.subType)
    }

    fun `test row cells are labeled with the header column names`() {
        val row = resolveRow(TWO_ROWS, "abc")

        assertEquals(listOf("code", "name"), row.cells.map { it.label })
    }

    fun `test row cell range covers the value without the separator`() {
        val row = resolveRow(TWO_ROWS, "abc")
        val start = TWO_ROWS.indexOf("abc")

        assertEquals(TextRange(start, start + "abc".length), row.cells[0].range)
    }

    fun `test row cell range of an empty cell is empty and placed right after the separator`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc;"
        val row = resolveRow(text, "abc")

        assertEquals(TextRange.from(text.length, 0), row.cells[1].range)
    }

    fun `test row cell of an empty cell in the middle is placed right after its separator`() {
        val text = "INSERT_UPDATE Product;code;name;description\n;abc;;desc"
        val row = resolveRow(text, "abc")

        assertEquals(TextRange.from(text.indexOf(";;") + 1, 0), row.cells[1].range)
    }

    fun `test row context is the header line`() {
        val row = resolveRow("INSERT_UPDATE Product;code;name\n;abc;ABC", "abc")

        assertEquals("INSERT_UPDATE Product;code;name\n", row.contextPrefix)
    }

    fun `test row context contains the macros declared above the row`() {
        val row = resolveRow("\$macro = 1\nINSERT_UPDATE Product;code\n;abc", "abc")

        assertEquals("\$macro = 1\nINSERT_UPDATE Product;code\n", row.contextPrefix)
    }

    fun `test row context does not contain the macros declared below the row`() {
        val row = resolveRow("INSERT_UPDATE Product;code\n;abc\n\$later = 1", "abc")

        assertEquals("INSERT_UPDATE Product;code\n", row.contextPrefix)
    }

    fun `test row range covers the whole value line`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc;ABC\n;def;DEF"
        val row = resolveRow(text, "abc")
        val start = text.indexOf(";abc;ABC")

        assertEquals(TextRange(start, start + ";abc;ABC".length), row.range)
    }

    fun `test row cell of a unique column is unique`() {
        val row = resolveRow("INSERT_UPDATE Product;code[unique=true];name\n;abc;ABC", "abc")

        assertTrue(row.cells[0].unique)
    }

    fun `test row cell of a regular column is not unique`() {
        val row = resolveRow("INSERT_UPDATE Product;code[unique=true];name\n;abc;ABC", "abc")

        assertFalse(row.cells[1].unique)
    }

    fun `test row is not resolved when the value line has no header`() {
        val file = file(";abc;def")
        val valueLine = checkNotNull(PsiTreeUtil.findChildOfType(file, ImpExValueLine::class.java))

        assertNull(ImpExRowResolver.resolve(valueLine))
    }

    fun `test rows with different cell labels do not have the same layout`() {
        val first = row(cells = listOf(ImpExRowCell("code", TextRange(0, 1))))
        val second = row(cells = listOf(ImpExRowCell("name", TextRange(0, 1))))

        assertFalse(first.hasSameLayoutAs(second))
    }

    fun `test rows with different value line lengths still have the same layout`() {
        val first = row(range = TextRange(10, 15))
        val second = row(range = TextRange(10, 40))

        assertTrue(first.hasSameLayoutAs(second))
    }

    fun `test rows with different cell ranges still have the same layout`() {
        val first = row(cells = listOf(ImpExRowCell("code", TextRange(0, 1))))
        val second = row(cells = listOf(ImpExRowCell("code", TextRange(5, 9))))

        assertTrue(first.hasSameLayoutAs(second))
    }

    fun `test rows starting at different offsets do not have the same layout`() {
        val first = row(range = TextRange(10, 15))
        val second = row(range = TextRange(20, 25))

        assertFalse(first.hasSameLayoutAs(second))
    }

    private fun file(text: String): ImpExFile = ImpExElementFactory.createPhysicalFile(project, text)

    private fun resolveRow(text: String, valueMarker: String): ImpExRow {
        val valueLine = checkNotNull(ImpExRowResolver.findValueLine(file(text), text.indexOf(valueMarker) + 1))

        return checkNotNull(ImpExRowResolver.resolve(valueLine))
    }

    private fun row(
        range: TextRange = TextRange(0, 1),
        cells: List<ImpExRowCell> = emptyList(),
    ) = ImpExRow(range = range, headerType = "Product", subType = null, cells = cells)

    companion object {
        private const val TWO_ROWS = "INSERT_UPDATE Product;code[unique=true];name\n;abc;ABC\n;def;DEF"
    }
}
