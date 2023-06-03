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
    private val alignmentStrategy: AlignmentStrategy
) : AbstractBlock(node, wrap, alignment) {

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

    override fun getIndent() = Indent.getNoneIndent()

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder
        .getSpacing(this, child1, child2)

    override fun isLeaf() = myNode.firstChildNode == null
}