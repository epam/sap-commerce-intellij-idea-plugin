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
package com.intellij.idea.plugin.hybris.polyglotQuery;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*;
import static com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryParserUtils.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PolyglotQueryParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
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

  /* ********************************************************** */
  // '{' IDENTIFIER ('[' IDENTIFIER ']')? '}'
  public static boolean attribute_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_key")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE_KEY, null);
    r = consumeTokens(b, 1, LBRACE, IDENTIFIER);
    p = r; // pin = 1
    r = r && report_error_(b, attribute_key_2(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('[' IDENTIFIER ']')?
  private static boolean attribute_key_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_key_2")) return false;
    attribute_key_2_0(b, l + 1);
    return true;
  }

  // '[' IDENTIFIER ']'
  private static boolean attribute_key_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_key_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, IDENTIFIER, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '?' IDENTIFIER ('.' IDENTIFIER)*
  public static boolean bind_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter")) return false;
    if (!nextTokenIs(b, QUESTION_MARK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BIND_PARAMETER, null);
    r = consumeTokens(b, 1, QUESTION_MARK, IDENTIFIER);
    p = r; // pin = 1
    r = r && bind_parameter_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('.' IDENTIFIER)*
  private static boolean bind_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!bind_parameter_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "bind_parameter_2", c)) break;
    }
    return true;
  }

  // '.' IDENTIFIER
  private static boolean bind_parameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_parameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '=' | '<>' | '>' | '<' | '>=' | '<='
  public static boolean cmp_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cmp_operator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CMP_OPERATOR, "<cmp operator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, UNEQ);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, GTE);
    if (!r) r = consumeToken(b, LTE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expr_atom ( AND expr_atom )*
  public static boolean expr_and(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_and")) return false;
    if (!nextTokenIs(b, "<expr and>", LBRACE, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_AND, "<expr and>");
    r = expr_atom(b, l + 1);
    r = r && expr_and_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( AND expr_atom )*
  private static boolean expr_and_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_and_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expr_and_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_and_1", c)) break;
    }
    return true;
  }

  // AND expr_atom
  private static boolean expr_and_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_and_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && expr_atom(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // attribute_key cmp_operator bind_parameter
  //     | attribute_key null_operator
  //     | '(' expr_or ')'
  public static boolean expr_atom(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_atom")) return false;
    if (!nextTokenIs(b, "<expr atom>", LBRACE, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_ATOM, "<expr atom>");
    r = expr_atom_0(b, l + 1);
    if (!r) r = expr_atom_1(b, l + 1);
    if (!r) r = expr_atom_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // attribute_key cmp_operator bind_parameter
  private static boolean expr_atom_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_atom_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attribute_key(b, l + 1);
    r = r && cmp_operator(b, l + 1);
    r = r && bind_parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // attribute_key null_operator
  private static boolean expr_atom_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_atom_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attribute_key(b, l + 1);
    r = r && null_operator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expr_or ')'
  private static boolean expr_atom_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_atom_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr_or(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr_and ( OR expr_and )*
  public static boolean expr_or(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_or")) return false;
    if (!nextTokenIs(b, "<expr or>", LBRACE, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_OR, "<expr or>");
    r = expr_and(b, l + 1);
    r = r && expr_or_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( OR expr_and )*
  private static boolean expr_or_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_or_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expr_or_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_or_1", c)) break;
    }
    return true;
  }

  // OR expr_and
  private static boolean expr_or_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_or_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    r = r && expr_and(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // where_clause
  //     | order_by?
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = where_clause(b, l + 1);
    if (!r) r = expression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // order_by?
  private static boolean expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1")) return false;
    order_by(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // GET type_key expression?
  static boolean get_query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "get_query")) return false;
    if (!nextTokenIs(b, GET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, GET);
    p = r; // pin = 1
    r = r && report_error_(b, type_key(b, l + 1));
    r = p && get_query_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean get_query_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "get_query_2")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IS (NOT)? NULL
  public static boolean null_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "null_operator")) return false;
    if (!nextTokenIs(b, IS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NULL_OPERATOR, null);
    r = consumeToken(b, IS);
    p = r; // pin = 1
    r = r && report_error_(b, null_operator_1(b, l + 1));
    r = p && consumeToken(b, NULL) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (NOT)?
  private static boolean null_operator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "null_operator_1")) return false;
    consumeToken(b, NOT);
    return true;
  }

  /* ********************************************************** */
  // order_clause_literal order_key ( ',' order_key )*
  public static boolean order_by(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by")) return false;
    if (!nextTokenIs(b, ORDER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY, null);
    r = order_clause_literal(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, order_key(b, l + 1));
    r = p && order_by_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( ',' order_key )*
  private static boolean order_by_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!order_by_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_by_2", c)) break;
    }
    return true;
  }

  // ',' order_key
  private static boolean order_by_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && order_key(b, l + 1);
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
  // attribute_key (ASC | DESC)?
  public static boolean order_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_key")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attribute_key(b, l + 1);
    r = r && order_key_1(b, l + 1);
    exit_section_(b, m, ORDER_KEY, r);
    return r;
  }

  // (ASC | DESC)?
  private static boolean order_key_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_key_1")) return false;
    order_key_1_0(b, l + 1);
    return true;
  }

  // ASC | DESC
  private static boolean order_key_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_key_1_0")) return false;
    boolean r;
    r = consumeToken(b, ASC);
    if (!r) r = consumeToken(b, DESC);
    return r;
  }

  /* ********************************************************** */
  // get_query
  public static boolean query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY, "<query>");
    r = get_query(b, l + 1);
    exit_section_(b, l, m, r, false, PolyglotQueryParser::query_recover);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | GET)
  static boolean query_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !query_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | GET
  private static boolean query_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, GET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement
  static boolean root(PsiBuilder b, int l) {
    return statement(b, l + 1);
  }

  /* ********************************************************** */
  // (query)*
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statement", c)) break;
    }
    return true;
  }

  // (query)
  private static boolean statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = query(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' IDENTIFIER '}'
  public static boolean type_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_key")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_KEY, null);
    r = consumeTokens(b, 1, LBRACE, IDENTIFIER, RBRACE);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // WHERE expr_or order_by?
  static boolean where_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "where_clause")) return false;
    if (!nextTokenIs(b, WHERE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHERE);
    r = r && expr_or(b, l + 1);
    r = r && where_clause_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // order_by?
  private static boolean where_clause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "where_clause_2")) return false;
    order_by(b, l + 1);
    return true;
  }

}
