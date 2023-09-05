/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.formatting


import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

class ImpexBlock(
    node: ASTNode,
    wrap: Wrap? = null,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder,
    private val codeStyleSettings: CodeStyleSettings,
    private val alignmentStrategy: ImpExAlignmentStrategy
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = "ImpEx Block"
    override fun isLeaf() = myNode.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent(): Indent = Indent.getNoneIndent()

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()

        alignmentStrategy.processNode(myNode)

        var currentNode = myNode.firstChildNode

        while (currentNode != null) {
            alignmentStrategy.processNode(currentNode)

            if (isNewBlockToBeMade(currentNode)) {
                val block = ImpexBlock(
                    node = currentNode,
                    alignment = alignmentStrategy.getAlignment(currentNode),
                    spacingBuilder = spacingBuilder,
                    codeStyleSettings = codeStyleSettings,
                    alignmentStrategy = alignmentStrategy
                )
                blocks.add(block)
            }

            currentNode = currentNode.treeNext
        }

        return blocks
    }

    private fun isNewBlockToBeMade(currentNode: ASTNode) = isNotWhitespaceOrNewLine(currentNode)
        && !isCurrentNodeHasParentValue(currentNode)

    private fun isNotWhitespaceOrNewLine(currentNode: ASTNode) = currentNode.elementType != TokenType.WHITE_SPACE
        && currentNode.elementType != ImpexTypes.CRLF

    private fun isCurrentNodeHasParentValue(currentNode: ASTNode) = currentNode
        .treeParent.elementType == ImpexTypes.VALUE

}