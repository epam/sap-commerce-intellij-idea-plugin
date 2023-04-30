/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 *
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
package com.intellij.idea.plugin.hybris.flexibleSearch;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import static com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchParserUtils.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class FlexibleSearchParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(FROM_CLAUSE_EXPR, FROM_CLAUSE_SELECT, Y_FROM_CLAUSE),
    create_token_set_(AND_EXPRESSION, BETWEEN_EXPRESSION, BIT_EXPRESSION, CASE_EXPRESSION,
      CAST_EXPRESSION, COLUMN_REF_EXPRESSION, COLUMN_REF_Y_EXPRESSION, COMPARISON_EXPRESSION,
      CONCAT_EXPRESSION, EQUIVALENCE_EXPRESSION, EXISTS_EXPRESSION, EXPRESSION,
      FUNCTION_CALL_EXPRESSION, IN_EXPRESSION, ISNULL_EXPRESSION, LIKE_EXPRESSION,
      LITERAL_EXPRESSION, MUL_EXPRESSION, MYSQL_FUNCTION_EXPRESSION, OR_EXPRESSION,
      PAREN_EXPRESSION, SUBQUERY_PAREN_EXPRESSION, UNARY_EXPRESSION),
  };

  /* ********************************************************** */
  // selected_table_name column_separator column_name
  static boolean aliased_column_ref_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aliased_column_ref_expression")) return false;
    if (!nextTokenIs(b, "", BACKTICK_LITERAL, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    p = r; // pin = 2
    r = r && column_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NUMBERED_PARAMETER | (NAMED_PARAMETER ( '.' ext_parameter_name)*)
  public static boolean bind_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter")) return false;
    if (!nextTokenIs(b, "<bind parameter>", NAMED_PARAMETER, NUMBERED_PARAMETER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BIND_PARAMETER, "<bind parameter>");
    r = consumeToken(b, NUMBERED_PARAMETER);
    if (!r) r = bind_parameter_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NAMED_PARAMETER ( '.' ext_parameter_name)*
  private static boolean bind_parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAMED_PARAMETER);
    r = r && bind_parameter_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( '.' ext_parameter_name)*
  private static boolean bind_parameter_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!bind_parameter_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "bind_parameter_1_1", c)) break;
    }
    return true;
  }

  // '.' ext_parameter_name
  private static boolean bind_parameter_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && ext_parameter_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // name
  public static boolean column_alias_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_alias_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_ALIAS_NAME, "<column alias name>");
    r = name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean column_localized_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_localized_name")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, COLUMN_LOCALIZED_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // ORDER | IDENTIFIER
  public static boolean column_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_name")) return false;
    if (!nextTokenIs(b, "<column name>", IDENTIFIER, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_NAME, "<column name>");
    r = consumeToken(b, ORDER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OUTER_JOIN
  public static boolean column_outer_join_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_outer_join_name")) return false;
    if (!nextTokenIs(b, OUTER_JOIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OUTER_JOIN);
    exit_section_(b, m, COLUMN_OUTER_JOIN_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // (selected_table_name column_separator)? y_column_name y_column_localized_name? column_outer_join_name?
  static boolean column_ref_y_expression_other(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_other")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = column_ref_y_expression_other_0(b, l + 1);
    r = r && y_column_name(b, l + 1);
    r = r && column_ref_y_expression_other_2(b, l + 1);
    r = r && column_ref_y_expression_other_3(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::column_ref_y_expression_recover);
    return r;
  }

  // (selected_table_name column_separator)?
  private static boolean column_ref_y_expression_other_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_other_0")) return false;
    column_ref_y_expression_other_0_0(b, l + 1);
    return true;
  }

  // selected_table_name column_separator
  private static boolean column_ref_y_expression_other_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_other_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // y_column_localized_name?
  private static boolean column_ref_y_expression_other_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_other_2")) return false;
    y_column_localized_name(b, l + 1);
    return true;
  }

  // column_outer_join_name?
  private static boolean column_ref_y_expression_other_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_other_3")) return false;
    column_outer_join_name(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !'}'
  static boolean column_ref_y_expression_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DOT | COLON
  public static boolean column_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_separator")) return false;
    if (!nextTokenIs(b, "<column separator>", COLON, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_SEPARATOR, "<column separator>");
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // UNION ALL?
  public static boolean compound_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_operator")) return false;
    if (!nextTokenIs(b, UNION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNION);
    r = r && compound_operator_1(b, l + 1);
    exit_section_(b, m, COMPOUND_OPERATOR, r);
    return r;
  }

  // ALL?
  private static boolean compound_operator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_operator_1")) return false;
    consumeToken(b, ALL);
    return true;
  }

  /* ********************************************************** */
  // (ORDER | IDENTIFIER) ('!' | '*')?
  public static boolean defined_table_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name")) return false;
    if (!nextTokenIs(b, "<defined table name>", IDENTIFIER, ORDER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINED_TABLE_NAME, "<defined table name>");
    r = defined_table_name_0(b, l + 1);
    p = r; // pin = 1
    r = r && defined_table_name_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ORDER | IDENTIFIER
  private static boolean defined_table_name_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name_0")) return false;
    boolean r;
    r = consumeToken(b, ORDER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    return r;
  }

  // ('!' | '*')?
  private static boolean defined_table_name_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name_1")) return false;
    defined_table_name_1_0(b, l + 1);
    return true;
  }

  // '!' | '*'
  private static boolean defined_table_name_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXCLAMATION_MARK);
    if (!r) r = consumeToken(b, STAR);
    return r;
  }

  /* ********************************************************** */
  // EXISTS '('
  static boolean exists_expression_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression_name")) return false;
    if (!nextTokenIs(b, EXISTS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, EXISTS, LPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NAMED_PARAMETER | expression ( ',' expression )* | from_clause_subqueries_statement
  static boolean expression_in_subquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAMED_PARAMETER);
    if (!r) r = expression_in_subquery_1(b, l + 1);
    if (!r) r = from_clause_subqueries_statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression ( ',' expression )*
  private static boolean expression_in_subquery_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && expression_in_subquery_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ',' expression )*
  private static boolean expression_in_subquery_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expression_in_subquery_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_in_subquery_1_1", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean expression_in_subquery_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ORDER | IDENTIFIER
  public static boolean ext_parameter_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ext_parameter_name")) return false;
    if (!nextTokenIs(b, "<ext parameter name>", IDENTIFIER, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXT_PARAMETER_NAME, "<ext parameter name>");
    r = consumeToken(b, ORDER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ',' result_column
  static boolean following_result_column(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "following_result_column")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && result_column(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FROM from_clause_expr ( join_operator from_clause_expr )*
  public static boolean from_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FROM_CLAUSE, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, from_clause_expr(b, l + 1));
    r = p && from_clause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( join_operator from_clause_expr )*
  private static boolean from_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_2", c)) break;
    }
    return true;
  }

  // join_operator from_clause_expr
  private static boolean from_clause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator(b, l + 1);
    r = r && from_clause_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // y_from_clause
  //   | from_clause_select
  public static boolean from_clause_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_expr")) return false;
    if (!nextTokenIs(b, "<from clause expr>", LBRACE, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FROM_CLAUSE_EXPR, "<from clause expr>");
    r = y_from_clause(b, l + 1);
    if (!r) r = from_clause_select(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ((from_clause_select_query ( ( AS )? table_alias_name)?) | from_clause_subqueries) join_constraint?
  public static boolean from_clause_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_clause_select_0(b, l + 1);
    r = r && from_clause_select_1(b, l + 1);
    exit_section_(b, m, FROM_CLAUSE_SELECT, r);
    return r;
  }

  // (from_clause_select_query ( ( AS )? table_alias_name)?) | from_clause_subqueries
  private static boolean from_clause_select_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_clause_select_0_0(b, l + 1);
    if (!r) r = from_clause_subqueries(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // from_clause_select_query ( ( AS )? table_alias_name)?
  private static boolean from_clause_select_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_clause_select_query(b, l + 1);
    r = r && from_clause_select_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ( AS )? table_alias_name)?
  private static boolean from_clause_select_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_0_0_1")) return false;
    from_clause_select_0_0_1_0(b, l + 1);
    return true;
  }

  // ( AS )? table_alias_name
  private static boolean from_clause_select_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_clause_select_0_0_1_0_0(b, l + 1);
    r = r && table_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean from_clause_select_0_0_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_0_0_1_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  // join_constraint?
  private static boolean from_clause_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_1")) return false;
    join_constraint(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' &(SELECT) from_query_greedy ')'
  public static boolean from_clause_select_query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_query")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FROM_CLAUSE_SELECT_QUERY, null);
    r = consumeToken(b, LPAREN);
    r = r && from_clause_select_query_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, from_query_greedy(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &(SELECT)
  private static boolean from_clause_select_query_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_select_query_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, SELECT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // table_or_subquery ( join_operator table_or_subquery join_constraint? )*
  public static boolean from_clause_simple(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_simple")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FROM_CLAUSE_SIMPLE, "<from clause simple>");
    r = table_or_subquery(b, l + 1);
    r = r && from_clause_simple_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( join_operator table_or_subquery join_constraint? )*
  private static boolean from_clause_simple_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_simple_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_simple_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_simple_1", c)) break;
    }
    return true;
  }

  // join_operator table_or_subquery join_constraint?
  private static boolean from_clause_simple_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_simple_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator(b, l + 1);
    r = r && table_or_subquery(b, l + 1);
    r = r && from_clause_simple_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // join_constraint?
  private static boolean from_clause_simple_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_simple_1_0_2")) return false;
    join_constraint(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' from_clause_subqueries_statement ')' ( ( AS )? table_alias_name)?
  public static boolean from_clause_subqueries(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && from_clause_subqueries_statement(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && from_clause_subqueries_3(b, l + 1);
    exit_section_(b, m, FROM_CLAUSE_SUBQUERIES, r);
    return r;
  }

  // ( ( AS )? table_alias_name)?
  private static boolean from_clause_subqueries_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_3")) return false;
    from_clause_subqueries_3_0(b, l + 1);
    return true;
  }

  // ( AS )? table_alias_name
  private static boolean from_clause_subqueries_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_clause_subqueries_3_0_0(b, l + 1);
    r = r && table_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean from_clause_subqueries_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_3_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  /* ********************************************************** */
  // ( select_subquery_combined compound_operator? )*
  static boolean from_clause_subqueries_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_statement")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_subqueries_statement_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_subqueries_statement", c)) break;
    }
    return true;
  }

  // select_subquery_combined compound_operator?
  private static boolean from_clause_subqueries_statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_statement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = select_subquery_combined(b, l + 1);
    r = r && from_clause_subqueries_statement_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // compound_operator?
  private static boolean from_clause_subqueries_statement_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_statement_0_1")) return false;
    compound_operator(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // select_statement
  static boolean from_query_greedy(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_query_greedy")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = select_statement(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::from_query_greedy_recover);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean from_query_greedy_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_query_greedy_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // defined_table_name ( ( AS )? table_alias_name )?
  public static boolean from_table(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_table")) return false;
    if (!nextTokenIs(b, "<from table>", IDENTIFIER, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FROM_TABLE, "<from table>");
    r = defined_table_name(b, l + 1);
    r = r && from_table_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( ( AS )? table_alias_name )?
  private static boolean from_table_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_table_1")) return false;
    from_table_1_0(b, l + 1);
    return true;
  }

  // ( AS )? table_alias_name
  private static boolean from_table_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_table_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = from_table_1_0_0(b, l + 1);
    r = r && table_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean from_table_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_table_1_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  /* ********************************************************** */
  // function_name '('
  static boolean function_call_expression_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_name")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_name(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean function_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_name")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, FUNCTION_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // group_by_literal expression ( ',' expression )* ( having_clause )?
  public static boolean group_by_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP_BY_CLAUSE, "<group by clause>");
    r = group_by_literal(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && report_error_(b, group_by_clause_2(b, l + 1)) && r;
    r = p && group_by_clause_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, FlexibleSearchParser::group_by_clause_recover);
    return r || p;
  }

  // ( ',' expression )*
  private static boolean group_by_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!group_by_clause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_by_clause_2", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean group_by_clause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( having_clause )?
  private static boolean group_by_clause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_3")) return false;
    group_by_clause_3_0(b, l + 1);
    return true;
  }

  // ( having_clause )
  private static boolean group_by_clause_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = having_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | HAVING | order_clause_literal | LIMIT | ')' | '}}')
  static boolean group_by_clause_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !group_by_clause_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | HAVING | order_clause_literal | LIMIT | ')' | '}}'
  private static boolean group_by_clause_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, HAVING);
    if (!r) r = order_clause_literal(b, l + 1);
    if (!r) r = consumeToken(b, LIMIT);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RDBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // GROUP BY
  static boolean group_by_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_literal")) return false;
    if (!nextTokenIs(b, GROUP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GROUP, BY);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // HAVING expression
  public static boolean having_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "having_clause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HAVING_CLAUSE, "<having clause>");
    r = consumeToken(b, HAVING);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, FlexibleSearchParser::having_clause_recover);
    return r || p;
  }

  /* ********************************************************** */
  // !(<<eof>> | LIMIT | order_clause_literal | ')' | '}}')
  static boolean having_clause_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "having_clause_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !having_clause_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | LIMIT | order_clause_literal | ')' | '}}'
  private static boolean having_clause_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "having_clause_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, LIMIT);
    if (!r) r = order_clause_literal(b, l + 1);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RDBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // join_constraint_on | join_constraint_using
  public static boolean join_constraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint")) return false;
    if (!nextTokenIs(b, "<join constraint>", ON, USING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CONSTRAINT, "<join constraint>");
    r = join_constraint_on(b, l + 1);
    if (!r) r = join_constraint_using(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ON expression
  static boolean join_constraint_on(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_on")) return false;
    if (!nextTokenIs(b, ON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ON);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // USING '(' column_name ( ',' column_name )* ')'
  static boolean join_constraint_using(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_using")) return false;
    if (!nextTokenIs(b, USING)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, USING, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, column_name(b, l + 1));
    r = p && report_error_(b, join_constraint_using_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( ',' column_name )*
  private static boolean join_constraint_using_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_using_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!join_constraint_using_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "join_constraint_using_3", c)) break;
    }
    return true;
  }

  // ',' column_name
  private static boolean join_constraint_using_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_using_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && column_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ',' | ( LEFT OUTER? | INNER | RIGHT )? JOIN
  public static boolean join_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_OPERATOR, "<join operator>");
    r = consumeToken(b, COMMA);
    if (!r) r = join_operator_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( LEFT OUTER? | INNER | RIGHT )? JOIN
  private static boolean join_operator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator_1_0(b, l + 1);
    r = r && consumeToken(b, JOIN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( LEFT OUTER? | INNER | RIGHT )?
  private static boolean join_operator_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0")) return false;
    join_operator_1_0_0(b, l + 1);
    return true;
  }

  // LEFT OUTER? | INNER | RIGHT
  private static boolean join_operator_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator_1_0_0_0(b, l + 1);
    if (!r) r = consumeToken(b, INNER);
    if (!r) r = consumeToken(b, RIGHT);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT OUTER?
  private static boolean join_operator_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT);
    r = r && join_operator_1_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OUTER?
  private static boolean join_operator_1_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0_0_0_1")) return false;
    consumeToken(b, OUTER);
    return true;
  }

  /* ********************************************************** */
  // LIMIT expression ( ( OFFSET | ',' ) expression )?
  public static boolean limit_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIMIT_CLAUSE, "<limit clause>");
    r = consumeToken(b, LIMIT);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && limit_clause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, FlexibleSearchParser::limit_clause_recover);
    return r || p;
  }

  // ( ( OFFSET | ',' ) expression )?
  private static boolean limit_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause_2")) return false;
    limit_clause_2_0(b, l + 1);
    return true;
  }

  // ( OFFSET | ',' ) expression
  private static boolean limit_clause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = limit_clause_2_0_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OFFSET | ','
  private static boolean limit_clause_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause_2_0_0")) return false;
    boolean r;
    r = consumeToken(b, OFFSET);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | ')' | '}}')
  static boolean limit_clause_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !limit_clause_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | ')' | '}}'
  private static boolean limit_clause_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RDBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // signed_number
  //   | string_literal // X marks a blob literal
  //   | NULL
  static boolean literal_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_value")) return false;
    boolean r;
    r = signed_number(b, l + 1);
    if (!r) r = string_literal(b, l + 1);
    if (!r) r = consumeToken(b, NULL);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER | BRACKET_LITERAL | BACKTICK_LITERAL | string_literal
  static boolean name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, BRACKET_LITERAL);
    if (!r) r = consumeToken(b, BACKTICK_LITERAL);
    if (!r) r = string_literal(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // order_clause_literal ordering_term ( ',' ordering_term )*
  public static boolean order_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ORDER_CLAUSE, "<order clause>");
    r = order_clause_literal(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, ordering_term(b, l + 1));
    r = p && order_clause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, FlexibleSearchParser::order_clause_recover);
    return r || p;
  }

  // ( ',' ordering_term )*
  private static boolean order_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!order_clause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_clause_2", c)) break;
    }
    return true;
  }

  // ',' ordering_term
  private static boolean order_clause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ordering_term(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ORDER BY
  static boolean order_clause_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_literal")) return false;
    if (!nextTokenIs(b, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ORDER, BY);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | LIMIT | ')' | '}}')
  static boolean order_clause_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !order_clause_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | LIMIT | ')' | '}}'
  private static boolean order_clause_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, LIMIT);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RDBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression ( ASC | DESC )?
  public static boolean ordering_term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ordering_term")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDERING_TERM, "<ordering term>");
    r = expression(b, l + 1, -1);
    r = r && ordering_term_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( ASC | DESC )?
  private static boolean ordering_term_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ordering_term_1")) return false;
    ordering_term_1_0(b, l + 1);
    return true;
  }

  // ASC | DESC
  private static boolean ordering_term_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ordering_term_1_0")) return false;
    boolean r;
    r = consumeToken(b, ASC);
    if (!r) r = consumeToken(b, DESC);
    return r;
  }

  /* ********************************************************** */
  // '*'
  //   | '{' selected_table_name column_separator '*' '}'
  //   | selected_table_name column_separator '*'
  //   | expression ( ( AS )? column_alias_name )?
  public static boolean result_column(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESULT_COLUMN, "<result column>");
    r = consumeToken(b, STAR);
    if (!r) r = result_column_1(b, l + 1);
    if (!r) r = result_column_2(b, l + 1);
    if (!r) r = result_column_3(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::result_column_recover);
    return r;
  }

  // '{' selected_table_name column_separator '*' '}'
  private static boolean result_column_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    r = r && consumeTokens(b, 0, STAR, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // selected_table_name column_separator '*'
  private static boolean result_column_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    r = r && consumeToken(b, STAR);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression ( ( AS )? column_alias_name )?
  private static boolean result_column_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && result_column_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ( AS )? column_alias_name )?
  private static boolean result_column_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_3_1")) return false;
    result_column_3_1_0(b, l + 1);
    return true;
  }

  // ( AS )? column_alias_name
  private static boolean result_column_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_3_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = result_column_3_1_0_0(b, l + 1);
    r = r && column_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean result_column_3_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_3_1_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  /* ********************************************************** */
  // !(<<eof>> | FROM | ',')
  static boolean result_column_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !result_column_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | FROM | ','
  private static boolean result_column_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, FROM);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // result_column ( following_result_column )*
  public static boolean result_columns(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESULT_COLUMNS, "<result columns>");
    r = result_column(b, l + 1);
    r = r && result_columns_1(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::result_columns_recover);
    return r;
  }

  // ( following_result_column )*
  private static boolean result_columns_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!result_columns_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "result_columns_1", c)) break;
    }
    return true;
  }

  // ( following_result_column )
  private static boolean result_columns_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = following_result_column(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | FROM)
  static boolean result_columns_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !result_columns_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | FROM
  private static boolean result_columns_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, FROM);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement
  static boolean root(PsiBuilder b, int l) {
    return statement(b, l + 1);
  }

  /* ********************************************************** */
  // SELECT ( DISTINCT | ALL )? result_columns from_clause where_clause? group_by_clause?
  public static boolean select_core_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_CORE_SELECT, null);
    r = consumeToken(b, SELECT);
    p = r; // pin = 1
    r = r && report_error_(b, select_core_select_1(b, l + 1));
    r = p && report_error_(b, result_columns(b, l + 1)) && r;
    r = p && report_error_(b, from_clause(b, l + 1)) && r;
    r = p && report_error_(b, select_core_select_4(b, l + 1)) && r;
    r = p && select_core_select_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( DISTINCT | ALL )?
  private static boolean select_core_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select_1")) return false;
    select_core_select_1_0(b, l + 1);
    return true;
  }

  // DISTINCT | ALL
  private static boolean select_core_select_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select_1_0")) return false;
    boolean r;
    r = consumeToken(b, DISTINCT);
    if (!r) r = consumeToken(b, ALL);
    return r;
  }

  // where_clause?
  private static boolean select_core_select_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select_4")) return false;
    where_clause(b, l + 1);
    return true;
  }

  // group_by_clause?
  private static boolean select_core_select_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select_5")) return false;
    group_by_clause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // select_core_select (compound_operator select_core_select)* order_clause? limit_clause?
  public static boolean select_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = select_core_select(b, l + 1);
    r = r && select_statement_1(b, l + 1);
    r = r && select_statement_2(b, l + 1);
    r = r && select_statement_3(b, l + 1);
    exit_section_(b, m, SELECT_STATEMENT, r);
    return r;
  }

  // (compound_operator select_core_select)*
  private static boolean select_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!select_statement_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "select_statement_1", c)) break;
    }
    return true;
  }

  // compound_operator select_core_select
  private static boolean select_statement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compound_operator(b, l + 1);
    r = r && select_core_select(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // order_clause?
  private static boolean select_statement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_2")) return false;
    order_clause(b, l + 1);
    return true;
  }

  // limit_clause?
  private static boolean select_statement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_3")) return false;
    limit_clause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' "{{" &(SELECT) subquery_greedy "}}" ')' ( ( AS )? table_alias_name)?
  public static boolean select_subquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_SUBQUERY, null);
    r = consumeTokens(b, 0, LPAREN, LDBRACE);
    r = r && select_subquery_2(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, subquery_greedy(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RDBRACE, RPAREN)) && r;
    r = p && select_subquery_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &(SELECT)
  private static boolean select_subquery_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, SELECT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( ( AS )? table_alias_name)?
  private static boolean select_subquery_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_6")) return false;
    select_subquery_6_0(b, l + 1);
    return true;
  }

  // ( AS )? table_alias_name
  private static boolean select_subquery_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = select_subquery_6_0_0(b, l + 1);
    r = r && table_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean select_subquery_6_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_6_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  /* ********************************************************** */
  // "{{" &(SELECT) subquery_greedy "}}"
  public static boolean select_subquery_combined(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_combined")) return false;
    if (!nextTokenIs(b, LDBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_SUBQUERY_COMBINED, null);
    r = consumeToken(b, LDBRACE);
    r = r && select_subquery_combined_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, subquery_greedy(b, l + 1));
    r = p && consumeToken(b, RDBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &(SELECT)
  private static boolean select_subquery_combined_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_subquery_combined_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, SELECT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER | BACKTICK_LITERAL
  public static boolean selected_table_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selected_table_name")) return false;
    if (!nextTokenIs(b, "<selected table name>", BACKTICK_LITERAL, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECTED_TABLE_NAME, "<selected table name>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, BACKTICK_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ( '+' | '-' )? NUMERIC_LITERAL
  public static boolean signed_number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "signed_number")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIGNED_NUMBER, "<signed number>");
    r = signed_number_0(b, l + 1);
    r = r && consumeToken(b, NUMERIC_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( '+' | '-' )?
  private static boolean signed_number_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "signed_number_0")) return false;
    signed_number_0_0(b, l + 1);
    return true;
  }

  // '+' | '-'
  private static boolean signed_number_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "signed_number_0_0")) return false;
    boolean r;
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    return r;
  }

  /* ********************************************************** */
  // (select_statement)
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    if (!nextTokenIs(b, "<statement>", SELECT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<statement>");
    r = select_statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SINGLE_QUOTE_STRING_LITERAL | DOUBLE_QUOTE_STRING_LITERAL
  static boolean string_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_literal")) return false;
    if (!nextTokenIs(b, "", DOUBLE_QUOTE_STRING_LITERAL, SINGLE_QUOTE_STRING_LITERAL)) return false;
    boolean r;
    r = consumeToken(b, SINGLE_QUOTE_STRING_LITERAL);
    if (!r) r = consumeToken(b, DOUBLE_QUOTE_STRING_LITERAL);
    return r;
  }

  /* ********************************************************** */
  // select_statement
  static boolean subquery_greedy(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subquery_greedy")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = select_statement(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::y_subquery_greedy_recover);
    return r;
  }

  /* ********************************************************** */
  // name
  public static boolean table_alias_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "table_alias_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_ALIAS_NAME, "<table alias name>");
    r = name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // from_table | select_subquery | '(' table_or_subquery ')'
  public static boolean table_or_subquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "table_or_subquery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_OR_SUBQUERY, "<table or subquery>");
    r = from_table(b, l + 1);
    if (!r) r = select_subquery(b, l + 1);
    if (!r) r = table_or_subquery_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' table_or_subquery ')'
  private static boolean table_or_subquery_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "table_or_subquery_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && table_or_subquery(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // name ( '(' signed_number ')' | '(' signed_number ',' signed_number ')' )?
  public static boolean type_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_NAME, "<type name>");
    r = name(b, l + 1);
    r = r && type_name_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( '(' signed_number ')' | '(' signed_number ',' signed_number ')' )?
  private static boolean type_name_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_name_1")) return false;
    type_name_1_0(b, l + 1);
    return true;
  }

  // '(' signed_number ')' | '(' signed_number ',' signed_number ')'
  private static boolean type_name_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_name_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type_name_1_0_0(b, l + 1);
    if (!r) r = type_name_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' signed_number ')'
  private static boolean type_name_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_name_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && signed_number(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' signed_number ',' signed_number ')'
  private static boolean type_name_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_name_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && signed_number(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && signed_number(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WHERE expression
  public static boolean where_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "where_clause")) return false;
    if (!nextTokenIs(b, WHERE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHERE_CLAUSE, null);
    r = consumeToken(b, WHERE);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '[' column_localized_name ']'
  static boolean y_column_localized_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "y_column_localized_name")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, column_localized_name(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ORDER | IDENTIFIER
  public static boolean y_column_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "y_column_name")) return false;
    if (!nextTokenIs(b, "<y column name>", IDENTIFIER, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, Y_COLUMN_NAME, "<y column name>");
    r = consumeToken(b, ORDER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' from_clause_simple '}'
  public static boolean y_from_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "y_from_clause")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, Y_FROM_CLAUSE, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, from_clause_simple(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !"}}"
  static boolean y_subquery_greedy_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "y_subquery_greedy_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RDBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Expression root: expression
  // Operator priority table:
  // 0: PREFIX(mysql_function_expression)
  // 1: BINARY(or_expression)
  // 2: BINARY(and_expression)
  // 3: ATOM(case_expression)
  // 4: ATOM(exists_expression)
  // 5: POSTFIX(in_expression)
  // 6: POSTFIX(isnull_expression)
  // 7: BINARY(like_expression)
  // 8: ATOM(cast_expression)
  // 9: ATOM(function_call_expression)
  // 10: BINARY(equivalence_expression) BINARY(between_expression)
  // 11: BINARY(comparison_expression)
  // 12: BINARY(bit_expression)
  // 13: BINARY(mul_expression)
  // 14: BINARY(concat_expression)
  // 15: BINARY(unary_expression)
  // 16: ATOM(literal_expression)
  // 17: ATOM(column_ref_y_expression)
  // 18: ATOM(subquery_paren_expression)
  // 19: ATOM(column_ref_expression)
  // 20: ATOM(paren_expression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = mysql_function_expression(b, l + 1);
    if (!r) r = case_expression(b, l + 1);
    if (!r) r = exists_expression(b, l + 1);
    if (!r) r = cast_expression(b, l + 1);
    if (!r) r = function_call_expression(b, l + 1);
    if (!r) r = literal_expression(b, l + 1);
    if (!r) r = column_ref_y_expression(b, l + 1);
    if (!r) r = subquery_paren_expression(b, l + 1);
    if (!r) r = column_ref_expression(b, l + 1);
    if (!r) r = paren_expression(b, l + 1);
    p = r;
    r = r && expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 1 && consumeTokenSmart(b, OR)) {
        r = expression(b, l, 1);
        exit_section_(b, l, m, OR_EXPRESSION, r, true, null);
      }
      else if (g < 2 && consumeTokenSmart(b, AND)) {
        r = expression(b, l, 2);
        exit_section_(b, l, m, AND_EXPRESSION, r, true, null);
      }
      else if (g < 5 && in_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, IN_EXPRESSION, r, true, null);
      }
      else if (g < 6 && isnull_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, ISNULL_EXPRESSION, r, true, null);
      }
      else if (g < 7 && like_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = like_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, LIKE_EXPRESSION, r, true, null);
      }
      else if (g < 10 && equivalence_expression_0(b, l + 1)) {
        r = expression(b, l, 10);
        exit_section_(b, l, m, EQUIVALENCE_EXPRESSION, r, true, null);
      }
      else if (g < 10 && between_expression_0(b, l + 1)) {
        r = report_error_(b, expression(b, l, 10));
        r = between_expression_1(b, l + 1) && r;
        exit_section_(b, l, m, BETWEEN_EXPRESSION, r, true, null);
      }
      else if (g < 11 && comparison_expression_0(b, l + 1)) {
        r = expression(b, l, 11);
        exit_section_(b, l, m, COMPARISON_EXPRESSION, r, true, null);
      }
      else if (g < 12 && bit_expression_0(b, l + 1)) {
        r = expression(b, l, 12);
        exit_section_(b, l, m, BIT_EXPRESSION, r, true, null);
      }
      else if (g < 13 && mul_expression_0(b, l + 1)) {
        r = expression(b, l, 13);
        exit_section_(b, l, m, MUL_EXPRESSION, r, true, null);
      }
      else if (g < 14 && consumeTokenSmart(b, CONCAT)) {
        r = expression(b, l, 14);
        exit_section_(b, l, m, CONCAT_EXPRESSION, r, true, null);
      }
      else if (g < 15 && unary_expression_0(b, l + 1)) {
        r = expression(b, l, 15);
        exit_section_(b, l, m, UNARY_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean mysql_function_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mysql_function_expression")) return false;
    if (!nextTokenIsSmart(b, INTERVAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, INTERVAL);
    p = r;
    r = p && expression(b, l, 0);
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    exit_section_(b, l, m, MYSQL_FUNCTION_EXPRESSION, r, p, null);
    return r || p;
  }

  // CASE expression? ( WHEN expression THEN expression )+ ( ELSE expression )? END
  public static boolean case_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression")) return false;
    if (!nextTokenIsSmart(b, CASE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CASE_EXPRESSION, null);
    r = consumeTokenSmart(b, CASE);
    p = r; // pin = 1
    r = r && report_error_(b, case_expression_1(b, l + 1));
    r = p && report_error_(b, case_expression_2(b, l + 1)) && r;
    r = p && report_error_(b, case_expression_3(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean case_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // ( WHEN expression THEN expression )+
  private static boolean case_expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = case_expression_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!case_expression_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "case_expression_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // WHEN expression THEN expression
  private static boolean case_expression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, WHEN);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, THEN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ELSE expression )?
  private static boolean case_expression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression_3")) return false;
    case_expression_3_0(b, l + 1);
    return true;
  }

  // ELSE expression
  private static boolean case_expression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_expression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ELSE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT? exists_expression_name from_clause_subqueries_statement ')'
  public static boolean exists_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression")) return false;
    if (!nextTokenIsSmart(b, EXISTS, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXISTS_EXPRESSION, "<exists expression>");
    r = exists_expression_0(b, l + 1);
    r = r && exists_expression_name(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, from_clause_subqueries_statement(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NOT?
  private static boolean exists_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression_0")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // NOT? IN '(' ( expression_in_subquery ) ')'
  private static boolean in_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "in_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = in_expression_0_0(b, l + 1);
    r = r && consumeTokensSmart(b, 0, IN, LPAREN);
    r = r && in_expression_0_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean in_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "in_expression_0_0")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // ( expression_in_subquery )
  private static boolean in_expression_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "in_expression_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression_in_subquery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // IS NOT? NULL
  private static boolean isnull_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isnull_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IS);
    r = r && isnull_expression_0_1(b, l + 1);
    r = r && consumeToken(b, NULL);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean isnull_expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isnull_expression_0_1")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // NOT? ( LIKE | GLOB | REGEXP | MATCH )
  private static boolean like_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "like_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = like_expression_0_0(b, l + 1);
    r = r && like_expression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean like_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "like_expression_0_0")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // LIKE | GLOB | REGEXP | MATCH
  private static boolean like_expression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "like_expression_0_1")) return false;
    boolean r;
    r = consumeTokenSmart(b, LIKE);
    if (!r) r = consumeTokenSmart(b, GLOB);
    if (!r) r = consumeTokenSmart(b, REGEXP);
    if (!r) r = consumeTokenSmart(b, MATCH);
    return r;
  }

  // ( ESCAPE expression )?
  private static boolean like_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "like_expression_1")) return false;
    like_expression_1_0(b, l + 1);
    return true;
  }

  // ESCAPE expression
  private static boolean like_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "like_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ESCAPE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CAST '(' expression AS type_name ')'
  public static boolean cast_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cast_expression")) return false;
    if (!nextTokenIsSmart(b, CAST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CAST_EXPRESSION, null);
    r = consumeTokensSmart(b, 1, CAST, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, AS)) && r;
    r = p && report_error_(b, type_name(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // function_call_expression_name ( ( DISTINCT )? expression ( ',' expression )* | '*' )? ')'
  public static boolean function_call_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_CALL_EXPRESSION, null);
    r = function_call_expression_name(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, function_call_expression_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( ( DISTINCT )? expression ( ',' expression )* | '*' )?
  private static boolean function_call_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1")) return false;
    function_call_expression_1_0(b, l + 1);
    return true;
  }

  // ( DISTINCT )? expression ( ',' expression )* | '*'
  private static boolean function_call_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_expression_1_0_0(b, l + 1);
    if (!r) r = consumeTokenSmart(b, STAR);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( DISTINCT )? expression ( ',' expression )*
  private static boolean function_call_expression_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_expression_1_0_0_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && function_call_expression_1_0_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( DISTINCT )?
  private static boolean function_call_expression_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1_0_0_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // ( ',' expression )*
  private static boolean function_call_expression_1_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1_0_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!function_call_expression_1_0_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_call_expression_1_0_0_2", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean function_call_expression_1_0_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_1_0_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '==' | '=' | '!=' | '<>' | IS NOT?
  private static boolean equivalence_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, EQEQ);
    if (!r) r = consumeTokenSmart(b, EQ);
    if (!r) r = consumeTokenSmart(b, NOT_EQ);
    if (!r) r = consumeTokenSmart(b, UNEQ);
    if (!r) r = equivalence_expression_0_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // IS NOT?
  private static boolean equivalence_expression_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence_expression_0_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IS);
    r = r && equivalence_expression_0_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean equivalence_expression_0_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equivalence_expression_0_4_1")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // NOT? BETWEEN
  private static boolean between_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "between_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = between_expression_0_0(b, l + 1);
    r = r && consumeToken(b, BETWEEN);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean between_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "between_expression_0_0")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // AND expression
  private static boolean between_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "between_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<' | '<=' | '>' | '>='
  private static boolean comparison_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparison_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, LT);
    if (!r) r = consumeTokenSmart(b, LTE);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, GTE);
    return r;
  }

  // '<<' | '>>' | '&' | '|'
  private static boolean bit_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bit_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, SHL);
    if (!r) r = consumeTokenSmart(b, SHR);
    if (!r) r = consumeTokenSmart(b, AMP);
    if (!r) r = consumeTokenSmart(b, BAR);
    return r;
  }

  // '*' | '/' | '%'
  private static boolean mul_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, STAR);
    if (!r) r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, MOD);
    return r;
  }

  // '-' | '+' | '~' | NOT !IN
  private static boolean unary_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MINUS);
    if (!r) r = consumeTokenSmart(b, PLUS);
    if (!r) r = consumeTokenSmart(b, TILDE);
    if (!r) r = unary_expression_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT !IN
  private static boolean unary_expression_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NOT);
    r = r && unary_expression_0_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !IN
  private static boolean unary_expression_0_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeTokenSmart(b, IN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // literal_value | bind_parameter
  public static boolean literal_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = literal_value(b, l + 1);
    if (!r) r = bind_parameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '{' column_ref_y_expression_other '}'
  public static boolean column_ref_y_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression")) return false;
    if (!nextTokenIsSmart(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_REF_Y_EXPRESSION, null);
    r = consumeTokenSmart(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, column_ref_y_expression_other(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '(' from_clause_subqueries_statement ')'
  public static boolean subquery_paren_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subquery_paren_expression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && from_clause_subqueries_statement(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, SUBQUERY_PAREN_EXPRESSION, r);
    return r;
  }

  // aliased_column_ref_expression
  //  | column_name
  public static boolean column_ref_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_REF_EXPRESSION, "<column ref expression>");
    r = aliased_column_ref_expression(b, l + 1);
    if (!r) r = column_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' (expression)* ')'
  public static boolean paren_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PAREN_EXPRESSION, null);
    r = consumeTokenSmart(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, paren_expression_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (expression)*
  private static boolean paren_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!paren_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "paren_expression_1", c)) break;
    }
    return true;
  }

  // (expression)
  private static boolean paren_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

}
