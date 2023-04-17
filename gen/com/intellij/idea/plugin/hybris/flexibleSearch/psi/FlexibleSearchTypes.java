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

// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

public interface FlexibleSearchTypes {

  IElementType AND_EXPRESSION = new FlexibleSearchElementType("AND_EXPRESSION");
  IElementType BETWEEN_EXPRESSION = new FlexibleSearchElementType("BETWEEN_EXPRESSION");
  IElementType BIND_PARAMETER = new FlexibleSearchElementType("BIND_PARAMETER");
  IElementType BIT_EXPRESSION = new FlexibleSearchElementType("BIT_EXPRESSION");
  IElementType CASE_EXPRESSION = new FlexibleSearchElementType("CASE_EXPRESSION");
  IElementType CAST_EXPRESSION = new FlexibleSearchElementType("CAST_EXPRESSION");
  IElementType COLUMN_ALIAS_NAME = new FlexibleSearchElementType("COLUMN_ALIAS_NAME");
  IElementType COLUMN_NAME = new FlexibleSearchElementType("COLUMN_NAME");
  IElementType COLUMN_REF_EXPRESSION = new FlexibleSearchElementType("COLUMN_REF_EXPRESSION");
  IElementType COMPARISON_EXPRESSION = new FlexibleSearchElementType("COMPARISON_EXPRESSION");
  IElementType COMPOUND_OPERATOR = new FlexibleSearchElementType("COMPOUND_OPERATOR");
  IElementType CONCAT_EXPRESSION = new FlexibleSearchElementType("CONCAT_EXPRESSION");
  IElementType DEFINED_TABLE_NAME = new FlexibleSearchElementType("DEFINED_TABLE_NAME");
  IElementType EQUIVALENCE_EXPRESSION = new FlexibleSearchElementType("EQUIVALENCE_EXPRESSION");
  IElementType EXISTS_EXPRESSION = new FlexibleSearchElementType("EXISTS_EXPRESSION");
  IElementType EXPRESSION = new FlexibleSearchElementType("EXPRESSION");
  IElementType FROM_CLAUSE = new FlexibleSearchElementType("FROM_CLAUSE");
  IElementType FROM_TABLE = new FlexibleSearchElementType("FROM_TABLE");
  IElementType GROUP_BY_CLAUSE = new FlexibleSearchElementType("GROUP_BY_CLAUSE");
  IElementType IN_EXPRESSION = new FlexibleSearchElementType("IN_EXPRESSION");
  IElementType ISNULL_EXPRESSION = new FlexibleSearchElementType("ISNULL_EXPRESSION");
  IElementType JOIN_CONSTRAINT = new FlexibleSearchElementType("JOIN_CONSTRAINT");
  IElementType JOIN_OPERATOR = new FlexibleSearchElementType("JOIN_OPERATOR");
  IElementType LIKE_EXPRESSION = new FlexibleSearchElementType("LIKE_EXPRESSION");
  IElementType LIMIT_CLAUSE = new FlexibleSearchElementType("LIMIT_CLAUSE");
  IElementType LITERAL_EXPRESSION = new FlexibleSearchElementType("LITERAL_EXPRESSION");
  IElementType MUL_EXPRESSION = new FlexibleSearchElementType("MUL_EXPRESSION");
  IElementType ORDERING_TERM = new FlexibleSearchElementType("ORDERING_TERM");
  IElementType ORDER_CLAUSE = new FlexibleSearchElementType("ORDER_CLAUSE");
  IElementType OR_EXPRESSION = new FlexibleSearchElementType("OR_EXPRESSION");
  IElementType PAREN_EXPRESSION = new FlexibleSearchElementType("PAREN_EXPRESSION");
  IElementType RESULT_COLUMN = new FlexibleSearchElementType("RESULT_COLUMN");
  IElementType RESULT_COLUMNS = new FlexibleSearchElementType("RESULT_COLUMNS");
  IElementType SELECTED_TABLE_NAME = new FlexibleSearchElementType("SELECTED_TABLE_NAME");
  IElementType SELECT_CORE = new FlexibleSearchElementType("SELECT_CORE");
  IElementType SELECT_CORE_SELECT = new FlexibleSearchElementType("SELECT_CORE_SELECT");
  IElementType SELECT_STATEMENT = new FlexibleSearchElementType("SELECT_STATEMENT");
  IElementType SELECT_SUBQUERY = new FlexibleSearchElementType("SELECT_SUBQUERY");
  IElementType SIGNED_NUMBER = new FlexibleSearchElementType("SIGNED_NUMBER");
  IElementType TABLE_ALIAS_NAME = new FlexibleSearchElementType("TABLE_ALIAS_NAME");
  IElementType TABLE_OR_SUBQUERY = new FlexibleSearchElementType("TABLE_OR_SUBQUERY");
  IElementType TYPE_NAME = new FlexibleSearchElementType("TYPE_NAME");
  IElementType UNARY_EXPRESSION = new FlexibleSearchElementType("UNARY_EXPRESSION");
  IElementType WHERE_CLAUSE = new FlexibleSearchElementType("WHERE_CLAUSE");

  IElementType ALL = new FlexibleSearchTokenType("ALL");
  IElementType AND = new FlexibleSearchTokenType("AND");
  IElementType AS = new FlexibleSearchTokenType("AS");
  IElementType ASC = new FlexibleSearchTokenType("ASC");
  IElementType ASTERISK = new FlexibleSearchTokenType("*");
  IElementType BACKTICK_LITERAL = new FlexibleSearchTokenType("BACKTICK_LITERAL");
  IElementType BETWEEN = new FlexibleSearchTokenType("BETWEEN");
  IElementType BRACKET_LITERAL = new FlexibleSearchTokenType("BRACKET_LITERAL");
  IElementType BY = new FlexibleSearchTokenType("BY");
  IElementType CASE = new FlexibleSearchTokenType("CASE");
  IElementType CAST = new FlexibleSearchTokenType("CAST");
  IElementType COLON = new FlexibleSearchTokenType(":");
  IElementType COMMA = new FlexibleSearchTokenType(",");
  IElementType CROSS = new FlexibleSearchTokenType("CROSS");
  IElementType CURRENT_DATE = new FlexibleSearchTokenType("CURRENT_DATE");
  IElementType CURRENT_TIME = new FlexibleSearchTokenType("CURRENT_TIME");
  IElementType CURRENT_TIMESTAMP = new FlexibleSearchTokenType("CURRENT_TIMESTAMP");
  IElementType DESC = new FlexibleSearchTokenType("DESC");
  IElementType DISTINCT = new FlexibleSearchTokenType("DISTINCT");
  IElementType DOT = new FlexibleSearchTokenType(".");
  IElementType DOUBLE_QUOTE_STRING_LITERAL = new FlexibleSearchTokenType("DOUBLE_QUOTE_STRING_LITERAL");
  IElementType ELSE = new FlexibleSearchTokenType("ELSE");
  IElementType END = new FlexibleSearchTokenType("END");
  IElementType EQUALS_OPERATOR = new FlexibleSearchTokenType("EQUALS_OPERATOR");
  IElementType ESCAPE = new FlexibleSearchTokenType("ESCAPE");
  IElementType EXCLAMATION_MARK = new FlexibleSearchTokenType("!");
  IElementType EXISTS = new FlexibleSearchTokenType("EXISTS");
  IElementType FROM = new FlexibleSearchTokenType("FROM");
  IElementType GLOB = new FlexibleSearchTokenType("GLOB");
  IElementType GREATER_THAN_OPERATOR = new FlexibleSearchTokenType(">");
  IElementType GREATER_THAN_OR_EQUALS_OPERATOR = new FlexibleSearchTokenType("GREATER_THAN_OR_EQUALS_OPERATOR");
  IElementType GROUP = new FlexibleSearchTokenType("GROUP");
  IElementType HAVING = new FlexibleSearchTokenType("HAVING");
  IElementType IDENTIFIER = new FlexibleSearchTokenType("IDENTIFIER");
  IElementType IN = new FlexibleSearchTokenType("IN");
  IElementType INNER = new FlexibleSearchTokenType("INNER");
  IElementType IS = new FlexibleSearchTokenType("IS");
  IElementType ISNULL = new FlexibleSearchTokenType("ISNULL");
  IElementType JOIN = new FlexibleSearchTokenType("JOIN");
  IElementType LEFT = new FlexibleSearchTokenType("LEFT");
  IElementType LEFT_BRACE = new FlexibleSearchTokenType("LEFT_BRACE");
  IElementType LEFT_BRACKET = new FlexibleSearchTokenType("[");
  IElementType LEFT_DOUBLE_BRACE = new FlexibleSearchTokenType("LEFT_DOUBLE_BRACE");
  IElementType LEFT_PAREN = new FlexibleSearchTokenType("(");
  IElementType LESS_THAN_OPERATOR = new FlexibleSearchTokenType("<");
  IElementType LESS_THAN_OR_EQUALS_OPERATOR = new FlexibleSearchTokenType("LESS_THAN_OR_EQUALS_OPERATOR");
  IElementType LIKE = new FlexibleSearchTokenType("LIKE");
  IElementType LIMIT = new FlexibleSearchTokenType("LIMIT");
  IElementType LINE_COMMENT = new FlexibleSearchTokenType("LINE_COMMENT");
  IElementType MATCH = new FlexibleSearchTokenType("MATCH");
  IElementType MINUS_SIGN = new FlexibleSearchTokenType("-");
  IElementType NAMED_PARAMETER = new FlexibleSearchTokenType("NAMED_PARAMETER");
  IElementType NOT = new FlexibleSearchTokenType("NOT");
  IElementType NOTNULL = new FlexibleSearchTokenType("NOTNULL");
  IElementType NOT_EQUALS_OPERATOR = new FlexibleSearchTokenType("<>");
  IElementType NULL = new FlexibleSearchTokenType("NULL");
  IElementType NUMBER = new FlexibleSearchTokenType("NUMBER");
  IElementType NUMBERED_PARAMETER = new FlexibleSearchTokenType("NUMBERED_PARAMETER");
  IElementType NUMERIC_LITERAL = new FlexibleSearchTokenType("NUMERIC_LITERAL");
  IElementType OFFSET = new FlexibleSearchTokenType("OFFSET");
  IElementType ON = new FlexibleSearchTokenType("ON");
  IElementType OR = new FlexibleSearchTokenType("OR");
  IElementType ORDER = new FlexibleSearchTokenType("ORDER");
  IElementType OUTER = new FlexibleSearchTokenType("OUTER");
  IElementType PERCENT = new FlexibleSearchTokenType("%");
  IElementType PLUS_SIGN = new FlexibleSearchTokenType("+");
  IElementType QUESTION_MARK = new FlexibleSearchTokenType("?");
  IElementType QUOTE = new FlexibleSearchTokenType("'");
  IElementType REGEXP = new FlexibleSearchTokenType("REGEXP");
  IElementType RIGHT_BRACE = new FlexibleSearchTokenType("RIGHT_BRACE");
  IElementType RIGHT_BRACKET = new FlexibleSearchTokenType("]");
  IElementType RIGHT_DOUBLE_BRACE = new FlexibleSearchTokenType("RIGHT_DOUBLE_BRACE");
  IElementType RIGHT_PAREN = new FlexibleSearchTokenType(")");
  IElementType SELECT = new FlexibleSearchTokenType("SELECT");
  IElementType SEMICOLON = new FlexibleSearchTokenType(";");
  IElementType SINGLE_QUOTE_STRING_LITERAL = new FlexibleSearchTokenType("SINGLE_QUOTE_STRING_LITERAL");
  IElementType STRING = new FlexibleSearchTokenType("STRING");
  IElementType TABLE_NAME_IDENTIFIER = new FlexibleSearchTokenType("TABLE_NAME_IDENTIFIER");
  IElementType THEN = new FlexibleSearchTokenType("THEN");
  IElementType UNDERSCORE = new FlexibleSearchTokenType("_");
  IElementType UNION = new FlexibleSearchTokenType("UNION");
  IElementType USING = new FlexibleSearchTokenType("USING");
  IElementType WHEN = new FlexibleSearchTokenType("WHEN");
  IElementType WHERE = new FlexibleSearchTokenType("WHERE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AND_EXPRESSION) {
        return new FlexibleSearchAndExpressionImpl(node);
      }
      else if (type == BETWEEN_EXPRESSION) {
        return new FlexibleSearchBetweenExpressionImpl(node);
      }
      else if (type == BIND_PARAMETER) {
        return new FlexibleSearchBindParameterImpl(node);
      }
      else if (type == BIT_EXPRESSION) {
        return new FlexibleSearchBitExpressionImpl(node);
      }
      else if (type == CASE_EXPRESSION) {
        return new FlexibleSearchCaseExpressionImpl(node);
      }
      else if (type == CAST_EXPRESSION) {
        return new FlexibleSearchCastExpressionImpl(node);
      }
      else if (type == COLUMN_ALIAS_NAME) {
        return new FlexibleSearchColumnAliasNameImpl(node);
      }
      else if (type == COLUMN_NAME) {
        return new FlexibleSearchColumnNameImpl(node);
      }
      else if (type == COLUMN_REF_EXPRESSION) {
        return new FlexibleSearchColumnRefExpressionImpl(node);
      }
      else if (type == COMPARISON_EXPRESSION) {
        return new FlexibleSearchComparisonExpressionImpl(node);
      }
      else if (type == COMPOUND_OPERATOR) {
        return new FlexibleSearchCompoundOperatorImpl(node);
      }
      else if (type == CONCAT_EXPRESSION) {
        return new FlexibleSearchConcatExpressionImpl(node);
      }
      else if (type == DEFINED_TABLE_NAME) {
        return new FlexibleSearchDefinedTableNameImpl(node);
      }
      else if (type == EQUIVALENCE_EXPRESSION) {
        return new FlexibleSearchEquivalenceExpressionImpl(node);
      }
      else if (type == EXISTS_EXPRESSION) {
        return new FlexibleSearchExistsExpressionImpl(node);
      }
      else if (type == EXPRESSION) {
        return new FlexibleSearchExpressionImpl(node);
      }
      else if (type == FROM_CLAUSE) {
        return new FlexibleSearchFromClauseImpl(node);
      }
      else if (type == FROM_TABLE) {
        return new FlexibleSearchFromTableImpl(node);
      }
      else if (type == GROUP_BY_CLAUSE) {
        return new FlexibleSearchGroupByClauseImpl(node);
      }
      else if (type == IN_EXPRESSION) {
        return new FlexibleSearchInExpressionImpl(node);
      }
      else if (type == ISNULL_EXPRESSION) {
        return new FlexibleSearchIsnullExpressionImpl(node);
      }
      else if (type == JOIN_CONSTRAINT) {
        return new FlexibleSearchJoinConstraintImpl(node);
      }
      else if (type == JOIN_OPERATOR) {
        return new FlexibleSearchJoinOperatorImpl(node);
      }
      else if (type == LIKE_EXPRESSION) {
        return new FlexibleSearchLikeExpressionImpl(node);
      }
      else if (type == LIMIT_CLAUSE) {
        return new FlexibleSearchLimitClauseImpl(node);
      }
      else if (type == LITERAL_EXPRESSION) {
        return new FlexibleSearchLiteralExpressionImpl(node);
      }
      else if (type == MUL_EXPRESSION) {
        return new FlexibleSearchMulExpressionImpl(node);
      }
      else if (type == ORDERING_TERM) {
        return new FlexibleSearchOrderingTermImpl(node);
      }
      else if (type == ORDER_CLAUSE) {
        return new FlexibleSearchOrderClauseImpl(node);
      }
      else if (type == OR_EXPRESSION) {
        return new FlexibleSearchOrExpressionImpl(node);
      }
      else if (type == PAREN_EXPRESSION) {
        return new FlexibleSearchParenExpressionImpl(node);
      }
      else if (type == RESULT_COLUMN) {
        return new FlexibleSearchResultColumnImpl(node);
      }
      else if (type == RESULT_COLUMNS) {
        return new FlexibleSearchResultColumnsImpl(node);
      }
      else if (type == SELECTED_TABLE_NAME) {
        return new FlexibleSearchSelectedTableNameImpl(node);
      }
      else if (type == SELECT_CORE) {
        return new FlexibleSearchSelectCoreImpl(node);
      }
      else if (type == SELECT_CORE_SELECT) {
        return new FlexibleSearchSelectCoreSelectImpl(node);
      }
      else if (type == SELECT_STATEMENT) {
        return new FlexibleSearchSelectStatementImpl(node);
      }
      else if (type == SELECT_SUBQUERY) {
        return new FlexibleSearchSelectSubqueryImpl(node);
      }
      else if (type == SIGNED_NUMBER) {
        return new FlexibleSearchSignedNumberImpl(node);
      }
      else if (type == TABLE_ALIAS_NAME) {
        return new FlexibleSearchTableAliasNameImpl(node);
      }
      else if (type == TABLE_OR_SUBQUERY) {
        return new FlexibleSearchTableOrSubqueryImpl(node);
      }
      else if (type == TYPE_NAME) {
        return new FlexibleSearchTypeNameImpl(node);
      }
      else if (type == UNARY_EXPRESSION) {
        return new FlexibleSearchUnaryExpressionImpl(node);
      }
      else if (type == WHERE_CLAUSE) {
        return new FlexibleSearchWhereClauseImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
