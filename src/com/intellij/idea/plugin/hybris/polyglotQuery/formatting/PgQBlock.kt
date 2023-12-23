/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.polyglotQuery.formatting

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

class PgQBlock internal constructor(
    private val node: ASTNode,
    alignment: Alignment?,
    private val indent: Indent?,
    wrap: Wrap?,
    private val codeStyleSettings: CodeStyleSettings,
    private val spacingBuilder: PgQSpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = "PgQ Block"
    override fun isLeaf() = node.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent() = indent

    override fun buildChildren(): MutableList<Block> {
        var child = node.firstChildNode
        val blocks = mutableListOf<Block>()
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                val block = PgQBlock(
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


        return blocks
    }

    private fun calculateAlignment(child: ASTNode) = when (child.elementType) {
        else -> Alignment.createAlignment()
    }

    private fun calculateIndent(child: ASTNode) = when (child.elementType) {
        else -> {
            Indent.getNoneIndent()
        }
    }

    private fun calculateWrap(child: ASTNode) = when (child.elementType) {
        PolyglotQueryTypes.GET -> Wrap.createWrap(WrapType.ALWAYS, true)

        PolyglotQueryTypes.WHERE_CLAUSE -> wrapIf(PgQCodeStyleSettings.WRAP_WHERE_CLAUSE)

        PolyglotQueryTypes.ORDER_BY -> wrapIf(PgQCodeStyleSettings.WRAP_WHERE_CLAUSE)

        else -> Wrap.createWrap(WrapType.NONE, false)
    }

    private fun wrapIf(enabled: Boolean) = if (enabled) {
        Wrap.createWrap(WrapType.ALWAYS, true)
    } else {
        Wrap.createWrap(WrapType.NONE, false)
    }

}