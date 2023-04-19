// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchElementType;
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchTokenType;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl.*;

public interface FlexibleSearchTypes {

  IElementType AND_EXPRESSION = new FlexibleSearchElementType("AND_EXPRESSION");
  IElementType BETWEEN_EXPRESSION = new FlexibleSearchElementType("BETWEEN_EXPRESSION");
  IElementType BIND_PARAMETER = new FlexibleSearchElementType("BIND_PARAMETER");
  IElementType BIT_EXPRESSION = new FlexibleSearchElementType("BIT_EXPRESSION");
  IElementType CASE_EXPRESSION = new FlexibleSearchElementType("CASE_EXPRESSION");
  IElementType CAST_EXPRESSION = new FlexibleSearchElementType("CAST_EXPRESSION");
  IElementType COLUMN_ALIAS_NAME = new FlexibleSearchElementType("COLUMN_ALIAS_NAME");
  IElementType COLUMN_LOCALIZED = new FlexibleSearchElementType("COLUMN_LOCALIZED");
  IElementType COLUMN_NAME = new FlexibleSearchElementType("COLUMN_NAME");
  IElementType COLUMN_OUTER_JOIN = new FlexibleSearchElementType("COLUMN_OUTER_JOIN");
  IElementType COLUMN_REF_EXPRESSION = new FlexibleSearchElementType("COLUMN_REF_EXPRESSION");
  IElementType COLUMN_REF_Y_EXPRESSION = new FlexibleSearchElementType("COLUMN_REF_Y_EXPRESSION");
  IElementType COLUMN_SEPARATOR = new FlexibleSearchElementType("COLUMN_SEPARATOR");
  IElementType COMPARISON_EXPRESSION = new FlexibleSearchElementType("COMPARISON_EXPRESSION");
  IElementType COMPOUND_OPERATOR = new FlexibleSearchElementType("COMPOUND_OPERATOR");
  IElementType CONCAT_EXPRESSION = new FlexibleSearchElementType("CONCAT_EXPRESSION");
  IElementType DEFINED_TABLE_NAME = new FlexibleSearchElementType("DEFINED_TABLE_NAME");
  IElementType EQUIVALENCE_EXPRESSION = new FlexibleSearchElementType("EQUIVALENCE_EXPRESSION");
  IElementType EXISTS_EXPRESSION = new FlexibleSearchElementType("EXISTS_EXPRESSION");
  IElementType EXPRESSION = new FlexibleSearchElementType("EXPRESSION");
  IElementType FROM_CLAUSE = new FlexibleSearchElementType("FROM_CLAUSE");
  IElementType FROM_CLAUSE_EXPRESSION = new FlexibleSearchElementType("FROM_CLAUSE_EXPRESSION");
  IElementType FROM_CLAUSE_SELECT = new FlexibleSearchElementType("FROM_CLAUSE_SELECT");
  IElementType FROM_CLAUSE_SELECT_QUERY = new FlexibleSearchElementType("FROM_CLAUSE_SELECT_QUERY");
  IElementType FROM_CLAUSE_SIMPLE = new FlexibleSearchElementType("FROM_CLAUSE_SIMPLE");
  IElementType FROM_CLAUSE_SUBQUERIES = new FlexibleSearchElementType("FROM_CLAUSE_SUBQUERIES");
  IElementType FROM_TABLE = new FlexibleSearchElementType("FROM_TABLE");
  IElementType FUNCTION_CALL_EXPRESSION = new FlexibleSearchElementType("FUNCTION_CALL_EXPRESSION");
  IElementType FUNCTION_NAME = new FlexibleSearchElementType("FUNCTION_NAME");
  IElementType GROUP_BY_CLAUSE = new FlexibleSearchElementType("GROUP_BY_CLAUSE");
  IElementType IN_EXPRESSION = new FlexibleSearchElementType("IN_EXPRESSION");
  IElementType ISNULL_EXPRESSION = new FlexibleSearchElementType("ISNULL_EXPRESSION");
  IElementType JOIN_CONSTRAINT = new FlexibleSearchElementType("JOIN_CONSTRAINT");
  IElementType JOIN_OPERATOR = new FlexibleSearchElementType("JOIN_OPERATOR");
  IElementType LIKE_EXPRESSION = new FlexibleSearchElementType("LIKE_EXPRESSION");
  IElementType LIMIT_CLAUSE = new FlexibleSearchElementType("LIMIT_CLAUSE");
  IElementType LITERAL_EXPRESSION = new FlexibleSearchElementType("LITERAL_EXPRESSION");
  IElementType MUL_EXPRESSION = new FlexibleSearchElementType("MUL_EXPRESSION");
  IElementType MYSQL_FUNCTION_EXPRESSION = new FlexibleSearchElementType("MYSQL_FUNCTION_EXPRESSION");
  IElementType ORDERING_TERM = new FlexibleSearchElementType("ORDERING_TERM");
  IElementType ORDER_CLAUSE = new FlexibleSearchElementType("ORDER_CLAUSE");
  IElementType OR_EXPRESSION = new FlexibleSearchElementType("OR_EXPRESSION");
  IElementType PAREN_EXPRESSION = new FlexibleSearchElementType("PAREN_EXPRESSION");
  IElementType RESULT_COLUMN = new FlexibleSearchElementType("RESULT_COLUMN");
  IElementType RESULT_COLUMNS = new FlexibleSearchElementType("RESULT_COLUMNS");
  IElementType SELECTED_TABLE_NAME = new FlexibleSearchElementType("SELECTED_TABLE_NAME");
  IElementType SELECT_CORE_SELECT = new FlexibleSearchElementType("SELECT_CORE_SELECT");
  IElementType SELECT_STATEMENT = new FlexibleSearchElementType("SELECT_STATEMENT");
  IElementType SELECT_SUBQUERY = new FlexibleSearchElementType("SELECT_SUBQUERY");
  IElementType SELECT_SUBQUERY_COMBINED = new FlexibleSearchElementType("SELECT_SUBQUERY_COMBINED");
  IElementType SIGNED_NUMBER = new FlexibleSearchElementType("SIGNED_NUMBER");
  IElementType TABLE_ALIAS_NAME = new FlexibleSearchElementType("TABLE_ALIAS_NAME");
  IElementType TABLE_OR_SUBQUERY = new FlexibleSearchElementType("TABLE_OR_SUBQUERY");
  IElementType TYPE_NAME = new FlexibleSearchElementType("TYPE_NAME");
  IElementType UNARY_EXPRESSION = new FlexibleSearchElementType("UNARY_EXPRESSION");
  IElementType WHERE_CLAUSE = new FlexibleSearchElementType("WHERE_CLAUSE");
  IElementType Y_FROM_CLAUSE = new FlexibleSearchElementType("Y_FROM_CLAUSE");

  IElementType ALL = new FlexibleSearchTokenType("ALL");
  IElementType AMP = new FlexibleSearchTokenType("&");
  IElementType AND = new FlexibleSearchTokenType("AND");
  IElementType AS = new FlexibleSearchTokenType("AS");
  IElementType ASC = new FlexibleSearchTokenType("ASC");
  IElementType ASTERISK = new FlexibleSearchTokenType("ASTERISK");
  IElementType BACKTICK_LITERAL = new FlexibleSearchTokenType("BACKTICK_LITERAL");
  IElementType BAR = new FlexibleSearchTokenType("|");
  IElementType BETWEEN = new FlexibleSearchTokenType("BETWEEN");
  IElementType BRACKET_LITERAL = new FlexibleSearchTokenType("BRACKET_LITERAL");
  IElementType BY = new FlexibleSearchTokenType("BY");
  IElementType CASE = new FlexibleSearchTokenType("CASE");
  IElementType CAST = new FlexibleSearchTokenType("CAST");
  IElementType COLON = new FlexibleSearchTokenType(":");
  IElementType COMMA = new FlexibleSearchTokenType(",");
  IElementType COMMENT = new FlexibleSearchTokenType("COMMENT");
  IElementType CONCAT = new FlexibleSearchTokenType("||");
  IElementType CROSS = new FlexibleSearchTokenType("CROSS");
  IElementType CURRENT_DATE = new FlexibleSearchTokenType("CURRENT_DATE");
  IElementType CURRENT_TIME = new FlexibleSearchTokenType("CURRENT_TIME");
  IElementType CURRENT_TIMESTAMP = new FlexibleSearchTokenType("CURRENT_TIMESTAMP");
  IElementType DESC = new FlexibleSearchTokenType("DESC");
  IElementType DISTINCT = new FlexibleSearchTokenType("DISTINCT");
  IElementType DIV = new FlexibleSearchTokenType("/");
  IElementType DOT = new FlexibleSearchTokenType(".");
  IElementType DOUBLE_QUOTE_STRING_LITERAL = new FlexibleSearchTokenType("DOUBLE_QUOTE_STRING_LITERAL");
  IElementType ELSE = new FlexibleSearchTokenType("ELSE");
  IElementType END = new FlexibleSearchTokenType("END");
  IElementType EQ = new FlexibleSearchTokenType("=");
  IElementType EQEQ = new FlexibleSearchTokenType("==");
  IElementType ESCAPE = new FlexibleSearchTokenType("ESCAPE");
  IElementType EXCLAMATION_MARK = new FlexibleSearchTokenType("!");
  IElementType EXISTS = new FlexibleSearchTokenType("EXISTS");
  IElementType FROM = new FlexibleSearchTokenType("FROM");
  IElementType FULL = new FlexibleSearchTokenType("FULL");
  IElementType GLOB = new FlexibleSearchTokenType("GLOB");
  IElementType GROUP = new FlexibleSearchTokenType("GROUP");
  IElementType GT = new FlexibleSearchTokenType(">");
  IElementType GTE = new FlexibleSearchTokenType(">=");
  IElementType HAVING = new FlexibleSearchTokenType("HAVING");
  IElementType IDENTIFIER = new FlexibleSearchTokenType("IDENTIFIER");
  IElementType IN = new FlexibleSearchTokenType("IN");
  IElementType INNER = new FlexibleSearchTokenType("INNER");
  IElementType INTERVAL = new FlexibleSearchTokenType("INTERVAL");
  IElementType IS = new FlexibleSearchTokenType("IS");
  IElementType JOIN = new FlexibleSearchTokenType("JOIN");
  IElementType LBRACE = new FlexibleSearchTokenType("{");
  IElementType LBRACKET = new FlexibleSearchTokenType("[");
  IElementType LDBRACE = new FlexibleSearchTokenType("{{");
  IElementType LEFT = new FlexibleSearchTokenType("LEFT");
  IElementType LIKE = new FlexibleSearchTokenType("LIKE");
  IElementType LIMIT = new FlexibleSearchTokenType("LIMIT");
  IElementType LINE_COMMENT = new FlexibleSearchTokenType("LINE_COMMENT");
  IElementType LPAREN = new FlexibleSearchTokenType("(");
  IElementType LT = new FlexibleSearchTokenType("<");
  IElementType LTE = new FlexibleSearchTokenType("<=");
  IElementType MATCH = new FlexibleSearchTokenType("MATCH");
  IElementType MINUS = new FlexibleSearchTokenType("-");
  IElementType MOD = new FlexibleSearchTokenType("%");
  IElementType NAMED_PARAMETER = new FlexibleSearchTokenType("NAMED_PARAMETER");
  IElementType NOT = new FlexibleSearchTokenType("NOT");
  IElementType NOT_EQ = new FlexibleSearchTokenType("!=");
  IElementType NULL = new FlexibleSearchTokenType("NULL");
  IElementType NUMBERED_PARAMETER = new FlexibleSearchTokenType("NUMBERED_PARAMETER");
  IElementType NUMERIC_LITERAL = new FlexibleSearchTokenType("NUMERIC_LITERAL");
  IElementType OFFSET = new FlexibleSearchTokenType("OFFSET");
  IElementType ON = new FlexibleSearchTokenType("ON");
  IElementType OR = new FlexibleSearchTokenType("OR");
  IElementType ORDER = new FlexibleSearchTokenType("ORDER");
  IElementType OUTER = new FlexibleSearchTokenType("OUTER");
  IElementType OUTER_JOIN = new FlexibleSearchTokenType(":o");
  IElementType PLUS = new FlexibleSearchTokenType("+");
  IElementType QUESTION_MARK = new FlexibleSearchTokenType("?");
  IElementType RBRACE = new FlexibleSearchTokenType("}");
  IElementType RBRACKET = new FlexibleSearchTokenType("]");
  IElementType RDBRACE = new FlexibleSearchTokenType("}}");
  IElementType REGEXP = new FlexibleSearchTokenType("REGEXP");
  IElementType RIGHT = new FlexibleSearchTokenType("RIGHT");
  IElementType RPAREN = new FlexibleSearchTokenType(")");
  IElementType SELECT = new FlexibleSearchTokenType("SELECT");
  IElementType SEMICOLON = new FlexibleSearchTokenType(";");
  IElementType SHL = new FlexibleSearchTokenType("<<");
  IElementType SHR = new FlexibleSearchTokenType(">>");
  IElementType SINGLE_QUOTE_STRING_LITERAL = new FlexibleSearchTokenType("SINGLE_QUOTE_STRING_LITERAL");
  IElementType STAR = new FlexibleSearchTokenType("*");
  IElementType THEN = new FlexibleSearchTokenType("THEN");
  IElementType TILDE = new FlexibleSearchTokenType("~");
  IElementType UNEQ = new FlexibleSearchTokenType("<>");
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
      else if (type == COLUMN_LOCALIZED) {
        return new FlexibleSearchColumnLocalizedImpl(node);
      }
      else if (type == COLUMN_NAME) {
        return new FlexibleSearchColumnNameImpl(node);
      }
      else if (type == COLUMN_OUTER_JOIN) {
        return new FlexibleSearchColumnOuterJoinImpl(node);
      }
      else if (type == COLUMN_REF_EXPRESSION) {
        return new FlexibleSearchColumnRefExpressionImpl(node);
      }
      else if (type == COLUMN_REF_Y_EXPRESSION) {
        return new FlexibleSearchColumnRefYExpressionImpl(node);
      }
      else if (type == COLUMN_SEPARATOR) {
        return new FlexibleSearchColumnSeparatorImpl(node);
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
      else if (type == FROM_CLAUSE) {
        return new FlexibleSearchFromClauseImpl(node);
      }
      else if (type == FROM_CLAUSE_EXPRESSION) {
        return new FlexibleSearchFromClauseExpressionImpl(node);
      }
      else if (type == FROM_CLAUSE_SELECT) {
        return new FlexibleSearchFromClauseSelectImpl(node);
      }
      else if (type == FROM_CLAUSE_SELECT_QUERY) {
        return new FlexibleSearchFromClauseSelectQueryImpl(node);
      }
      else if (type == FROM_CLAUSE_SIMPLE) {
        return new FlexibleSearchFromClauseSimpleImpl(node);
      }
      else if (type == FROM_CLAUSE_SUBQUERIES) {
        return new FlexibleSearchFromClauseSubqueriesImpl(node);
      }
      else if (type == FROM_TABLE) {
        return new FlexibleSearchFromTableImpl(node);
      }
      else if (type == FUNCTION_CALL_EXPRESSION) {
        return new FlexibleSearchFunctionCallExpressionImpl(node);
      }
      else if (type == FUNCTION_NAME) {
        return new FlexibleSearchFunctionNameImpl(node);
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
      else if (type == MYSQL_FUNCTION_EXPRESSION) {
        return new FlexibleSearchMysqlFunctionExpressionImpl(node);
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
      else if (type == SELECT_CORE_SELECT) {
        return new FlexibleSearchSelectCoreSelectImpl(node);
      }
      else if (type == SELECT_STATEMENT) {
        return new FlexibleSearchSelectStatementImpl(node);
      }
      else if (type == SELECT_SUBQUERY) {
        return new FlexibleSearchSelectSubqueryImpl(node);
      }
      else if (type == SELECT_SUBQUERY_COMBINED) {
        return new FlexibleSearchSelectSubqueryCombinedImpl(node);
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
      else if (type == Y_FROM_CLAUSE) {
        return new FlexibleSearchYFromClauseImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
