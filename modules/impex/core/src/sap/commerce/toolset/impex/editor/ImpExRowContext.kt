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

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.util.getLineNumber

/**
 * A cell of the row mode is edited in a standalone ImpEx file, which contains only the header and the cell.
 * Everything that depends on the rest of the original file (macros, document ids) has to be resolved against the original one.
 */
object ImpExRowContext {

    private val KEY_VALUE_LINE = Key.create<SmartPsiElementPointer<ImpExValueLine>>("impex.row_mode.context.value_line")

    fun attach(file: VirtualFile, valueLine: SmartPsiElementPointer<ImpExValueLine>) {
        file.putUserData(KEY_VALUE_LINE, valueLine)
    }

    fun isAttachedTo(file: VirtualFile): Boolean = file.getUserData(KEY_VALUE_LINE) != null

    fun valueLineOf(element: PsiElement): ImpExValueLine? = element.containingFile
        // a completion works on a copy of the file
        ?.originalFile
        ?.viewProvider
        ?.virtualFile
        ?.getUserData(KEY_VALUE_LINE)
        ?.element

    fun fileOf(element: PsiElement): PsiFile = valueLineOf(element)?.containingFile
        ?: element.containingFile

    fun lineNumberOf(element: PsiElement): Int {
        val valueLine = valueLineOf(element)
            ?: return element.getLineNumber()

        val isInHeader = PsiTreeUtil.getParentOfType(element, ImpExHeaderLine::class.java, false) != null

        return (if (isInHeader) valueLine.headerLine else valueLine)
            ?.getLineNumber()
            ?: 0
    }
}
