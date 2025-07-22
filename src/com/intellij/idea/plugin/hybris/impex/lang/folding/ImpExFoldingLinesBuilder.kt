/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.lang.folding

import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely

class ImpExFoldingLinesBuilder : AbstractImpExFoldingBuilder() {

    override fun buildFoldRegionsInternal(
        psi: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> {
        val impExFile = psi.asSafely<ImpexFile>() ?: return emptyArray()
        val document = PsiDocumentManager.getInstance(psi.project).getDocument(impExFile) ?: return emptyArray()

        return impExFile.getHeaderLines()
            .filter { (_, valueLines) -> valueLines.size > 1 }
            .map { (headerLine, valueLines) ->
                val firstValueLine = valueLines.first()
                val lastValueLine = valueLines.last()
                val lineNumber = document.getLineNumber(firstValueLine.startOffset)
                val lineStartOffset = document.getLineStartOffset(lineNumber)

                ImpexFoldingDescriptor(
                    firstValueLine,
                    lineStartOffset, lastValueLine.endOffset,
                    FoldingGroup.newGroup("impex.${headerLine.startOffset}")
                ) {
                    ";..;.. ${valueLines.size} data rows"
                }
            }.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = "..."
    override fun isCollapsedByDefault(node: ASTNode) = true
}