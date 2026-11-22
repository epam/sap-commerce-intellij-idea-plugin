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
import sap.commerce.toolset.project.vfs.HybrisWritingAccessProvider
import sap.commerce.toolset.settings.WorkspaceSettings
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpExRowEditabilityTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // the provider is applied to the SAP Commerce projects only, the light project of the test is shared
        WorkspaceSettings.getInstance(project).hybrisProject = true
        WritingAccessProvider.EP.getPoint(project).registerExtension(HybrisWritingAccessProvider(project), testRootDisposable)
    }

    override fun tearDown() {
        try {
            WorkspaceSettings.getInstance(project).hybrisProject = false
        } finally {
            super.tearDown()
        }
    }

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

    fun `test read-only document is not editable`() {
        myFixture.configureByText("readonly.txt", "text")
        val document = myFixture.editor.document

        document.setReadOnly(true)

        assertFalse(ImpExRowEditability.isEditable(document))
    }

    fun `test document without a file is editable`() {
        assertTrue(ImpExRowEditability.isEditable(EditorFactory.getInstance().createDocument("text")))
    }

    fun `test document of a file the writing access provider denies is not editable`() {
        myFixture.configureByText("denied.txt", "text")
        val file = myFixture.file.virtualFile
        registerWritingAccessProvider(potentiallyWritable = { it != file })

        assertFalse(ImpExRowEditability.isEditable(myFixture.editor.document, project))
    }

    fun `test document of an out-of-the-box file is not editable`() {
        val synchronizer = synchronizerOf(OOTB_FILE)

        assertFalse(ImpExRowEditability.isEditable(source(), project))
        assertFalse(synchronizer.mirrorWritable)
    }

    fun `test document of a custom file is editable`() {
        val synchronizer = synchronizerOf(CUSTOM_FILE)

        assertTrue(ImpExRowEditability.isEditable(source(), project))
        assertTrue(synchronizer.mirrorWritable)
    }

    fun `test mirror of an out-of-the-box file is not potentially writable`() {
        val synchronizer = synchronizerOf(OOTB_FILE)

        assertFalse(WritingAccessProvider.isPotentiallyWritable(synchronizer.virtualFile, project))
    }

    fun `test mirror of a custom file is potentially writable`() {
        val synchronizer = synchronizerOf(CUSTOM_FILE)

        assertTrue(WritingAccessProvider.isPotentiallyWritable(synchronizer.virtualFile, project))
    }

    fun `test mirror is not writable when the source file is refused by a provider once the modification is requested`() {
        val synchronizer = synchronizerOf(CUSTOM_FILE)
        val file = checkNotNull(FileDocumentManager.getInstance().getFile(source()))
        registerWritingAccessProvider(requestWriting = { files -> files.filter { it == file } })

        assertTrue(WritingAccessProvider.isPotentiallyWritable(file, project))
        assertFalse(synchronizer.mirrorWritable)
    }

    fun `test mirror is not writable when the source file is not writable for the provider`() {
        val synchronizer = synchronizerOf(CUSTOM_FILE)
        val file = checkNotNull(FileDocumentManager.getInstance().getFile(source()))
        registerWritingAccessProvider(potentiallyWritable = { it != file }, requestWriting = { files -> files.filter { it == file } })

        assertFalse(synchronizer.mirrorWritable)
    }

    fun `test mirror is writable for a project which is not a SAP Commerce one`() {
        val synchronizer = synchronizerOf(OOTB_FILE)
        WorkspaceSettings.getInstance(project).hybrisProject = false

        // the provider is not applied, so there is no restriction for the file outside of any provider
        assertTrue(synchronizer.mirrorWritable)
    }

    private val ImpExRowCellSynchronizer.mirrorWritable
        get() = FileDocumentManager.getInstance().requestWriting(mirror, project)

    private fun source() = myFixture.editor.document

    private fun synchronizerOf(path: String): ImpExRowCellSynchronizer {
        myFixture.configureFromExistingVirtualFile(myFixture.addFileToProject(path, "text").virtualFile)

        return ImpExRowCellSynchronizer(source(), TextRange(0, 4), TextRange(0, 4), testRootDisposable)
    }

    private fun registerWritingAccessProvider(
        potentiallyWritable: (VirtualFile) -> Boolean = { true },
        requestWriting: (Collection<VirtualFile>) -> Collection<VirtualFile> = { emptyList() },
    ) {
        WritingAccessProvider.EP.getPoint(project).registerExtension(object : WritingAccessProvider() {
            override fun requestWriting(files: Collection<VirtualFile>) = requestWriting(files)
            override fun isPotentiallyWritable(file: VirtualFile) = potentiallyWritable(file)
        }, testRootDisposable)
    }

    companion object {
        private const val OOTB_FILE = "hybris/bin/modules/ootb/resources/ootb.txt"
        private const val CUSTOM_FILE = "hybris/bin/custom/mine/resources/mine.txt"
    }
}
