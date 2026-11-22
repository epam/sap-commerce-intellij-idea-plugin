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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.impex.psi.ImpExFile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The value of a cell is resolved from the PSI each time the document is committed, so it may become different from what the field edits.
 */
class ImpExRowCellRangeTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test value which grows after the whitespaces is not covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;abc  ;x")
        val document = document(file)
        val synchronizer = synchronizerOf(file, document)

        edit(document) { insertString(file.text.indexOf("  ;x") + 2, "d") }

        assertFalse(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    fun `test value which grows before the whitespaces is not covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;  abc;x")
        val document = document(file)
        val synchronizer = synchronizerOf(file, document)

        edit(document) { insertString(file.text.indexOf(";  abc") + 1, "d") }

        assertFalse(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    fun `test value which grows with the new word is not covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;abc  ;x")
        val document = document(file)
        val synchronizer = synchronizerOf(file, document)

        edit(document) { insertString(file.text.indexOf("  ;x") + 2, "d e f") }

        assertFalse(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    fun `test value which grows by the text typed into the field is covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;abc;x")
        val document = document(file)
        val synchronizer = synchronizerOf(file, document)

        edit(document) { synchronizer.mirror.insertString(synchronizer.editableRange.endOffset, " d  e") }

        assertTrue(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    fun `test value with the whitespaces typed into the field is covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;abc;x")
        val document = document(file)
        val synchronizer = synchronizerOf(file, document)

        edit(document) { synchronizer.mirror.insertString(synchronizer.editableRange.endOffset, "  ") }

        assertTrue(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    fun `test unchanged value is covered by the field`() {
        val file = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code;name\n;  abc  ;x")
        val synchronizer = synchronizerOf(file, document(file))

        assertTrue(synchronizer.isCovering(resolveRow(file).cells[0].range))
    }

    private fun document(file: ImpExFile): Document = checkNotNull(PsiDocumentManager.getInstance(project).getDocument(file))

    private fun resolveRow(file: ImpExFile): ImpExRow {
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val valueLine = checkNotNull(ImpExRowService.getInstance(project).findValueLine(file, file.text.lastIndexOf('\n') + 2))

        return checkNotNull(ImpExRowService.getInstance(project).resolve(valueLine))
    }

    private fun synchronizerOf(file: ImpExFile, document: Document): ImpExRowCellSynchronizer {
        val row = resolveRow(file)

        return ImpExRowCellSynchronizer(document, row.range, row.cells[0].range, testRootDisposable, row.contextPrefix)
    }

    private fun edit(document: Document, action: Document.() -> Unit) = WriteCommandAction.runWriteCommandAction(project) { document.action() }
}
