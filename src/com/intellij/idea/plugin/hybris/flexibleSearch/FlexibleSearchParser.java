// This is a generated file. Not intended for manual editing.
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
    create_token_set_(AND_EXPRESSION, BETWEEN_EXPRESSION, BIT_EXPRESSION, CASE_EXPRESSION,
      CAST_EXPRESSION, COLUMN_REF_EXPRESSION, COLUMN_REF_Y_EXPRESSION, COMPARISON_EXPRESSION,
      CONCAT_EXPRESSION, EQUIVALENCE_EXPRESSION, EXISTS_EXPRESSION, EXPRESSION,
      FROM_CLAUSE_EXPRESSION, FUNCTION_CALL_EXPRESSION, IN_EXPRESSION, ISNULL_EXPRESSION,
      LIKE_EXPRESSION, LITERAL_EXPRESSION, MUL_EXPRESSION, MYSQL_FUNCTION_EXPRESSION,
      OR_EXPRESSION, PAREN_EXPRESSION, UNARY_EXPRESSION),
  };

  /* ********************************************************** */
  // NUMBERED_PARAMETER | NAMED_PARAMETER
  public static boolean bind_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter")) return false;
    if (!nextTokenIs(b, "<bind parameter>", NAMED_PARAMETER, NUMBERED_PARAMETER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BIND_PARAMETER, "<bind parameter>");
    r = consumeToken(b, NUMBERED_PARAMETER);
    if (!r) r = consumeToken(b, NAMED_PARAMETER);
    exit_section_(b, l, m, r, false, null);
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
  // BRACKET_LITERAL
  public static boolean column_localized(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_localized")) return false;
    if (!nextTokenIs(b, BRACKET_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BRACKET_LITERAL);
    exit_section_(b, m, COLUMN_LOCALIZED, r);
    return r;
  }

  /* ********************************************************** */
  // name
  public static boolean column_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_NAME, "<column name>");
    r = name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OUTER_JOIN
  public static boolean column_outer_join(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_outer_join")) return false;
    if (!nextTokenIs(b, OUTER_JOIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OUTER_JOIN);
    exit_section_(b, m, COLUMN_OUTER_JOIN, r);
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
  // name (EXCLAMATION_MARK | ASTERISK)?
  public static boolean defined_table_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFINED_TABLE_NAME, "<defined table name>");
    r = name(b, l + 1);
    r = r && defined_table_name_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (EXCLAMATION_MARK | ASTERISK)?
  private static boolean defined_table_name_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name_1")) return false;
    defined_table_name_1_0(b, l + 1);
    return true;
  }

  // EXCLAMATION_MARK | ASTERISK
  private static boolean defined_table_name_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defined_table_name_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXCLAMATION_MARK);
    if (!r) r = consumeToken(b, ASTERISK);
    return r;
  }

  /* ********************************************************** */
  // ( NAMED_PARAMETER | "{{" expression_subquery "}}" | expression ( ',' expression )* )?
  static boolean expression_in_subquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery")) return false;
    expression_in_subquery_0(b, l + 1);
    return true;
  }

  // NAMED_PARAMETER | "{{" expression_subquery "}}" | expression ( ',' expression )*
  private static boolean expression_in_subquery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAMED_PARAMETER);
    if (!r) r = expression_in_subquery_0_1(b, l + 1);
    if (!r) r = expression_in_subquery_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "{{" expression_subquery "}}"
  private static boolean expression_in_subquery_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LDBRACE);
    r = r && expression_subquery(b, l + 1);
    r = r && consumeToken(b, RDBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression ( ',' expression )*
  private static boolean expression_in_subquery_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && expression_in_subquery_0_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ',' expression )*
  private static boolean expression_in_subquery_0_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_0_2_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expression_in_subquery_0_2_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_in_subquery_0_2_1", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean expression_in_subquery_0_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_in_subquery_0_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // &(SELECT) subquery_greedy
  static boolean expression_subquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_subquery")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = expression_subquery_0(b, l + 1);
    p = r; // pin = 1
    r = r && subquery_greedy(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &(SELECT)
  private static boolean expression_subquery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_subquery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, SELECT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FROM from_clause_expression ( join_operator from_clause_expression )*
  public static boolean from_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && from_clause_expression(b, l + 1);
    r = r && from_clause_2(b, l + 1);
    exit_section_(b, m, FROM_CLAUSE, r);
    return r;
  }

  // ( join_operator from_clause_expression )*
  private static boolean from_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_2", c)) break;
    }
    return true;
  }

  // join_operator from_clause_expression
  private static boolean from_clause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator(b, l + 1);
    r = r && from_clause_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // y_from_clause
  //   | from_clause_select
  public static boolean from_clause_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_expression")) return false;
    if (!nextTokenIs(b, "<from clause expression>", LBRACE, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FROM_CLAUSE_EXPRESSION, "<from clause expression>");
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
  // '(' (  select_subquery_combined compound_operator? )* ')' ( ( AS )? table_alias_name)?
  public static boolean from_clause_subqueries(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && from_clause_subqueries_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && from_clause_subqueries_3(b, l + 1);
    exit_section_(b, m, FROM_CLAUSE_SUBQUERIES, r);
    return r;
  }

  // (  select_subquery_combined compound_operator? )*
  private static boolean from_clause_subqueries_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_subqueries_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_subqueries_1", c)) break;
    }
    return true;
  }

  // select_subquery_combined compound_operator?
  private static boolean from_clause_subqueries_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = select_subquery_combined(b, l + 1);
    r = r && from_clause_subqueries_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // compound_operator?
  private static boolean from_clause_subqueries_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_subqueries_1_0_1")) return false;
    compound_operator(b, l + 1);
    return true;
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
  // select_statement
  static boolean from_query_greedy(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_query_greedy")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = select_statement(b, l + 1);
    exit_section_(b, l, m, r, false, FlexibleSearchParser::from_query_recover);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean from_query_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_query_recover")) return false;
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
  // name
  public static boolean function_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_NAME, "<function name>");
    r = name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // GROUP BY expression ( ',' expression )* ( HAVING expression )?
  public static boolean group_by_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause")) return false;
    if (!nextTokenIs(b, GROUP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GROUP, BY);
    r = r && expression(b, l + 1, -1);
    r = r && group_by_clause_3(b, l + 1);
    r = r && group_by_clause_4(b, l + 1);
    exit_section_(b, m, GROUP_BY_CLAUSE, r);
    return r;
  }

  // ( ',' expression )*
  private static boolean group_by_clause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!group_by_clause_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_by_clause_3", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean group_by_clause_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( HAVING expression )?
  private static boolean group_by_clause_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_4")) return false;
    group_by_clause_4_0(b, l + 1);
    return true;
  }

  // HAVING expression
  private static boolean group_by_clause_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HAVING);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ON expression | USING '(' column_name ( ',' column_name )* ')'
  public static boolean join_constraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint")) return false;
    if (!nextTokenIs(b, "<join constraint>", ON, USING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CONSTRAINT, "<join constraint>");
    r = join_constraint_0(b, l + 1);
    if (!r) r = join_constraint_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ON expression
  private static boolean join_constraint_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // USING '(' column_name ( ',' column_name )* ')'
  private static boolean join_constraint_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, USING, LPAREN);
    r = r && column_name(b, l + 1);
    r = r && join_constraint_1_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ',' column_name )*
  private static boolean join_constraint_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_1_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!join_constraint_1_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "join_constraint_1_3", c)) break;
    }
    return true;
  }

  // ',' column_name
  private static boolean join_constraint_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_constraint_1_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && column_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ',' | ( LEFT OUTER? | INNER | CROSS | RIGHT )? JOIN
  public static boolean join_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_OPERATOR, "<join operator>");
    r = consumeToken(b, COMMA);
    if (!r) r = join_operator_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( LEFT OUTER? | INNER | CROSS | RIGHT )? JOIN
  private static boolean join_operator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator_1_0(b, l + 1);
    r = r && consumeToken(b, JOIN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( LEFT OUTER? | INNER | CROSS | RIGHT )?
  private static boolean join_operator_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0")) return false;
    join_operator_1_0_0(b, l + 1);
    return true;
  }

  // LEFT OUTER? | INNER | CROSS | RIGHT
  private static boolean join_operator_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "join_operator_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = join_operator_1_0_0_0(b, l + 1);
    if (!r) r = consumeToken(b, INNER);
    if (!r) r = consumeToken(b, CROSS);
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
    if (!nextTokenIs(b, LIMIT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LIMIT);
    r = r && expression(b, l + 1, -1);
    r = r && limit_clause_2(b, l + 1);
    exit_section_(b, m, LIMIT_CLAUSE, r);
    return r;
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
  // signed_number
  //   | string_literal // X marks a blob literal
  //   | NULL
  //   | CURRENT_TIME
  //   | CURRENT_DATE
  //   | CURRENT_TIMESTAMP
  static boolean literal_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_value")) return false;
    boolean r;
    r = signed_number(b, l + 1);
    if (!r) r = string_literal(b, l + 1);
    if (!r) r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, CURRENT_TIME);
    if (!r) r = consumeToken(b, CURRENT_DATE);
    if (!r) r = consumeToken(b, CURRENT_TIMESTAMP);
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
  // ORDER BY ordering_term ( ',' ordering_term )*
  public static boolean order_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause")) return false;
    if (!nextTokenIs(b, ORDER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ORDER, BY);
    r = r && ordering_term(b, l + 1);
    r = r && order_clause_3(b, l + 1);
    exit_section_(b, m, ORDER_CLAUSE, r);
    return r;
  }

  // ( ',' ordering_term )*
  private static boolean order_clause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!order_clause_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_clause_3", c)) break;
    }
    return true;
  }

  // ',' ordering_term
  private static boolean order_clause_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_clause_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ordering_term(b, l + 1);
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
  //   | '{'? selected_table_name column_separator '*' '}'?
  //   | expression ( ( AS )? column_alias_name )?
  public static boolean result_column(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESULT_COLUMN, "<result column>");
    r = consumeToken(b, STAR);
    if (!r) r = result_column_1(b, l + 1);
    if (!r) r = result_column_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '{'? selected_table_name column_separator '*' '}'?
  private static boolean result_column_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = result_column_1_0(b, l + 1);
    r = r && selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    r = r && consumeToken(b, STAR);
    r = r && result_column_1_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '{'?
  private static boolean result_column_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_1_0")) return false;
    consumeToken(b, LBRACE);
    return true;
  }

  // '}'?
  private static boolean result_column_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_1_4")) return false;
    consumeToken(b, RBRACE);
    return true;
  }

  // expression ( ( AS )? column_alias_name )?
  private static boolean result_column_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && result_column_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ( AS )? column_alias_name )?
  private static boolean result_column_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_2_1")) return false;
    result_column_2_1_0(b, l + 1);
    return true;
  }

  // ( AS )? column_alias_name
  private static boolean result_column_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = result_column_2_1_0_0(b, l + 1);
    r = r && column_alias_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( AS )?
  private static boolean result_column_2_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_column_2_1_0_0")) return false;
    consumeToken(b, AS);
    return true;
  }

  /* ********************************************************** */
  // result_column ( ',' result_column )*
  public static boolean result_columns(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESULT_COLUMNS, "<result columns>");
    r = result_column(b, l + 1);
    r = r && result_columns_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( ',' result_column )*
  private static boolean result_columns_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!result_columns_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "result_columns_1", c)) break;
    }
    return true;
  }

  // ',' result_column
  private static boolean result_columns_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "result_columns_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && result_column(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement SEMICOLON?
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    r = r && root_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SEMICOLON?
  private static boolean root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // SELECT ( DISTINCT | ALL )? result_columns from_clause? where_clause? group_by_clause?
  public static boolean select_core_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SELECT);
    r = r && select_core_select_1(b, l + 1);
    r = r && result_columns(b, l + 1);
    r = r && select_core_select_3(b, l + 1);
    r = r && select_core_select_4(b, l + 1);
    r = r && select_core_select_5(b, l + 1);
    exit_section_(b, m, SELECT_CORE_SELECT, r);
    return r;
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

  // from_clause?
  private static boolean select_core_select_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_core_select_3")) return false;
    from_clause(b, l + 1);
    return true;
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
  // name
  public static boolean selected_table_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selected_table_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECTED_TABLE_NAME, "<selected table name>");
    r = name(b, l + 1);
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
    exit_section_(b, l, m, r, false, FlexibleSearchParser::subquery_recover);
    return r;
  }

  /* ********************************************************** */
  // !"}}"
  static boolean subquery_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subquery_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RDBRACE);
    exit_section_(b, l, m, r, false, null);
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHERE);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, WHERE_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // '{' from_clause_simple '}'
  public static boolean y_from_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "y_from_clause")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && from_clause_simple(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, Y_FROM_CLAUSE, r);
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
  // 8: PREFIX(cast_expression)
  // 9: ATOM(function_call_expression)
  // 10: BINARY(equivalence_expression) BINARY(between_expression)
  // 11: BINARY(comparison_expression)
  // 12: BINARY(bit_expression)
  // 13: BINARY(mul_expression)
  // 14: BINARY(concat_expression)
  // 15: BINARY(unary_expression)
  // 16: ATOM(literal_expression)
  // 17: ATOM(column_ref_y_expression)
  // 18: ATOM(column_ref_expression)
  // 19: ATOM(paren_expression)
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, CASE);
    r = r && case_expression_1(b, l + 1);
    r = r && case_expression_2(b, l + 1);
    r = r && case_expression_3(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, CASE_EXPRESSION, r);
    return r;
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

  // ( NOT? EXISTS )? '(' "{{" expression_subquery "}}" ')'
  public static boolean exists_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXISTS_EXPRESSION, "<exists expression>");
    r = exists_expression_0(b, l + 1);
    r = r && consumeTokensSmart(b, 0, LPAREN, LDBRACE);
    r = r && expression_subquery(b, l + 1);
    r = r && consumeTokensSmart(b, 0, RDBRACE, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( NOT? EXISTS )?
  private static boolean exists_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression_0")) return false;
    exists_expression_0_0(b, l + 1);
    return true;
  }

  // NOT? EXISTS
  private static boolean exists_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = exists_expression_0_0_0(b, l + 1);
    r = r && consumeToken(b, EXISTS);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOT?
  private static boolean exists_expression_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exists_expression_0_0_0")) return false;
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

  public static boolean cast_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cast_expression")) return false;
    if (!nextTokenIsSmart(b, CAST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = parseTokensSmart(b, 0, CAST, LPAREN);
    p = r;
    r = p && expression(b, l, 8);
    r = p && report_error_(b, cast_expression_1(b, l + 1)) && r;
    exit_section_(b, l, m, CAST_EXPRESSION, r, p, null);
    return r || p;
  }

  // AS type_name ')'
  private static boolean cast_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cast_expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AS);
    r = r && type_name(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // function_name '(' ( ( DISTINCT )? expression ( ',' expression )* | '*' )? ')'
  public static boolean function_call_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_CALL_EXPRESSION, "<function call expression>");
    r = function_name(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && function_call_expression_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( ( DISTINCT )? expression ( ',' expression )* | '*' )?
  private static boolean function_call_expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2")) return false;
    function_call_expression_2_0(b, l + 1);
    return true;
  }

  // ( DISTINCT )? expression ( ',' expression )* | '*'
  private static boolean function_call_expression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_expression_2_0_0(b, l + 1);
    if (!r) r = consumeTokenSmart(b, STAR);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( DISTINCT )? expression ( ',' expression )*
  private static boolean function_call_expression_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_expression_2_0_0_0(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && function_call_expression_2_0_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( DISTINCT )?
  private static boolean function_call_expression_2_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2_0_0_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // ( ',' expression )*
  private static boolean function_call_expression_2_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2_0_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!function_call_expression_2_0_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_call_expression_2_0_0_2", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean function_call_expression_2_0_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_expression_2_0_0_2_0")) return false;
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

  // '{' (selected_table_name column_separator)? column_name column_localized? column_outer_join? '}'
  public static boolean column_ref_y_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression")) return false;
    if (!nextTokenIsSmart(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LBRACE);
    r = r && column_ref_y_expression_1(b, l + 1);
    r = r && column_name(b, l + 1);
    r = r && column_ref_y_expression_3(b, l + 1);
    r = r && column_ref_y_expression_4(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, COLUMN_REF_Y_EXPRESSION, r);
    return r;
  }

  // (selected_table_name column_separator)?
  private static boolean column_ref_y_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_1")) return false;
    column_ref_y_expression_1_0(b, l + 1);
    return true;
  }

  // selected_table_name column_separator
  private static boolean column_ref_y_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // column_localized?
  private static boolean column_ref_y_expression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_3")) return false;
    column_localized(b, l + 1);
    return true;
  }

  // column_outer_join?
  private static boolean column_ref_y_expression_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_y_expression_4")) return false;
    column_outer_join(b, l + 1);
    return true;
  }

  // selected_table_name column_separator column_name
  //  | column_name
  public static boolean column_ref_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN_REF_EXPRESSION, "<column ref expression>");
    r = column_ref_expression_0(b, l + 1);
    if (!r) r = column_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // selected_table_name column_separator column_name
  private static boolean column_ref_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_ref_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = selected_table_name(b, l + 1);
    r = r && column_separator(b, l + 1);
    r = r && column_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' (expression)* ')'
  public static boolean paren_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expression")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && paren_expression_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, PAREN_EXPRESSION, r);
    return r;
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
