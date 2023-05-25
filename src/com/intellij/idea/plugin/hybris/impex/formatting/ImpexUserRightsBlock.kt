/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRights
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsAwarePsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.parentOfType

class ImpexUserRightsBlock(
    private val node: ASTNode,
    private val alignment: Alignment?,
    private val indent: Indent?,
    private val wrap: Wrap?,
    private val codeStyleSettings: CodeStyleSettings,
    private val spacingBuilder: ImpexUserRightsSpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = "Impex UserRights Block"
    override fun isLeaf() = node.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent() = indent

    override fun buildChildren(): MutableList<Block> {
        var child = node.firstChildNode
        val blocks = mutableListOf<Block>()
        while (child != null) {
            val createBlock = child.elementType != TokenType.WHITE_SPACE
                && (child.psi is ImpexUserRights
                || child.psi is ImpexUserRightsAwarePsiElement
                || child.psi.parentOfType<ImpexUserRights>() != null)
            if (createBlock) {
                val block = ImpexUserRightsBlock(
                    child,
                    calculateAlignment(child),
                    calculateIndent(child),
                    calculateWrap(child),
                    codeStyleSettings,
                    spacingBuilder
                )

                blocks.add(block)
            }

            child = child.treeNext
        }


        return blocks;
    }

    private fun calculateAlignment(child: ASTNode) = when (child.elementType) {
        else -> Alignment.createAlignment()
    }

    private fun calculateIndent(child: ASTNode) = when (child.elementType) {
        else -> Indent.getNoneIndent()
    }

    private fun calculateWrap(child: ASTNode) = when (child.elementType) {
        else -> Wrap.createWrap(WrapType.NONE, false)
    }

    private fun wrapIf(enabled: Boolean) = if (enabled) {
        Wrap.createWrap(WrapType.ALWAYS, true)
    } else {
        Wrap.createWrap(WrapType.NONE, false)
    }
}