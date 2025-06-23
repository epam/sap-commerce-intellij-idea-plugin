/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.formatting

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchExpression
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchJoinOperator
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchWhereClause
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType

class FxSBlock internal constructor(
    private val node: ASTNode,
    alignment: Alignment?,
    private val indent: Indent?,
    wrap: Wrap?,
    private val codeStyleSettings: CodeStyleSettings,
    private val spacingBuilder: FxSSpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun getDebugName() = "FxS Block"
    override fun isLeaf() = node.firstChildNode == null
    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)
    override fun getIndent() = indent

    companion object {
        private val KEY_WHERE_CLAUSE = Key.create<Alignment>("WHERE_CLAUSE_ALIGNMENTS")
    }

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
//                    null,
//                    null,
                    codeStyleSettings,
                    spacingBuilder
                )

                blocks.add(block)
            }

            child = child.treeNext
        }


        return blocks
    }

    private fun calculateAlignment(child: ASTNode): Alignment? {
        return when {
            child.elementType == RBRACE -> {
                if (child.treeParent.elementType == Y_FROM_CLAUSE) {
                    null
                } else {
                    Alignment.createAlignment()
                }
            }

            child.psi is FlexibleSearchExpression -> {
                val whereClause = child.psi.parentOfType<FlexibleSearchWhereClause>() ?: return null
                whereClause
                    .childrenOfType<FlexibleSearchExpression>()
                    .firstOrNull()
                    ?.takeIf { it == child.psi }
                    ?: return null

                val alignment = Alignment.createAlignment()

                whereClause.putUserData(KEY_WHERE_CLAUSE, alignment)

                alignment
            }

            child.elementType == AND || child.elementType == OR -> {
                val whereClause = child.psi.parentOfType<FlexibleSearchWhereClause>() ?: return null

                whereClause.getUserData(KEY_WHERE_CLAUSE)
            }

            else -> Alignment.createAlignment()
        }
    }

    private fun calculateIndent(child: ASTNode) = when (child.elementType) {
        Y_FROM_CLAUSE,
        FROM_CLAUSE_SELECT -> Indent.getSpaceIndent("FROM".length)

        JOIN_OPERATOR -> if (child.treeParent.elementType == FROM_CLAUSE) {
            Indent.getSpaceIndent("FROM".length)
        } else {
            Indent.getNoneIndent()
        }

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
                Indent.getSpaceIndent("{{".length)
            } else if (PsiTreeUtilExt.getPrevSiblingOfElementType(child.psi, LPAREN) != null) {
                Indent.getSpaceIndent("(".length)
            } else {
                Indent.getNoneIndent()
            }
        }

        FROM_CLAUSE_SIMPLE -> {
            if (child.treeParent.elementType == Y_FROM_CLAUSE) {
                Indent.getSpaceIndent("{".length)
            } else {
                Indent.getNoneIndent()
            }
        }

        SELECT_SUBQUERY_COMBINED,
        COMPOUND_OPERATOR -> {
            if (PsiTreeUtilExt.getPrevSiblingOfElementType(child.psi, LPAREN) != null) {
                Indent.getSpaceIndent("(".length)
            } else {
                Indent.getNoneIndent()
            }
        }

        TABLE_OR_SUBQUERY -> Indent.getSpaceIndent(spacingBuilder.longestJoinOperatorSpaces(child))

        RPAREN -> Indent.getNoneIndent()

        else -> {
            if (PsiTreeUtil.skipWhitespacesAndCommentsBackward(child.psi)?.elementType == WHERE) {
                Indent.getNormalIndent()
            } else if (child.treeParent.elementType == PAREN_EXPRESSION) {
                Indent.getSpaceIndent("(".length)
            } else {
                Indent.getNoneIndent()
            }
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

        RPAREN -> {
            if (PsiTreeUtilExt.getPrevSiblingOfElementType(child.psi, SELECT_SUBQUERY_COMBINED) != null) {
                Wrap.createWrap(WrapType.ALWAYS, true)
            } else {
                Wrap.createWrap(WrapType.NONE, false)
            }
        }

        COMPOUND_OPERATOR -> wrapIf(FxSCodeStyleSettings.WRAP_COMPOUND_OPERATOR)
        FROM_CLAUSE -> wrapIf(FxSCodeStyleSettings.WRAP_FROM_CLAUSE)
        WHERE_CLAUSE -> wrapIf(FxSCodeStyleSettings.WRAP_WHERE_CLAUSE)
        ORDER_CLAUSE -> wrapIf(FxSCodeStyleSettings.WRAP_ORDER_CLAUSE)
        GROUP_BY_CLAUSE -> wrapIf(FxSCodeStyleSettings.WRAP_GROUP_BY_CLAUSE)
        HAVING_CLAUSE -> wrapIf(FxSCodeStyleSettings.WRAP_HAVING_CLAUSE)
        JOIN_CONSTRAINT -> wrapIf(FxSCodeStyleSettings.WRAP_JOIN_CONSTRAINT)

        LDBRACE,
        RDBRACE -> wrapIf(FxSCodeStyleSettings.WRAP_DBRACES)

        CASE_EXPRESSION -> wrapIf(FxSCodeStyleSettings.WRAP_CASE)
        THEN -> wrapIf(FxSCodeStyleSettings.WRAP_CASE_THEN)
        WHEN -> wrapIf(FxSCodeStyleSettings.WRAP_CASE_WHEN)
        ELSE -> wrapIf(FxSCodeStyleSettings.WRAP_CASE_ELSE)

        JOIN_OPERATOR -> if (child.firstChildNode.elementType != COMMA) {
            Wrap.createWrap(WrapType.ALWAYS, true)
        } else {
            Wrap.createWrap(WrapType.NONE, false)
        }

        SELECT_STATEMENT -> {
            if (child.treeParent.elementType == SELECT_SUBQUERY_COMBINED) {
                wrapIf(FxSCodeStyleSettings.WRAP_SELECT_STATEMENT_IN_SUBQUERY)
            } else {
                Wrap.createWrap(WrapType.NONE, false)
            }
        }

        else -> Wrap.createWrap(WrapType.NONE, false)
    }

    private fun wrapIf(enabled: Boolean) = if (enabled) {
        Wrap.createWrap(WrapType.ALWAYS, true)
    } else {
        Wrap.createWrap(WrapType.NONE, false)
    }

}