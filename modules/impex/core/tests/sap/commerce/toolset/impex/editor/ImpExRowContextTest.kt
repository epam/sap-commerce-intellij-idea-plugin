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
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.ImpExParserDefinition
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.psi.*
import sap.commerce.toolset.impex.psi.references.ImpExMacroReference
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ImpExRowContextTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        // plugin descriptors are not loaded for the module tests, the ImpEx language has to be registered manually
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(ImpExLanguage, ImpExParserDefinition(), testRootDisposable)
    }

    fun `test macro used in a cell is resolved to the declaration of the original file`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "\$macro = abc\nINSERT_UPDATE Product;code\n;\$macro")

        val cellFile = cellFile(original, "\$macro")
        val macroUsage = checkNotNull(PsiTreeUtil.findChildOfType(cellFile, ImpExMacroUsageDec::class.java))
        val target = checkNotNull(
            macroUsage.references
                .filterIsInstance<ImpExMacroReference>()
                .firstNotNullOfOrNull { it.resolve() }
        )

        assertSame(original, target.containingFile)
    }

    fun `test file of an element of the cell file is the original file`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code\n;abc")

        val cellFile = cellFile(original, "abc")

        assertSame(original, ImpExRowContext.fileOf(checkNotNull(cellFile.firstChild)))
    }

    fun `test file of an element outside of the cell file is its own file`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code\n;abc")

        assertSame(original, ImpExRowContext.fileOf(checkNotNull(original.firstChild)))
    }

    fun `test line number of a cell element is the line of the original value line`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "\$macro = abc\nINSERT_UPDATE Product;code\n;abc")

        val cellFile = cellFile(original, "abc")
        val value = checkNotNull(PsiTreeUtil.findChildOfType(cellFile, ImpExValue::class.java))

        assertEquals(2, ImpExRowContext.lineNumberOf(value))
    }

    fun `test line number of a header element is the line of the original header`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "\$macro = abc\n\nINSERT_UPDATE Product;code\n;abc")

        val cellFile = cellFile(original, "abc")
        val header = checkNotNull(PsiTreeUtil.findChildOfType(cellFile, ImpExHeaderLine::class.java))

        assertEquals(2, ImpExRowContext.lineNumberOf(header))
    }

    fun `test file of the cell is recognized as a row mode file`() {
        val original = ImpExElementFactory.createPhysicalFile(project, "INSERT_UPDATE Product;code\n;abc")

        val cellFile = cellFile(original, "abc")

        assertTrue(ImpExRowContext.isAttachedTo(checkNotNull(cellFile.viewProvider.virtualFile)))
    }

    fun `test regular ImpEx file is not recognized as a row mode file`() {
        val file = LightVirtualFile("regular.impex", ImpExFileType, "INSERT_UPDATE Product;code\n;abc")

        assertFalse(ImpExRowContext.isAttachedTo(file))
    }

    private fun cellFile(original: ImpExFile, value: String): PsiFile {
        val text = original.text
        val valueLine = checkNotNull(ImpExRowResolver.findValueLine(original, text.lastIndexOf(value) + 1))
        val row = checkNotNull(ImpExRowResolver.resolve(valueLine))
        val synchronizer = ImpExRowCellSynchronizer(
            source = checkNotNull(PsiDocumentManager.getInstance(project).getDocument(original)),
            rowRange = row.range,
            cellRange = row.cells.first().range,
            parentDisposable = testRootDisposable,
            contextPrefix = row.contextPrefix,
            valueLine = SmartPointerManager.createPointer(valueLine),
        )

        return checkNotNull(PsiDocumentManager.getInstance(project).getPsiFile(synchronizer.mirror))
    }
}
