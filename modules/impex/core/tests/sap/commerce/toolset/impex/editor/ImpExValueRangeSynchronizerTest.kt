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

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.test.assertEquals

class ImpExValueRangeSynchronizerTest : BasePlatformTestCase() {

    fun `test mirror initially contains the text of the range`() {
        val synchronizer = synchronizer(document(), "abc")

        assertEquals("abc", synchronizer.mirror.text)
    }

    fun `test mirror is empty for an empty range`() {
        val synchronizer = synchronizer(document(";abc;;def"), TextRange.from(";abc;".length, 0))

        assertEquals("", synchronizer.mirror.text)
    }

    fun `test source edit inside the range is reflected in the mirror`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(SOURCE.indexOf("abc") + 1, "X") }

        assertEquals("aXbc", synchronizer.mirror.text)
    }

    fun `test source edit outside the range does not change the mirror`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(0, "# comment\n") }

        assertEquals("abc", synchronizer.mirror.text)
    }

    fun `test range follows the source edits made before it`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(0, "# comment\n") }
        edit { synchronizer.mirror.insertString(1, "X") }

        assertEquals("# comment\n$SOURCE".replace("abc", "aXbc"), source.text)
    }

    fun `test mirror edit is reflected in the source range only`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.insertString(1, "X") }

        assertEquals(SOURCE.replace("abc", "aXbc"), source.text)
    }

    fun `test mirror text deletion is reflected in the source`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.deleteString(0, 2) }

        assertEquals(SOURCE.replace("abc", "c"), source.text)
    }

    fun `test replacing the whole mirror text keeps the source range in sync`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.replaceString(0, synchronizer.mirror.textLength, "new") }
        edit { synchronizer.mirror.insertString(3, "!") }

        assertEquals(SOURCE.replace("abc", "new!"), source.text)
    }

    fun `test emptying the mirror and typing again keeps the source range in sync`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.deleteString(0, synchronizer.mirror.textLength) }
        edit { synchronizer.mirror.insertString(0, "xyz") }

        assertEquals(SOURCE.replace("abc", "xyz"), source.text)
    }

    fun `test typing into an empty range puts the text between the separators`() {
        val source = document(";abc;;def")
        val synchronizer = synchronizer(source, TextRange.from(";abc;".length, 0))

        edit { synchronizer.mirror.insertString(0, "x") }

        assertEquals(";abc;x;def", source.text)
    }

    fun `test other range is not affected by the mirror edit`() {
        val source = document()
        val first = synchronizer(source, "abc")
        val second = synchronizer(source, "ABC")

        edit { first.mirror.insertString(3, "!!") }

        assertEquals("ABC", second.mirror.text)
        assertEquals(SOURCE.replace("abc", "abc!!"), source.text)
    }

    fun `test mirror edit is not applied to a read-only source`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")
        source.setReadOnly(true)

        edit { synchronizer.mirror.insertString(1, "X") }

        assertEquals(SOURCE, source.text)
    }

    fun `test synchronization stops once the parent is disposed`() {
        val source = document()
        val parent = Disposer.newDisposable()
        val synchronizer = ImpExValueRangeSynchronizer(source, range(source, "abc"), parent)

        Disposer.dispose(parent)
        edit { source.insertString(SOURCE.indexOf("abc") + 1, "X") }

        assertEquals("abc", synchronizer.mirror.text)
    }

    private fun document(text: String = SOURCE): Document = EditorFactory.getInstance().createDocument(text)

    private fun range(document: Document, value: String): TextRange = document.text.indexOf(value).let { TextRange(it, it + value.length) }

    private fun synchronizer(document: Document, value: String) = synchronizer(document, range(document, value))

    private fun synchronizer(document: Document, range: TextRange) = ImpExValueRangeSynchronizer(document, range, testRootDisposable)

    private fun edit(action: () -> Unit) = WriteCommandAction.runWriteCommandAction(project) { action() }

    companion object {
        private const val SOURCE = "INSERT_UPDATE Product;code;name\n;abc;ABC"
    }
}
