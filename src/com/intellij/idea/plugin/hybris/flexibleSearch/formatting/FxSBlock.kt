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

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchJoinOperator
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class FxSBlock internal constructor(
    private val node: ASTNode,
    private val alignment: Alignment?,
    private val indent: Indent?,
    private val wrap: Wrap?,
    private val codeStyleSettings: CodeStyleSettings,
    private val spacingBuilder: FxSSpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = "FxS Block"
    override fun isLeaf() = node.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent() = indent

    override fun buildChildren(): MutableList<Block> {
        var child = node.firstChildNode
        val blocks = mutableListOf<Block>()
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                val block = FxSBlock(
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
        RBRACE -> {
            if (child.treeParent.elementType == Y_FROM_CLAUSE) {
                null
            } else {
                Alignment.createAlignment()
            }
        }

        else -> Alignment.createAlignment()
    }

    private fun calculateIndent(child: ASTNode) = when (child.elementType) {
        FROM_CLAUSE_EXPRESSION,
        WHEN,
        JOIN_CONSTRAINT,
        ELSE -> Indent.getNormalIndent()

        AS -> if (child.treeParent.elementType == RESULT_COLUMN) {
            Indent.getNormalIndent()
        } else {
            Indent.getNoneIndent()
        }

        RESULT_COLUMNS -> Indent.getSpaceIndent("SELECT".length)

        THEN -> Indent.getContinuationIndent()

        SELECT_STATEMENT -> {
            if (child.treeParent.elementType == SELECT_SUBQUERY_COMBINED) {
                Indent.getNormalIndent()
            } else {
                Indent.getNoneIndent()
            }
        }

        TABLE_OR_SUBQUERY -> Indent.getSpaceIndent(spacingBuilder.longestJoinOperatorSpaces(child))

        else -> if (PsiTreeUtil.skipWhitespacesAndCommentsBackward(child.psi)?.elementType == WHERE) {
            Indent.getNormalIndent()
        } else {
            Indent.getNoneIndent()
        }
    }

    private fun calculateWrap(child: ASTNode) = when (child.elementType) {
        FROM_CLAUSE_SIMPLE,
        RBRACE -> {
            if (child.treeParent.elementType == Y_FROM_CLAUSE
                && PsiTreeUtil.findChildOfType(child.treeParent.psi, FlexibleSearchJoinOperator::class.java) != null
            ) {
                Wrap.createWrap(WrapType.ALWAYS, true)
            } else {
                Wrap.createWrap(WrapType.NONE, false)
            }
        }

        LDBRACE,
        RDBRACE,
        CASE_EXPRESSION,
        WHEN,
        THEN,
        ELSE,
        FROM_CLAUSE,
        ORDER_CLAUSE,
        JOIN_OPERATOR,
        COMPOUND_OPERATOR -> Wrap.createWrap(WrapType.ALWAYS, true)

        JOIN_CONSTRAINT -> wrapIf(FxSCodeStyleSettings.WRAP_JOIN_CONSTRAINT)

        else -> Wrap.createWrap(WrapType.NONE, false)
    }

    private fun wrapIf(enabled: Boolean) = if (enabled) {
        Wrap.createWrap(WrapType.ALWAYS, true)
    } else {
        Wrap.createWrap(WrapType.NONE, false)
    }

}