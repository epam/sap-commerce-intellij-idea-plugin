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

package sap.commerce.toolset.flexibleSearch.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.psi.*
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes.*

class FxSSpacingBuilder(settings: CodeStyleSettings) : SpacingBuilder(settings, FlexibleSearchLanguage) {

    init {
        this
            .before(RBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

            .after(LBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

            .before(
                TokenSet.create(
                    LDBRACE, AS, COMPOUND_OPERATOR,
                    IS, NOT, NULL, FROM,
                    FROM_CLAUSE, WHERE_CLAUSE,
                    THEN, ELSE, END, ON,
                    BETWEEN, JOIN, WHERE, JOIN_OPERATOR,
                )
            )
            .spaces(1)

            .after(
                TokenSet.create(
                    JOIN_OPERATOR,
                    SELECT, FROM, WHERE, AS, ON, BY,
                    CASE, WHEN, THEN, ELSE, COMMA,
                )
            )
            .spaces(1)

            .around(TokenSet.create(AND, OR, EQ, EQEQ, GT, GTE, LT, LTE, MINUS, MOD, NOT_EQ, PLUS, SHL, SHR, UNEQ))
            .spaceIf(FxSCodeStyleSettings.SPACE_AROUND_OP)

            .before(TokenSet.create(COLUMN_OUTER_JOIN_NAME, COMMA))
            .spaces(0)

            .after(LBRACKET)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACKETS)
            .before(RBRACKET)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACKETS)

            .after(LDBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)
            .before(RDBRACE)
            .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)

            .around(COLUMN_SEPARATOR)
            .spaces(0)
    }

    override fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
        (child1 as? ASTBlock)?.node
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