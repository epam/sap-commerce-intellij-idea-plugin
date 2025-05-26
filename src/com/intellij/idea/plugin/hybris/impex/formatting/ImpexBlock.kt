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

package com.intellij.idea.plugin.hybris.impex.formatting


import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

class ImpexBlock(
    node: ASTNode,
    wrap: Wrap? = Wrap.createWrap(WrapType.NONE, false),
    alignment: Alignment? = null,
    private val spacingBuilder: SpacingBuilder,
    private val codeStyleSettings: CodeStyleSettings,
    private val alignmentStrategy: ImpExAlignmentStrategy
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = when (myNode.elementType) {
        ImpexTypes.HEADER_LINE -> "Header Line"
        ImpexTypes.VALUE_LINE -> "Value Line"
        ImpexTypes.ANY_HEADER_MODE -> "Mode"
        ImpexTypes.FULL_HEADER_PARAMETER -> "Parameter"
        ImpexTypes.ANY_HEADER_PARAMETER_NAME -> "Parameter Name"
        ImpexTypes.FULL_HEADER_TYPE -> "Type"
        ImpexTypes.HEADER_TYPE_NAME -> "Name"
        ImpexTypes.SUB_TYPE_NAME -> "Sub-Type"
        ImpexTypes.PARAMETERS_SEPARATOR -> "Separator"
        ImpexTypes.FIELD_VALUE_SEPARATOR -> "Separator"
        ImpexTypes.VALUE_GROUP -> "Value Group"
        ImpexTypes.VALUE -> "Value"
        ImpexTypes.LINE_COMMENT -> "Line Comment"
        ImpexTypes.MODIFIERS -> "Modifiers"
        ImpexTypes.ANY_ATTRIBUTE_NAME -> "Name"
        ImpexTypes.ANY_ATTRIBUTE_VALUE -> "Value"
        ImpexTypes.ASSIGN_VALUE -> "="
        ImpexTypes.ATTRIBUTE -> "Attribute"
        ImpexTypes.ATTRIBUTE_SEPARATOR -> ","
        ImpexTypes.LEFT_SQUARE_BRACKET -> "["
        ImpexTypes.RIGHT_SQUARE_BRACKET -> "]"
        ImpexTypes.MACRO_USAGE_DEC -> "Macro Usage"
        else -> "Block"
    }

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

    private fun isNewBlockToBeMade(currentNode: ASTNode) = currentNode.elementType != TokenType.WHITE_SPACE
        && currentNode.elementType != ImpexTypes.CRLF
        && currentNode.elementType != ImpexTypes.ATTRIBUTE_NAME
        && currentNode.elementType != ImpexTypes.ATTRIBUTE_VALUE
        && currentNode.elementType != ImpexTypes.HEADER_TYPE
        && currentNode.elementType != ImpexTypes.VALUE_SUBTYPE
        && currentNode.treeParent.elementType != ImpexTypes.VALUE
        && currentNode.treeParent.elementType != ImpexTypes.ANY_ATTRIBUTE_VALUE

}