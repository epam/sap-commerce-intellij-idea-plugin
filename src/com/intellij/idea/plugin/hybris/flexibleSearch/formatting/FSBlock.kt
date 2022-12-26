/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.formatting.alignment.AlignmentStrategy
import com.intellij.formatting.alignment.AlignmentStrategy.createAlignmentPerTypeStrategy
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import java.util.function.Predicate

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class FSBlock internal constructor(
        private val node: ASTNode,
        private val alignment: Alignment?,
        private val indent: Indent?,
        private val wrap: Wrap?,
        private val codeStyleSettings: CodeStyleSettings,
        private val spacingBuilder: SpacingBuilder
) : ASTBlock {

    private var subBlocks: MutableList<Block>? = null

    override fun getNode() = node

    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks(): List<Block> {
        if (subBlocks == null) {
            val isWhitespaceOrEmpty = Predicate<ASTNode> { isWhitespaceOrEmpty(it) }
            val strategy = createStrategy(getNode())

            subBlocks = mutableListOf()
            var subNode: ASTNode? = node.firstChildNode
            while (subNode != null) {
                if (isWhitespaceOrEmpty.test(subNode)) {
                    subNode = subNode.treeNext
                    continue
                }

                val alignment = strategy?.getAlignment(getNode().elementType, subNode.elementType)

                val block = makeSubBlock(subNode, alignment)
                subBlocks!!.add(block)
                subNode = subNode.treeNext
            }
        }
        return subBlocks as MutableList<Block>
    }

    private fun createStrategy(node: ASTNode?): AlignmentStrategy.AlignmentPerTypeStrategy? {
        return if (node == null) {
            null
        } else createAlignmentPerTypeStrategy(arrayListOf(node.elementType), node.elementType, true
        )

    }

    private fun makeSubBlock(node: ASTNode, alignment: Alignment?): FSBlock {
        val wrap = Wrap.createWrap(WrapType.NONE, false)
        val indent = calcIndent(node)

        return FSBlock(node, alignment, indent, wrap, codeStyleSettings, spacingBuilder)
    }

    private fun calcIndent(node: ASTNode): Indent {
        val parentType = this.node.elementType
        val type = node.elementType

        if (parentType === QUERY_SPECIFICATION && type === SUBQUERY) {
            return Indent.getNoneIndent()
        }
        if (type === LEFT_DOUBLE_BRACE || type === RIGHT_DOUBLE_BRACE) {
            return Indent.getNormalIndent()
        }
        if (parentType === SUBQUERY && type === QUERY_SPECIFICATION) {
            return Indent.getContinuationWithoutFirstIndent()
        }
        if (type === FROM_CLAUSE) {
            return Indent.getNormalIndent()
        }
        if (type !== FROM && parentType === FROM_CLAUSE) {
            return Indent.getNormalIndent()
        }
        if (type === ON || type === BOOLEAN_PREDICAND) {
            return Indent.getNormalIndent()
        }
        if (isReturnBodyKeywords(node)) {
            return Indent.getNormalIndent()
        }
        return if (type === WHERE) {
            Indent.getNormalIndent()
        } else Indent.getNoneIndent()

    }

    override fun getWrap() = wrap
    override fun getIndent() = indent
    override fun getAlignment() = alignment

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int) = ChildAttributes(Indent.getNoneIndent(), null)

    override fun isIncomplete() = false

    override fun isLeaf() = node.firstChildNode == null

    private fun isWhitespaceOrEmpty(node: ASTNode): Boolean {
        return node.elementType === TokenType.WHITE_SPACE || node.textLength == 0
    }

    private fun isReturnBodyKeywords(node: ASTNode): Boolean {
        return FormatterUtil.isOneOf(
                node,
                LEFT,
                JOIN,
                ORDER
        )
    }
}