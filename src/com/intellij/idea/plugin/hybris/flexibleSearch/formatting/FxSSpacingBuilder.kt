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

package com.intellij.idea.plugin.hybris.flexibleSearch.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType

class FxSSpacingBuilder(private val settings: CodeStyleSettings) : SpacingBuilder(settings, FlexibleSearchLanguage.INSTANCE) {

    init {
        this
            .before(RBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

            .after(LBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

            .before(
                TokenSet.create(
                    LDBRACE, RDBRACE,
                    IS, NOT, NULL,
                    THEN, ELSE, END,
                    BETWEEN, JOIN,
                    AND, OR, FROM, EQ, EQEQ, GT, GTE, LT, LTE, MINUS, MOD, NOT_EQ, PLUS, SHL, SHR, UNEQ
                )
            )
            .spaces(1)

            .after(
                TokenSet.create(
                    LDBRACE,
                    SELECT, FROM, WHERE, AS, ON, BY,
                    CASE, WHEN, THEN, ELSE, COMMA,
                    AND, OR, EQ, EQEQ, GT, GTE, LT, LTE, MINUS, MOD, NOT_EQ, PLUS, SHL, SHR, UNEQ
                )
            )
            .spaces(1)

            .before(TokenSet.create(RBRACKET, COLUMN_OUTER_JOIN_NAME, COLUMN_LOCALIZED_NAME))
            .spaces(0)

            .after(LBRACKET)
            .spaces(0)
    }

    override fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
        val childNode1 = (child1 as? ASTBlock)?.node
            ?: return super.getSpacing(null, null, child2)
        val childNode2 = (child2 as? ASTBlock)?.node
            ?: return super.getSpacing(null, null, child2)

        // separator between "JOIN <table>"
        if (child1.node?.elementType == JOIN_OPERATOR && child2.node?.elementType == TABLE_OR_SUBQUERY) {
            val spaces = longestJoinOperatorSpaces(childNode2) - (child1.node?.textLength ?: 0)
            return Spacing.createSpacing(spaces, spaces, 0, true, 0)
        } else if (child1.node?.elementType == DEFINED_TABLE_NAME && child2.node?.elementType == AS) {
            val longestOffset = longestDefinedTableName(childNode2)
            val currentDefinedTableNameLength = child1.node?.textLength ?: 0

            val spaces = longestOffset - currentDefinedTableNameLength + 1
            return Spacing.createSpacing(spaces, spaces, 0, true, 0)
        } else if (child1.node?.elementType == TABLE_OR_SUBQUERY && child2.node?.elementType == JOIN_CONSTRAINT) {
            val longestOffset = longestTableAliasName(childNode2)
            val currentJoinConstraintLength = PsiTreeUtil.findChildOfType(child1.node?.psi, FlexibleSearchTableAliasName::class.java)?.textLength ?: 0

            val spaces = longestOffset - currentJoinConstraintLength + 1
            return Spacing.createSpacing(spaces, spaces, 0, true, 0)
        }

        return super.getSpacing(parent, child1, child2)
    }

    fun longestJoinOperatorSpaces(node: ASTNode) = PsiTreeUtil.getParentOfType(node.psi, FlexibleSearchFromClauseSimple::class.java)
        ?.childrenOfType<FlexibleSearchJoinOperator>()
        ?.maxOfOrNull { it.textLength + 1 }
        ?: 0

    /*
    We have to get only direct children at the same level of nesting, without subqueries
     */
    private fun longestDefinedTableName(node: ASTNode) = PsiTreeUtil.getParentOfType(node.psi, FlexibleSearchFromClauseSimple::class.java)
        ?.childrenOfType<FlexibleSearchTableOrSubquery>()
        ?.flatMap { it.childrenOfType<FlexibleSearchFromTable>() }
        ?.flatMap { it.childrenOfType<FlexibleSearchDefinedTableName>() }
        ?.maxOfOrNull { it.textLength }
        ?: 0

    /*
    We have to get only direct children at the same level of nesting, without subqueries
     */
    private fun longestTableAliasName(node: ASTNode) = PsiTreeUtil.getParentOfType(node.psi, FlexibleSearchFromClauseSimple::class.java)
        ?.childrenOfType<FlexibleSearchTableOrSubquery>()
        ?.flatMap { it.childrenOfType<FlexibleSearchFromTable>() }
        ?.flatMap { it.childrenOfType<FlexibleSearchTableAliasName>() }
        ?.maxOfOrNull { it.textLength }
        ?: 0

}