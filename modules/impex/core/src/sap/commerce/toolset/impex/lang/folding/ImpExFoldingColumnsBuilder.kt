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
package sap.commerce.toolset.impex.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter

class ImpExFoldingColumnsBuilder : AbstractImpExFoldingBuilder() {

    override fun buildFoldRegionsInternal(
        psi: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> = psi.asSafely<ImpExFile>()
        ?.getHeaderLines()
        ?.flatMap { (headerLine, valueLines) ->
            val statementBlock = headerLine.startOffset

            headerLine.fullHeaderParameterList
                .flatMap { parameter ->
                    val columnNumber = parameter.columnNumber
                    val group = FoldingGroup.newGroup("${ImpExConstants.Folding.GROUP_PREFIX}.$statementBlock.$columnNumber")
                    val valuesDescriptors = valueLines
                        .mapNotNull { valueLine -> valueLine.getValueGroup(columnNumber) }
                        .map { valueGroup ->
                            ImpExFoldingDescriptor(
                                psiElement = valueGroup,
                                startOffset = valueGroup.startOffset,
                                endOffset = valueGroup.evaluateEndOffset(),
                                group = group
                            ) {
                                "${ImpExConstants.Folding.VALUE_PREFIX}$columnNumber${ImpExConstants.Folding.VALUE_POSTFIX}"
                            }
                        }
                    val columnDescriptor = ImpExFoldingDescriptor(
                        psiElement = parameter,
                        startOffset = parameter.evaluateStartOffset(),
                        endOffset = parameter.evaluateEndOffset(),
                        group = group
                    ) { "${ImpExConstants.Folding.HEADER_PREFIX}$columnNumber${ImpExConstants.Folding.HEADER_POSTFIX}" }

                    buildList {
                        addAll(valuesDescriptors)
                        add(columnDescriptor)
                    }
                }
        }
        ?.toTypedArray()
        ?: emptyArray()

    override fun getPlaceholderText(node: ASTNode) = node.psi
        .asSafely<ImpExFullHeaderParameter>()
        ?.let { "<${it.columnNumber}>" }
        ?: "; <..>"

    override fun isCollapsedByDefault(node: ASTNode) = false

    private fun PsiElement.evaluateEndOffset(): Int = this.nextSibling
        .asSafely<PsiWhiteSpace>()
        ?.endOffset
        ?: endOffset

    private fun PsiElement.evaluateStartOffset(): Int = this.prevSibling
        .asSafely<PsiWhiteSpace>()
        ?.startOffset
        ?: startOffset
}
