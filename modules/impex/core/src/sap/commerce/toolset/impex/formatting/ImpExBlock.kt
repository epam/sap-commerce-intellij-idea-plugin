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

package sap.commerce.toolset.impex.formatting


import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import sap.commerce.toolset.impex.psi.ImpExTypes

class ImpExBlock(
    node: ASTNode,
    wrap: Wrap? = Wrap.createWrap(WrapType.NONE, false),
    alignment: Alignment? = null,
    private val spacingBuilder: SpacingBuilder,
    private val codeStyleSettings: CodeStyleSettings,
    private val alignmentStrategy: ImpExAlignmentStrategy
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = when (myNode.elementType) {
        ImpExTypes.HEADER_LINE -> "Header Line"
        ImpExTypes.VALUE_LINE -> "Value Line"
        ImpExTypes.ANY_HEADER_MODE -> "Mode"
        ImpExTypes.FULL_HEADER_PARAMETER -> "Parameter"
        ImpExTypes.ANY_HEADER_PARAMETER_NAME -> "Parameter Name"
        ImpExTypes.FULL_HEADER_TYPE -> "Type"
        ImpExTypes.HEADER_TYPE_NAME -> "Name"
        ImpExTypes.SUB_TYPE_NAME -> "Sub-Type"
        ImpExTypes.PARAMETERS_SEPARATOR -> "Separator"
        ImpExTypes.FIELD_VALUE_SEPARATOR -> "Separator"
        ImpExTypes.VALUE_GROUP -> "Value Group"
        ImpExTypes.VALUE -> "Value"
        ImpExTypes.LINE_COMMENT -> "Line Comment"
        ImpExTypes.MODIFIERS -> "Modifiers"
        ImpExTypes.ANY_ATTRIBUTE_NAME -> "Name"
        ImpExTypes.ANY_ATTRIBUTE_VALUE -> "Value"
        ImpExTypes.ASSIGN_VALUE -> "="
        ImpExTypes.ATTRIBUTE -> "Attribute"
        ImpExTypes.ATTRIBUTE_SEPARATOR -> ","
        ImpExTypes.LEFT_SQUARE_BRACKET -> "["
        ImpExTypes.RIGHT_SQUARE_BRACKET -> "]"
        ImpExTypes.MACRO_USAGE_DEC -> "Macro Usage"
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
                val block = ImpExBlock(
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
        && currentNode.elementType != ImpExTypes.CRLF
        && currentNode.elementType != ImpExTypes.ATTRIBUTE_NAME
        && currentNode.elementType != ImpExTypes.ATTRIBUTE_VALUE
        && currentNode.elementType != ImpExTypes.HEADER_TYPE
        && currentNode.elementType != ImpExTypes.VALUE_SUBTYPE
        && currentNode.treeParent.elementType != ImpExTypes.VALUE
        && currentNode.treeParent.elementType != ImpExTypes.ANY_ATTRIBUTE_VALUE

}