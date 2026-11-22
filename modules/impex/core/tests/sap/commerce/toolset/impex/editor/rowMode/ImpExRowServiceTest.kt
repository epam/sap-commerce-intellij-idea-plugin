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
import com.intellij.openapi.editor.EditorFactory
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

class ImpExRowServiceTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test value line is found when the caret is inside a value`() {
        val file = file(TWO_ROWS)

        assertNotNull(file.findValueLine(TWO_ROWS.indexOf("abc") + 1))
    }

    fun `test value line is found when the caret is at the end of the line`() {
        val file = file(TWO_ROWS)

        assertNotNull(file.findValueLine(TWO_ROWS.indexOf(";abc;ABC") + ";abc;ABC".length))
    }

    fun `test value line is found when the caret is after trailing whitespaces`() {
        val text = "INSERT_UPDATE Product;code\n;abc   \n"
        val file = file(text)

        assertNotNull(file.findValueLine(text.indexOf("abc") + "abc   ".length))
    }

    fun `test value line is found when the caret is at the end of the file`() {
        val text = "INSERT_UPDATE Product;code\n;abc"
        val file = file(text)

        assertNotNull(file.findValueLine(text.length))
    }

    fun `test value line is found for the line under the caret`() {
        val file = file(TWO_ROWS)

        val valueLine = file.findValueLine(TWO_ROWS.indexOf("def") + 1)

        assertEquals(TWO_ROWS.indexOf(";def"), valueLine?.textRange?.startOffset)
    }

    fun `test value line is not found on the header line`() {
        val file = file(TWO_ROWS)

        assertNull(file.findValueLine(TWO_ROWS.indexOf("code") + 1))
    }

    fun `test value line is not found on an empty line`() {
        val text = "INSERT_UPDATE Product;code\n;abc\n\n;def"
        val file = file(text)

        assertNull(file.findValueLine(text.indexOf(";abc") + ";abc\n".length))
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

    fun `test row cell range covers all the words of the value`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc  def ghi;x"
        val row = resolveRow(text, "abc")

        assertEquals("abc  def ghi", row.cells[0].range.substring(text))
    }

    fun `test row cell range covers the words of the value with a macro`() {
        val text = "INSERT_UPDATE Product;code;name\n;\$macro def;x"
        val row = resolveRow(text, "macro")

        assertEquals("\$macro def", row.cells[0].range.substring(text))
    }

    fun `test row cell range covers the words of the value with a quoted string`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc \"d e\";x"
        val row = resolveRow(text, "abc")

        assertEquals("abc \"d e\"", row.cells[0].range.substring(text))
    }

    fun `test row cell range does not include the whitespaces around the value`() {
        val text = "INSERT_UPDATE Product;code;name\n;  abc  ;x"
        val row = resolveRow(text, "abc")

        assertEquals("abc", row.cells[0].range.substring(text))
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

    fun `test row cell of a document id declaration column is marked as such`() {
        val row = resolveRow("INSERT_UPDATE Product;&productId;code\n;id1;abc", "id1")

        assertTrue(row.cells[0].documentIdDeclaration)
    }

    fun `test row cell of a regular column is not a document id declaration`() {
        val row = resolveRow("INSERT_UPDATE Product;&productId;code\n;id1;abc", "id1")

        assertFalse(row.cells[1].documentIdDeclaration)
    }

    fun `test row line number starts from one`() {
        val text = ";abc"
        val row = row(range = TextRange(0, text.length))

        assertEquals(1, row.lineNumber(EditorFactory.getInstance().createDocument(text)))
    }

    fun `test row line number is the line of the value line in the document`() {
        val text = "\$macro = 1\n\nINSERT_UPDATE Product;code\n;abc\n;def"
        val row = resolveRow(text, "def")

        assertEquals(5, row.lineNumber(EditorFactory.getInstance().createDocument(text)))
    }

    fun `test row line number is the first line of a multiline value line`() {
        val text = "INSERT_UPDATE Product;code;name\n;abc;\\\nABC"
        val row = resolveRow(text, "abc")

        assertEquals(2, row.lineNumber(EditorFactory.getInstance().createDocument(text)))
    }

    fun `test row has a cell for every column of the header`() {
        val row = resolveRow("INSERT_UPDATE Product;code;name;desc\n;abc", "abc")

        assertEquals(listOf("code", "name", "desc"), row.cells.map { it.label })
    }

    fun `test row cells of the existing groups are not missing`() {
        val row = resolveRow("INSERT_UPDATE Product;code;name;desc\n;abc;ABC", "abc")

        assertEquals(listOf(0, 0, 1), row.cells.map { it.missingSeparators })
    }

    fun `test row cells of the missing groups know the number of the missing separators`() {
        val row = resolveRow("INSERT_UPDATE Product;code;name;desc;extra\n;abc", "abc")

        assertEquals(listOf(0, 1, 2, 3), row.cells.map { it.missingSeparators })
    }

    fun `test row cell range of a missing group is empty and placed at the end of the value line`() {
        val text = "INSERT_UPDATE Product;code;name;desc\n;abc;ABC"
        val row = resolveRow(text, "abc")

        assertEquals(TextRange.from(text.length, 0), row.cells[2].range)
    }

    fun `test row has no cells for the groups without a column in the header`() {
        val row = resolveRow("INSERT_UPDATE Product;code\n;abc;def;ghi", "abc")

        assertEquals(listOf("code"), row.cells.map { it.label })
    }

    fun `test row knows the number of the groups of the value line`() {
        val row = resolveRow("INSERT_UPDATE Product;code;name;desc\n;abc;ABC", "abc")

        assertEquals(2, row.groupCount)
    }

    fun `test row cell of a missing group keeps the attributes of the column`() {
        val row = resolveRow("INSERT_UPDATE Product;code;&productId;name[unique=true]\n;abc", "abc")

        assertTrue(row.cells[1].documentIdDeclaration)
    }

    fun `test rows with different number of groups do not have the same layout`() {
        val first = row(groupCount = 1)
        val second = row(groupCount = 2)

        assertFalse(first.hasSameLayoutAs(second))
    }

    fun `test row cells are not unique while the type system is not ready`() {
        val row = resolveRow("INSERT_UPDATE Product;code[unique=true];name\n;abc;ABC", "abc")

        assertFalse(row.cells.any { it.unique })
    }

    fun `test rows with different unique cells do not have the same layout`() {
        val first = row(cells = listOf(ImpExRow.Cell("code", TextRange(0, 1), unique = true)))
        val second = row(cells = listOf(ImpExRow.Cell("code", TextRange(0, 1), unique = false)))

        assertFalse(first.hasSameLayoutAs(second))
    }

    fun `test row is not resolved when the value line has no header`() {
        val file = file(";abc;def")
        val valueLine = checkNotNull(PsiTreeUtil.findChildOfType(file, ImpExValueLine::class.java))

        assertNull(ImpExRowService.getInstance(project).resolve(valueLine))
    }

    fun `test rows with different cell labels do not have the same layout`() {
        val first = row(cells = listOf(ImpExRow.Cell("code", TextRange(0, 1))))
        val second = row(cells = listOf(ImpExRow.Cell("name", TextRange(0, 1))))

        assertFalse(first.hasSameLayoutAs(second))
    }

    fun `test rows with different value line lengths still have the same layout`() {
        val first = row(range = TextRange(10, 15))
        val second = row(range = TextRange(10, 40))

        assertTrue(first.hasSameLayoutAs(second))
    }

    fun `test rows with different cell ranges still have the same layout`() {
        val first = row(cells = listOf(ImpExRow.Cell("code", TextRange(0, 1))))
        val second = row(cells = listOf(ImpExRow.Cell("code", TextRange(5, 9))))

        assertTrue(first.hasSameLayoutAs(second))
    }

    fun `test rows starting at different offsets do not have the same layout`() {
        val first = row(range = TextRange(10, 15))
        val second = row(range = TextRange(20, 25))

        assertFalse(first.hasSameLayoutAs(second))
    }

    private fun file(text: String): ImpExFile = ImpExElementFactory.createPhysicalFile(project, text)

    private fun resolveRow(text: String, valueMarker: String): ImpExRow {
        val valueLine = checkNotNull(file(text).findValueLine(text.indexOf(valueMarker) + 1))

        return checkNotNull(ImpExRowService.getInstance(project).resolve(valueLine))
    }

    private fun row(
        range: TextRange = TextRange(0, 1),
        cells: List<ImpExRow.Cell> = emptyList(),
        groupCount: Int = 0,
    ) = ImpExRow(range = range, headerType = "Product", subType = null, cells = cells, groupCount = groupCount)

    companion object {
        private const val TWO_ROWS = "INSERT_UPDATE Product;code[unique=true];name\n;abc;ABC\n;def;DEF"
    }
}
