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

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExValueLine

object ImpExRowResolver {

    fun findValueLine(file: PsiFile, offset: Int): ImpExValueLine? {
        valueLineAt(file, offset)?.let { return it }

        val text = file.viewProvider.contents
        if (offset <= 0 || offset > text.length) return null
        if (offset < text.length && text[offset] != '\n' && text[offset] != '\r') return null

        // the caret is at the end of a line, the line itself ends before the trailing whitespaces
        var lastLineOffset = offset
        while (lastLineOffset > 0 && (text[lastLineOffset - 1] == ' ' || text[lastLineOffset - 1] == '\t')) lastLineOffset--

        return if (lastLineOffset > 0) valueLineAt(file, lastLineOffset - 1) else null
    }

    fun resolve(valueLine: ImpExValueLine): ImpExRow? {
        val headerLine = valueLine.headerLine ?: return null
        val headerType = headerLine.fullHeaderType?.headerTypeName?.text ?: return null
        val subType = valueLine.subTypeName?.text

        // the lexer classifies macros based on the declarations above, so they have to be a part of the context
        val context = PsiTreeUtil.findChildrenOfType(valueLine.containingFile, ImpExMacroDeclaration::class.java)
            .filter { it.textRange.endOffset <= valueLine.textRange.startOffset }
            .map { it.text }
            .plus(headerLine.text)
            .joinToString(separator = "\n", postfix = "\n")

        return ImpExRow(
            lineStartOffset = valueLine.textRange.startOffset,
            headerType = headerType,
            subType = subType,
            cells = valueLine.valueGroupList.mapIndexed { index, group ->
                ImpExRowCell(
                    label = group.fullHeaderParameter?.anyHeaderParameterName?.text?.trim()
                        ?: "#${index + 1}",
                    range = group.value?.textRange
                        // the very first character of the group is always the `;` separator
                        ?: TextRange.from(group.textRange.startOffset + 1, 0),
                    // preceding cells are left empty, the column of the cell is defined by the number of the separators
                    contextPrefix = context + (subType ?: "") + ";".repeat(index + 1),
                )
            }
        )
    }

    private fun valueLineAt(file: PsiFile, offset: Int): ImpExValueLine? = file.findElementAt(offset)
        ?.let { PsiTreeUtil.getParentOfType(it, ImpExValueLine::class.java, false) }
}
