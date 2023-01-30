// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.impl.*;

public interface FlexibleSearchTypes {

  IElementType AGGREGATE_FUNCTION = new FlexibleSearchElementType("AGGREGATE_FUNCTION");
  IElementType BETWEEN_PREDICATE = new FlexibleSearchElementType("BETWEEN_PREDICATE");
  IElementType BOOLEAN_FACTOR = new FlexibleSearchElementType("BOOLEAN_FACTOR");
  IElementType BOOLEAN_PREDICAND = new FlexibleSearchElementType("BOOLEAN_PREDICAND");
  IElementType BOOLEAN_PRIMARY = new FlexibleSearchElementType("BOOLEAN_PRIMARY");
  IElementType BOOLEAN_TERM = new FlexibleSearchElementType("BOOLEAN_TERM");
  IElementType BOOLEAN_TEST = new FlexibleSearchElementType("BOOLEAN_TEST");
  IElementType BOOLEAN_VALUE_EXPRESSION = new FlexibleSearchElementType("BOOLEAN_VALUE_EXPRESSION");
  IElementType CHARACTER_LIKE_PREDICATE = new FlexibleSearchElementType("CHARACTER_LIKE_PREDICATE");
  IElementType CHARACTER_PATTERN = new FlexibleSearchElementType("CHARACTER_PATTERN");
  IElementType CHARACTER_STRING_LITERAL = new FlexibleSearchElementType("CHARACTER_STRING_LITERAL");
  IElementType CHARACTER_SUBSTRING_FUNCTION = new FlexibleSearchElementType("CHARACTER_SUBSTRING_FUNCTION");
  IElementType CHARACTER_VALUE_FUNCTION = new FlexibleSearchElementType("CHARACTER_VALUE_FUNCTION");
  IElementType COLUMN_LOCALIZATION = new FlexibleSearchElementType("COLUMN_LOCALIZATION");
  IElementType COLUMN_REFERENCE = new FlexibleSearchElementType("COLUMN_REFERENCE");
  IElementType COMMON_VALUE_EXPRESSION = new FlexibleSearchElementType("COMMON_VALUE_EXPRESSION");
  IElementType COMP_OP = new FlexibleSearchElementType("COMP_OP");
  IElementType CORRELATION_NAME = new FlexibleSearchElementType("CORRELATION_NAME");
  IElementType DERIVED_COLUMN = new FlexibleSearchElementType("DERIVED_COLUMN");
  IElementType EXISTS_PREDICATE = new FlexibleSearchElementType("EXISTS_PREDICATE");
  IElementType FROM_CLAUSE = new FlexibleSearchElementType("FROM_CLAUSE");
  IElementType GENERAL_LITERAL = new FlexibleSearchElementType("GENERAL_LITERAL");
  IElementType GENERAL_SET_FUNCTION = new FlexibleSearchElementType("GENERAL_SET_FUNCTION");
  IElementType GROUPING_COLUMN_REFERENCE = new FlexibleSearchElementType("GROUPING_COLUMN_REFERENCE");
  IElementType GROUPING_COLUMN_REFERENCE_LIST = new FlexibleSearchElementType("GROUPING_COLUMN_REFERENCE_LIST");
  IElementType GROUPING_ELEMENT = new FlexibleSearchElementType("GROUPING_ELEMENT");
  IElementType GROUPING_ELEMENT_LIST = new FlexibleSearchElementType("GROUPING_ELEMENT_LIST");
  IElementType GROUP_BY_CLAUSE = new FlexibleSearchElementType("GROUP_BY_CLAUSE");
  IElementType IN_PREDICATE = new FlexibleSearchElementType("IN_PREDICATE");
  IElementType JOINED_TABLE = new FlexibleSearchElementType("JOINED_TABLE");
  IElementType JOIN_CONDITION = new FlexibleSearchElementType("JOIN_CONDITION");
  IElementType JOIN_SPECIFICATION = new FlexibleSearchElementType("JOIN_SPECIFICATION");
  IElementType JOIN_TYPE = new FlexibleSearchElementType("JOIN_TYPE");
  IElementType LIKE_PREDICATE = new FlexibleSearchElementType("LIKE_PREDICATE");
  IElementType NULL_ORDERING = new FlexibleSearchElementType("NULL_ORDERING");
  IElementType NULL_PREDICATE = new FlexibleSearchElementType("NULL_PREDICATE");
  IElementType ORDERING_SPECIFICATION = new FlexibleSearchElementType("ORDERING_SPECIFICATION");
  IElementType ORDER_BY_CLAUSE = new FlexibleSearchElementType("ORDER_BY_CLAUSE");
  IElementType ORDINARY_GROUPING_SET = new FlexibleSearchElementType("ORDINARY_GROUPING_SET");
  IElementType PARAMETER_REFERENCE = new FlexibleSearchElementType("PARAMETER_REFERENCE");
  IElementType PREDICATE = new FlexibleSearchElementType("PREDICATE");
  IElementType QUERY_SPECIFICATION = new FlexibleSearchElementType("QUERY_SPECIFICATION");
  IElementType ROW_VALUE_PREDICAND = new FlexibleSearchElementType("ROW_VALUE_PREDICAND");
  IElementType SEARCH_CONDITION = new FlexibleSearchElementType("SEARCH_CONDITION");
  IElementType SELECT_LIST = new FlexibleSearchElementType("SELECT_LIST");
  IElementType SELECT_SUBLIST = new FlexibleSearchElementType("SELECT_SUBLIST");
  IElementType SET_FUNCTION_TYPE = new FlexibleSearchElementType("SET_FUNCTION_TYPE");
  IElementType SET_QUANTIFIER = new FlexibleSearchElementType("SET_QUANTIFIER");
  IElementType SORT_KEY = new FlexibleSearchElementType("SORT_KEY");
  IElementType SORT_SPECIFICATION = new FlexibleSearchElementType("SORT_SPECIFICATION");
  IElementType SORT_SPECIFICATION_LIST = new FlexibleSearchElementType("SORT_SPECIFICATION_LIST");
  IElementType STRING_VALUE_EXPRESSION = new FlexibleSearchElementType("STRING_VALUE_EXPRESSION");
  IElementType STRING_VALUE_FUNCTION = new FlexibleSearchElementType("STRING_VALUE_FUNCTION");
  IElementType SUBQUERY = new FlexibleSearchElementType("SUBQUERY");
  IElementType TABLE_EXPRESSION = new FlexibleSearchElementType("TABLE_EXPRESSION");
  IElementType TABLE_NAME = new FlexibleSearchElementType("TABLE_NAME");
  IElementType TABLE_PRIMARY = new FlexibleSearchElementType("TABLE_PRIMARY");
  IElementType TABLE_REFERENCE = new FlexibleSearchElementType("TABLE_REFERENCE");
  IElementType TABLE_REFERENCE_LIST = new FlexibleSearchElementType("TABLE_REFERENCE_LIST");
  IElementType TABLE_SUBQUERY = new FlexibleSearchElementType("TABLE_SUBQUERY");
  IElementType TRUTH_VALUE = new FlexibleSearchElementType("TRUTH_VALUE");
  IElementType VALUE_EXPRESSION = new FlexibleSearchElementType("VALUE_EXPRESSION");
  IElementType WHERE_CLAUSE = new FlexibleSearchElementType("WHERE_CLAUSE");

  IElementType ALL = new FlexibleSearchTokenType("ALL");
  IElementType AND = new FlexibleSearchTokenType("AND");
  IElementType ANY = new FlexibleSearchTokenType("ANY");
  IElementType AS = new FlexibleSearchTokenType("AS");
  IElementType ASC = new FlexibleSearchTokenType("ASC");
  IElementType ASTERISK = new FlexibleSearchTokenType("*");
  IElementType AVG = new FlexibleSearchTokenType("AVG");
  IElementType BETWEEN = new FlexibleSearchTokenType("BETWEEN");
  IElementType BY = new FlexibleSearchTokenType("BY");
  IElementType COLON = new FlexibleSearchTokenType(":");
  IElementType COLUMN_REFERENCE_IDENTIFIER = new FlexibleSearchTokenType("COLUMN_REFERENCE_IDENTIFIER");
  IElementType COMMA = new FlexibleSearchTokenType(",");
  IElementType CONCAT = new FlexibleSearchTokenType("CONCAT");
  IElementType COUNT = new FlexibleSearchTokenType("COUNT");
  IElementType DESC = new FlexibleSearchTokenType("DESC");
  IElementType DISTINCT = new FlexibleSearchTokenType("DISTINCT");
  IElementType DOT = new FlexibleSearchTokenType(".");
  IElementType EQUALS_OPERATOR = new FlexibleSearchTokenType("EQUALS_OPERATOR");
  IElementType EVERY = new FlexibleSearchTokenType("EVERY");
  IElementType EXCLAMATION_MARK = new FlexibleSearchTokenType("!");
  IElementType EXISTS = new FlexibleSearchTokenType("EXISTS");
  IElementType FALSE = new FlexibleSearchTokenType("FALSE");
  IElementType FIRST = new FlexibleSearchTokenType("FIRST");
  IElementType FROM = new FlexibleSearchTokenType("FROM");
  IElementType GREATER_THAN_OPERATOR = new FlexibleSearchTokenType(">");
  IElementType GREATER_THAN_OR_EQUALS_OPERATOR = new FlexibleSearchTokenType("GREATER_THAN_OR_EQUALS_OPERATOR");
  IElementType GROUP = new FlexibleSearchTokenType("GROUP");
  IElementType IDENTIFIER = new FlexibleSearchTokenType("IDENTIFIER");
  IElementType IN = new FlexibleSearchTokenType("IN");
  IElementType IS = new FlexibleSearchTokenType("IS");
  IElementType JOIN = new FlexibleSearchTokenType("JOIN");
  IElementType JOIN_TYPE_1_0 = new FlexibleSearchTokenType("join_type_1_0");
  IElementType LAST = new FlexibleSearchTokenType("LAST");
  IElementType LEFT = new FlexibleSearchTokenType("LEFT");
  IElementType LEFT_BRACE = new FlexibleSearchTokenType("LEFT_BRACE");
  IElementType LEFT_BRACKET = new FlexibleSearchTokenType("[");
  IElementType LEFT_DOUBLE_BRACE = new FlexibleSearchTokenType("LEFT_DOUBLE_BRACE");
  IElementType LEFT_PAREN = new FlexibleSearchTokenType("(");
  IElementType LESS_THAN_OPERATOR = new FlexibleSearchTokenType("<");
  IElementType LESS_THAN_OR_EQUALS_OPERATOR = new FlexibleSearchTokenType("LESS_THAN_OR_EQUALS_OPERATOR");
  IElementType LIKE = new FlexibleSearchTokenType("LIKE");
  IElementType LINE_TERMINATOR = new FlexibleSearchTokenType("LINE_TERMINATOR");
  IElementType MAX = new FlexibleSearchTokenType("MAX");
  IElementType MIN = new FlexibleSearchTokenType("MIN");
  IElementType MINUS_SIGN = new FlexibleSearchTokenType("-");
  IElementType NOT = new FlexibleSearchTokenType("NOT");
  IElementType NOT_EQUALS_OPERATOR = new FlexibleSearchTokenType("<>");
  IElementType NULL = new FlexibleSearchTokenType("NULL");
  IElementType NULLS = new FlexibleSearchTokenType("NULLS");
  IElementType NUMBER = new FlexibleSearchTokenType("NUMBER");
  IElementType ON = new FlexibleSearchTokenType("ON");
  IElementType OR = new FlexibleSearchTokenType("OR");
  IElementType ORDER = new FlexibleSearchTokenType("ORDER");
  IElementType PARAMETER_IDENTIFIER = new FlexibleSearchTokenType("PARAMETER_IDENTIFIER");
  IElementType PERCENT = new FlexibleSearchTokenType("%");
  IElementType PLUS_SIGN = new FlexibleSearchTokenType("+");
  IElementType QUESTION_MARK = new FlexibleSearchTokenType("?");
  IElementType QUOTE = new FlexibleSearchTokenType("'");
  IElementType RIGHT_BRACE = new FlexibleSearchTokenType("RIGHT_BRACE");
  IElementType RIGHT_BRACKET = new FlexibleSearchTokenType("]");
  IElementType RIGHT_DOUBLE_BRACE = new FlexibleSearchTokenType("RIGHT_DOUBLE_BRACE");
  IElementType RIGHT_PAREN = new FlexibleSearchTokenType(")");
  IElementType SELECT = new FlexibleSearchTokenType("SELECT");
  IElementType SEMICOLON = new FlexibleSearchTokenType(";");
  IElementType SOME = new FlexibleSearchTokenType("SOME");
  IElementType SPACE = new FlexibleSearchTokenType("SPACE");
  IElementType STRING = new FlexibleSearchTokenType("STRING");
  IElementType SUM = new FlexibleSearchTokenType("SUM");
  IElementType TABLE_NAME_IDENTIFIER = new FlexibleSearchTokenType("TABLE_NAME_IDENTIFIER");
  IElementType TRUE = new FlexibleSearchTokenType("TRUE");
  IElementType UNDERSCORE = new FlexibleSearchTokenType("_");
  IElementType UNION = new FlexibleSearchTokenType("UNION");
  IElementType WHERE = new FlexibleSearchTokenType("WHERE");
  IElementType WHITE_SPACE = new FlexibleSearchTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == AGGREGATE_FUNCTION) {
        return new FlexibleSearchAggregateFunctionImpl(node);
      }
      else if (type == BETWEEN_PREDICATE) {
        return new FlexibleSearchBetweenPredicateImpl(node);
      }
      else if (type == BOOLEAN_FACTOR) {
        return new FlexibleSearchBooleanFactorImpl(node);
      }
      else if (type == BOOLEAN_PREDICAND) {
        return new FlexibleSearchBooleanPredicandImpl(node);
      }
      else if (type == BOOLEAN_PRIMARY) {
        return new FlexibleSearchBooleanPrimaryImpl(node);
      }
      else if (type == BOOLEAN_TERM) {
        return new FlexibleSearchBooleanTermImpl(node);
      }
      else if (type == BOOLEAN_TEST) {
        return new FlexibleSearchBooleanTestImpl(node);
      }
      else if (type == BOOLEAN_VALUE_EXPRESSION) {
        return new FlexibleSearchBooleanValueExpressionImpl(node);
      }
      else if (type == CHARACTER_LIKE_PREDICATE) {
        return new FlexibleSearchCharacterLikePredicateImpl(node);
      }
      else if (type == CHARACTER_PATTERN) {
        return new FlexibleSearchCharacterPatternImpl(node);
      }
      else if (type == CHARACTER_STRING_LITERAL) {
        return new FlexibleSearchCharacterStringLiteralImpl(node);
      }
      else if (type == CHARACTER_SUBSTRING_FUNCTION) {
        return new FlexibleSearchCharacterSubstringFunctionImpl(node);
      }
      else if (type == CHARACTER_VALUE_FUNCTION) {
        return new FlexibleSearchCharacterValueFunctionImpl(node);
      }
      else if (type == COLUMN_LOCALIZATION) {
        return new FlexibleSearchColumnLocalizationImpl(node);
      }
      else if (type == COLUMN_REFERENCE) {
        return new FlexibleSearchColumnReferenceImpl(node);
      }
      else if (type == COMMON_VALUE_EXPRESSION) {
        return new FlexibleSearchCommonValueExpressionImpl(node);
      }
      else if (type == COMP_OP) {
        return new FlexibleSearchCompOpImpl(node);
      }
      else if (type == CORRELATION_NAME) {
        return new FlexibleSearchCorrelationNameImpl(node);
      }
      else if (type == DERIVED_COLUMN) {
        return new FlexibleSearchDerivedColumnImpl(node);
      }
      else if (type == EXISTS_PREDICATE) {
        return new FlexibleSearchExistsPredicateImpl(node);
      }
      else if (type == FROM_CLAUSE) {
        return new FlexibleSearchFromClauseImpl(node);
      }
      else if (type == GENERAL_LITERAL) {
        return new FlexibleSearchGeneralLiteralImpl(node);
      }
      else if (type == GENERAL_SET_FUNCTION) {
        return new FlexibleSearchGeneralSetFunctionImpl(node);
      }
      else if (type == GROUPING_COLUMN_REFERENCE) {
        return new FlexibleSearchGroupingColumnReferenceImpl(node);
      }
      else if (type == GROUPING_COLUMN_REFERENCE_LIST) {
        return new FlexibleSearchGroupingColumnReferenceListImpl(node);
      }
      else if (type == GROUPING_ELEMENT) {
        return new FlexibleSearchGroupingElementImpl(node);
      }
      else if (type == GROUPING_ELEMENT_LIST) {
        return new FlexibleSearchGroupingElementListImpl(node);
      }
      else if (type == GROUP_BY_CLAUSE) {
        return new FlexibleSearchGroupByClauseImpl(node);
      }
      else if (type == IN_PREDICATE) {
        return new FlexibleSearchInPredicateImpl(node);
      }
      else if (type == JOINED_TABLE) {
        return new FlexibleSearchJoinedTableImpl(node);
      }
      else if (type == JOIN_CONDITION) {
        return new FlexibleSearchJoinConditionImpl(node);
      }
      else if (type == JOIN_SPECIFICATION) {
        return new FlexibleSearchJoinSpecificationImpl(node);
      }
      else if (type == JOIN_TYPE) {
        return new FlexibleSearchJoinTypeImpl(node);
      }
      else if (type == LIKE_PREDICATE) {
        return new FlexibleSearchLikePredicateImpl(node);
      }
      else if (type == NULL_ORDERING) {
        return new FlexibleSearchNullOrderingImpl(node);
      }
      else if (type == NULL_PREDICATE) {
        return new FlexibleSearchNullPredicateImpl(node);
      }
      else if (type == ORDERING_SPECIFICATION) {
        return new FlexibleSearchOrderingSpecificationImpl(node);
      }
      else if (type == ORDER_BY_CLAUSE) {
        return new FlexibleSearchOrderByClauseImpl(node);
      }
      else if (type == ORDINARY_GROUPING_SET) {
        return new FlexibleSearchOrdinaryGroupingSetImpl(node);
      }
      else if (type == PARAMETER_REFERENCE) {
        return new FlexibleSearchParameterReferenceImpl(node);
      }
      else if (type == PREDICATE) {
        return new FlexibleSearchPredicateImpl(node);
      }
      else if (type == QUERY_SPECIFICATION) {
        return new FlexibleSearchQuerySpecificationImpl(node);
      }
      else if (type == ROW_VALUE_PREDICAND) {
        return new FlexibleSearchRowValuePredicandImpl(node);
      }
      else if (type == SEARCH_CONDITION) {
        return new FlexibleSearchSearchConditionImpl(node);
      }
      else if (type == SELECT_LIST) {
        return new FlexibleSearchSelectListImpl(node);
      }
      else if (type == SELECT_SUBLIST) {
        return new FlexibleSearchSelectSublistImpl(node);
      }
      else if (type == SET_FUNCTION_TYPE) {
        return new FlexibleSearchSetFunctionTypeImpl(node);
      }
      else if (type == SET_QUANTIFIER) {
        return new FlexibleSearchSetQuantifierImpl(node);
      }
      else if (type == SORT_KEY) {
        return new FlexibleSearchSortKeyImpl(node);
      }
      else if (type == SORT_SPECIFICATION) {
        return new FlexibleSearchSortSpecificationImpl(node);
      }
      else if (type == SORT_SPECIFICATION_LIST) {
        return new FlexibleSearchSortSpecificationListImpl(node);
      }
      else if (type == STRING_VALUE_EXPRESSION) {
        return new FlexibleSearchStringValueExpressionImpl(node);
      }
      else if (type == STRING_VALUE_FUNCTION) {
        return new FlexibleSearchStringValueFunctionImpl(node);
      }
      else if (type == SUBQUERY) {
        return new FlexibleSearchSubqueryImpl(node);
      }
      else if (type == TABLE_EXPRESSION) {
        return new FlexibleSearchTableExpressionImpl(node);
      }
      else if (type == TABLE_NAME) {
        return new FlexibleSearchTableNameImpl(node);
      }
      else if (type == TABLE_PRIMARY) {
        return new FlexibleSearchTablePrimaryImpl(node);
      }
      else if (type == TABLE_REFERENCE) {
        return new FlexibleSearchTableReferenceImpl(node);
      }
      else if (type == TABLE_REFERENCE_LIST) {
        return new FlexibleSearchTableReferenceListImpl(node);
      }
      else if (type == TABLE_SUBQUERY) {
        return new FlexibleSearchTableSubqueryImpl(node);
      }
      else if (type == TRUTH_VALUE) {
        return new FlexibleSearchTruthValueImpl(node);
      }
      else if (type == VALUE_EXPRESSION) {
        return new FlexibleSearchValueExpressionImpl(node);
      }
      else if (type == WHERE_CLAUSE) {
        return new FlexibleSearchWhereClauseImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
