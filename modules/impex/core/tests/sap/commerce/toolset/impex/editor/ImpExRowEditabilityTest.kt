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

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExRowEditabilityTest : BasePlatformTestCase() {

    fun `test document of a writable file is editable`() {
        myFixture.configureByText("editable.txt", "text")

        assertTrue(ImpExRowEditability.isEditable(myFixture.editor.document))
    }

    fun `test document of a read-only file is not editable`() {
        myFixture.configureByText("readonly.txt", "text")
        val document = myFixture.editor.document

        runWriteAction { myFixture.file.virtualFile.isWritable = false }

        assertFalse(ImpExRowEditability.isEditable(document, project))
    }

    fun `test document of a file the writing access provider denies is not editable`() {
        myFixture.configureByText("denied.txt", "text")
        val file = myFixture.file.virtualFile
        registerWritingAccessProvider(potentiallyWritable = { it != file })

        assertFalse(ImpExRowEditability.isEditable(myFixture.editor.document, project))
    }

    fun `test mirror is not writable when the source file is not writable for the provider`() {
        myFixture.configureByText("denied.txt", "text")
        val source = myFixture.file.virtualFile
        registerWritingAccessProvider(requestWriting = { files -> files.filter { it == source } })
        val synchronizer = ImpExRowCellSynchronizer(myFixture.editor.document, TextRange(0, 4), TextRange(0, 4), testRootDisposable)

        assertFalse(FileDocumentManager.getInstance().requestWriting(synchronizer.mirror, project))
    }

    fun `test mirror is writable when the source file is writable for the provider`() {
        myFixture.configureByText("allowed.txt", "text")
        val synchronizer = ImpExRowCellSynchronizer(myFixture.editor.document, TextRange(0, 4), TextRange(0, 4), testRootDisposable)

        assertTrue(FileDocumentManager.getInstance().requestWriting(synchronizer.mirror, project))
    }

    private fun registerWritingAccessProvider(
        potentiallyWritable: (VirtualFile) -> Boolean = { true },
        requestWriting: (Collection<VirtualFile>) -> Collection<VirtualFile> = { emptyList() },
    ) {
        val point = WritingAccessProvider.EP.getPoint(project)

        point.registerExtension(ImpExRowWritingAccessProvider(project), testRootDisposable)
        point.registerExtension(object : WritingAccessProvider() {
            override fun requestWriting(files: Collection<VirtualFile>) = requestWriting(files)
            override fun isPotentiallyWritable(file: VirtualFile) = potentiallyWritable(file)
        }, testRootDisposable)
    }

    fun `test read-only document is not editable`() {
        myFixture.configureByText("readonly.txt", "text")
        val document = myFixture.editor.document

        document.setReadOnly(true)

        assertFalse(ImpExRowEditability.isEditable(document))
    }

    fun `test document without a file is editable`() {
        assertTrue(ImpExRowEditability.isEditable(EditorFactory.getInstance().createDocument("text")))
    }
}
