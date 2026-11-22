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
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExRowCellSynchronizerTest : BasePlatformTestCase() {

    fun `test mirror contains the context followed by the whole row`() {
        val synchronizer = synchronizer(document(), "abc")

        assertEquals(CONTEXT + ROW, synchronizer.mirror.text)
    }

    fun `test editable text is the text of the cell`() {
        val synchronizer = synchronizer(document(), "abc")

        assertEquals("abc", synchronizer.editableText)
    }

    fun `test editable text of an empty cell is empty`() {
        val synchronizer = synchronizer(document(";abc;;def"), TextRange.from(";abc;".length, 0))

        assertEquals("", synchronizer.editableText)
    }

    fun `test source edit inside the cell is reflected in the mirror`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(SOURCE.indexOf("abc") + 1, "X") }

        assertEquals("aXbc", synchronizer.editableText)
        assertEquals(CONTEXT + ROW.replace("abc", "aXbc"), synchronizer.mirror.text)
    }

    fun `test source edit of another cell is reflected in the locked part of the mirror`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(SOURCE.indexOf("ABC") + 1, "X") }

        assertEquals("abc", synchronizer.editableText)
        assertEquals(CONTEXT + ROW.replace("ABC", "AXBC"), synchronizer.mirror.text)
    }

    fun `test source edit outside the row does not change the mirror`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(0, "# comment\n") }

        assertEquals(CONTEXT + ROW, synchronizer.mirror.text)
    }

    fun `test row follows the source edits made before it`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(0, "# comment\n") }
        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset + 1, "X") }

        assertEquals("# comment\n$SOURCE".replace("abc", "aXbc"), source.text)
    }

    fun `test mirror edit is reflected in the source row only`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset + 1, "X") }

        assertEquals(SOURCE.replace("abc", "aXbc"), source.text)
    }

    fun `test mirror text deletion is reflected in the source`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")
        val start = synchronizer.editableRange.startOffset

        edit { synchronizer.mirror.deleteString(start, start + 2) }

        assertEquals(SOURCE.replace("abc", "c"), source.text)
    }

    fun `test replacing the whole cell keeps the row in sync`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.replaceString(synchronizer.editableRange.startOffset, synchronizer.editableRange.endOffset, "new") }
        edit { synchronizer.mirror.insertString(synchronizer.editableRange.endOffset, "!") }

        assertEquals(SOURCE.replace("abc", "new!"), source.text)
        assertEquals("new!", synchronizer.editableText)
    }

    fun `test emptying the cell and typing again keeps the row in sync`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.deleteString(synchronizer.editableRange.startOffset, synchronizer.editableRange.endOffset) }
        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset, "xyz") }

        assertEquals(SOURCE.replace("abc", "xyz"), source.text)
    }

    fun `test typing into an empty cell puts the text between the separators`() {
        val source = document(";abc;;def")
        val synchronizer = synchronizer(source, TextRange.from(";abc;".length, 0))

        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset, "x") }

        assertEquals(";abc;x;def", source.text)
    }

    fun `test edit of a cell is reflected in the mirror of another cell`() {
        val source = document()
        val first = synchronizer(source, "abc")
        val second = synchronizer(source, "ABC")

        edit { first.mirror.insertString(first.editableRange.endOffset, "!!") }

        assertEquals("ABC", second.editableText)
        assertEquals(CONTEXT + ROW.replace("abc", "abc!!"), second.mirror.text)
        assertEquals(SOURCE.replace("abc", "abc!!"), source.text)
    }

    fun `test mirror stays consistent with the source after the edits`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(SOURCE.indexOf("ABC") + 1, "X") }
        edit { synchronizer.mirror.insertString(synchronizer.editableRange.endOffset, "!") }

        assertTrue(synchronizer.isConsistent)
    }

    fun `test mirror is inconsistent when a source edit goes beyond the row`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        edit { source.replaceString(SOURCE.indexOf("code"), SOURCE.indexOf("abc") + 1, "") }

        assertFalse(synchronizer.isConsistent)
    }

    fun `test mirror edit is not applied to a read-only source`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")
        source.setReadOnly(true)

        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset + 1, "X") }

        assertEquals(SOURCE, source.text)
    }

    fun `test undoing a source edit updates the mirror`() {
        myFixture.configureByText("undo.txt", SOURCE)
        val source = myFixture.editor.document
        val synchronizer = synchronizer(source, "abc")

        edit { source.insertString(SOURCE.indexOf("abc") + 1, "X") }
        UndoManager.getInstance(project).undo(TextEditorProvider.getInstance().getTextEditor(myFixture.editor))

        assertEquals(SOURCE, source.text)
        assertEquals("abc", synchronizer.editableText)
    }

    fun `test undoing a mirror edit reverts the source and the mirror`() {
        myFixture.configureByText("undo.txt", SOURCE)
        val source = myFixture.editor.document
        val synchronizer = synchronizer(source, "abc")

        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset + 1, "X") }
        UndoManager.getInstance(project).undo(TextEditorProvider.getInstance().getTextEditor(myFixture.editor))

        assertEquals(SOURCE, source.text)
        assertEquals("abc", synchronizer.editableText)
    }

    fun `test context of the mirror cannot be modified by an editor`() {
        val synchronizer = synchronizer(document(), "abc")

        withGuardedBlockChecking(synchronizer) {
            assertFailsWith<ReadOnlyFragmentModificationException> { edit { synchronizer.mirror.insertString(1, "X") } }
            assertFailsWith<ReadOnlyFragmentModificationException> {
                edit { synchronizer.mirror.deleteString(synchronizer.editableRange.startOffset - 1, synchronizer.editableRange.startOffset) }
            }
        }

        assertEquals(CONTEXT + ROW, synchronizer.mirror.text)
    }

    fun `test other cells of the mirror cannot be modified by an editor`() {
        val synchronizer = synchronizer(document(), "abc")

        withGuardedBlockChecking(synchronizer) {
            assertFailsWith<ReadOnlyFragmentModificationException> { edit { synchronizer.mirror.insertString(synchronizer.mirror.textLength - 1, "X") } }
            assertFailsWith<ReadOnlyFragmentModificationException> {
                edit { synchronizer.mirror.deleteString(synchronizer.editableRange.endOffset, synchronizer.editableRange.endOffset + 1) }
            }
        }

        assertEquals(CONTEXT + ROW, synchronizer.mirror.text)
    }

    fun `test text can be typed by an editor at the start of the cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        withGuardedBlockChecking(synchronizer) {
            edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset, "X") }
        }

        assertEquals(SOURCE.replace("abc", "Xabc"), source.text)
    }

    fun `test text can be typed by an editor at the end of the cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        withGuardedBlockChecking(synchronizer) {
            edit { synchronizer.mirror.insertString(synchronizer.editableRange.endOffset, "X") }
        }

        assertEquals(SOURCE.replace("abc", "abcX"), source.text)
    }

    fun `test text can be typed by an editor into an empty cell`() {
        val source = document(";abc;;def")
        val synchronizer = synchronizer(source, TextRange.from(";abc;".length, 0))

        withGuardedBlockChecking(synchronizer) {
            edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset, "x") }
        }

        assertEquals(";abc;x;def", source.text)
    }

    fun `test text can be typed by an editor into the last cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        withGuardedBlockChecking(synchronizer) {
            edit { synchronizer.mirror.insertString(synchronizer.mirror.textLength, "X") }
        }

        assertEquals(SOURCE + "X", source.text)
    }

    fun `test source group offset is the separator right before the cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        assertEquals(SOURCE.indexOf(";ABC"), synchronizer.sourceGroupOffset)
    }

    fun `test source group offset of the first cell is the first separator of the row`() {
        val source = document()
        val synchronizer = synchronizer(source, "abc")

        assertEquals(SOURCE.indexOf(";abc"), synchronizer.sourceGroupOffset)
    }

    fun `test source group offset skips the whitespaces between the separator and the value`() {
        val source = document("INSERT_UPDATE Product;code\n;   abc")
        val synchronizer = synchronizer(source, "abc")

        assertEquals(source.text.indexOf(";   abc"), synchronizer.sourceGroupOffset)
    }

    fun `test source group offset of an empty cell is its separator`() {
        val source = document(";abc;;def")
        val synchronizer = synchronizer(source, TextRange.from(";abc;".length, 0))

        assertEquals(";abc".length, synchronizer.sourceGroupOffset)
    }

    fun `test source group offset follows the source edits made before it`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        edit { source.insertString(0, "# comment\n") }

        assertEquals("# comment\n".length + SOURCE.indexOf(";ABC"), synchronizer.sourceGroupOffset)
    }

    fun `test source group offset follows the edits of the preceding cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        edit { source.insertString(SOURCE.indexOf("abc") + 1, "XYZ") }

        assertEquals(SOURCE.indexOf(";ABC") + "XYZ".length, synchronizer.sourceGroupOffset)
    }

    fun `test source group offset follows the text appended to the preceding cell`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        edit { source.insertString(SOURCE.indexOf(";ABC"), "X") }

        assertEquals(SOURCE.indexOf(";ABC") + 1, synchronizer.sourceGroupOffset)
        assertEquals(';', source.text[synchronizer.sourceGroupOffset])
    }

    fun `test source group offset follows the text typed into the preceding field`() {
        val source = document()
        val first = synchronizer(source, "abc")
        val second = synchronizer(source, "ABC")

        edit { first.mirror.insertString(first.editableRange.endOffset, "XY") }

        assertEquals(';', source.text[second.sourceGroupOffset])
        assertEquals("ABC", source.text.substring(second.sourceGroupOffset + 1, second.sourceGroupOffset + 4))
    }

    fun `test source group offset does not move when the text is typed into the cell itself`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        edit { synchronizer.mirror.insertString(synchronizer.editableRange.startOffset, "X") }

        assertEquals(SOURCE.indexOf(";ABC"), synchronizer.sourceGroupOffset)
    }

    fun `test source group offset is the end of the value line when the cell has no group`() {
        val source = document("abc")
        val synchronizer = ImpExRowCellSynchronizer(source, TextRange(0, 3), TextRange(0, 3), testRootDisposable)

        assertEquals(3, synchronizer.sourceGroupOffset)
    }

    fun `test source group offset is the end of the value line when the separator is removed`() {
        val source = document()
        val synchronizer = synchronizer(source, "ABC")

        edit { source.deleteString(SOURCE.indexOf(";ABC"), SOURCE.indexOf(";ABC") + 1) }

        assertEquals(source.textLength, synchronizer.sourceGroupOffset)
    }

    fun `test synchronization stops once the parent is disposed`() {
        val source = document()
        val parent = Disposer.newDisposable()
        val synchronizer = ImpExRowCellSynchronizer(source, rowRange(source), cellRange(source, "abc"), parent, CONTEXT)

        Disposer.dispose(parent)
        edit { source.insertString(SOURCE.indexOf("abc") + 1, "X") }

        assertEquals("abc", synchronizer.editableText)
    }

    private fun withGuardedBlockChecking(synchronizer: ImpExRowCellSynchronizer, action: () -> Unit) {
        synchronizer.mirror.startGuardedBlockChecking()
        try {
            action()
        } finally {
            synchronizer.mirror.stopGuardedBlockChecking()
        }
    }

    private fun document(text: String = SOURCE): Document = EditorFactory.getInstance().createDocument(text)

    private fun rowRange(document: Document) = TextRange(document.text.lastIndexOf('\n') + 1, document.textLength)

    private fun cellRange(document: Document, value: String) = rowRange(document).let {
        val start = document.text.indexOf(value, it.startOffset)
        TextRange(start, start + value.length)
    }

    private fun synchronizer(document: Document, value: String) = synchronizer(document, cellRange(document, value))

    private fun synchronizer(document: Document, cellRange: TextRange) = ImpExRowCellSynchronizer(
        source = document,
        rowRange = rowRange(document),
        cellRange = cellRange,
        parentDisposable = testRootDisposable,
        contextPrefix = CONTEXT,
    )

    private fun edit(action: () -> Unit) = WriteCommandAction.runWriteCommandAction(project) { action() }

    companion object {
        private const val CONTEXT = "INSERT_UPDATE Product;code;name\n"
        private const val ROW = ";abc;ABC"
        private const val SOURCE = CONTEXT + ROW
    }
}
