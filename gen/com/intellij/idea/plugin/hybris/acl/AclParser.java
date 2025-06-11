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
package com.intellij.idea.plugin.hybris.acl;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.idea.plugin.hybris.acl.psi.AclTypes.*;
import static com.intellij.idea.plugin.hybris.acl.utils.AclParserUtils.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class AclParser implements PsiParser, LightPsiParser {

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
    return aclFile(b, l + 1);
  }

  /* ********************************************************** */
  // root*
  static boolean aclFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aclFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aclFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LINE_COMMENT
  public static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!nextTokenIs(b, LINE_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LINE_COMMENT);
    exit_section_(b, m, COMMENT, r);
    return r;
  }

  /* ********************************************************** */
  // !(CRLF)
  static boolean not_line_break(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_line_break")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, CRLF);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(CRLF | PARAMETERS_SEPARATOR)
  static boolean recover_header_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_line")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_header_line_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CRLF | PARAMETERS_SEPARATOR
  private static boolean recover_header_line_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_line_0")) return false;
    boolean r;
    r = consumeToken(b, CRLF);
    if (!r) r = consumeToken(b, PARAMETERS_SEPARATOR);
    return r;
  }

  /* ********************************************************** */
  // !(
  //      CRLF
  //   |  user_rights_start
  //   |  LINE_COMMENT
  // )
  static boolean recover_root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_root")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_root_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CRLF
  //   |  user_rights_start
  //   |  LINE_COMMENT
  private static boolean recover_root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_root_0")) return false;
    boolean r;
    r = consumeToken(b, CRLF);
    if (!r) r = user_rights_start(b, l + 1);
    if (!r) r = consumeToken(b, LINE_COMMENT);
    return r;
  }

  /* ********************************************************** */
  // !(CRLF | FIELD_VALUE_SEPARATOR)
  static boolean recover_value_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_line")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_value_line_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CRLF | FIELD_VALUE_SEPARATOR
  private static boolean recover_value_line_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_line_0")) return false;
    boolean r;
    r = consumeToken(b, CRLF);
    if (!r) r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    return r;
  }

  /* ********************************************************** */
  // CRLF | ( !<<eof>> root_group (CRLF | <<eof>> ))
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, CRLF);
    if (!r) r = root_1(b, l + 1);
    exit_section_(b, l, m, r, false, AclParser::recover_root);
    return r;
  }

  // !<<eof>> root_group (CRLF | <<eof>> )
  private static boolean root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_1_0(b, l + 1);
    r = r && root_group(b, l + 1);
    r = r && root_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !<<eof>>
  private static boolean root_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !eof(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CRLF | <<eof>>
  private static boolean root_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CRLF);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // user_rights
  //     | comment
  //     | (string (';')?)
  static boolean root_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = user_rights(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = root_group_2(b, l + 1);
    exit_section_(b, l, m, r, false, AclParser::not_line_break);
    return r;
  }

  // string (';')?
  private static boolean root_group_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING);
    r = r && root_group_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (';')?
  private static boolean root_group_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_2_1")) return false;
    root_group_2_1_0(b, l + 1);
    return true;
  }

  // (';')
  private static boolean root_group_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ";");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // user_rights_start (PARAMETERS_SEPARATOR)* CRLF+ user_rights_body user_rights_end
  public static boolean user_rights(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS, "<user rights>");
    r = user_rights_start(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_1(b, l + 1));
    r = p && report_error_(b, user_rights_2(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_body(b, l + 1)) && r;
    r = p && user_rights_end(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_root);
    return r || p;
  }

  // (PARAMETERS_SEPARATOR)*
  private static boolean user_rights_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, PARAMETERS_SEPARATOR)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_1", c)) break;
    }
    return true;
  }

  // CRLF+
  private static boolean user_rights_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CRLF);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, CRLF)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (user_rights_header_line CRLF+) (user_rights_value_line CRLF+)*
  static boolean user_rights_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = user_rights_body_0(b, l + 1);
    r = r && user_rights_body_1(b, l + 1);
    exit_section_(b, l, m, r, false, AclParser::user_rights_body_recover);
    return r;
  }

  // user_rights_header_line CRLF+
  private static boolean user_rights_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = user_rights_header_line(b, l + 1);
    r = r && user_rights_body_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CRLF+
  private static boolean user_rights_body_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CRLF);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, CRLF)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_body_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (user_rights_value_line CRLF+)*
  private static boolean user_rights_body_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_body_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_body_1", c)) break;
    }
    return true;
  }

  // user_rights_value_line CRLF+
  private static boolean user_rights_body_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = user_rights_value_line(b, l + 1);
    r = r && user_rights_body_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CRLF+
  private static boolean user_rights_body_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CRLF);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, CRLF)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_body_1_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(user_rights_end)
  static boolean user_rights_body_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !user_rights_body_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (user_rights_end)
  private static boolean user_rights_body_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = user_rights_end(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // END_USERRIGHTS (PARAMETERS_SEPARATOR)*
  public static boolean user_rights_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_end")) return false;
    if (!nextTokenIs(b, END_USERRIGHTS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_USERRIGHTS);
    r = r && user_rights_end_1(b, l + 1);
    exit_section_(b, m, USER_RIGHTS_END, r);
    return r;
  }

  // (PARAMETERS_SEPARATOR)*
  private static boolean user_rights_end_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_end_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, PARAMETERS_SEPARATOR)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_end_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_header_parameter_type
  //     user_rights_header_parameter_uid
  //     user_rights_header_parameter_member_of_groups
  //     user_rights_header_parameter_password
  //     user_rights_header_parameter_target
  //     user_rights_header_parameter_permission*
  public static boolean user_rights_header_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_LINE, "<user rights header line>");
    r = user_rights_header_parameter_type(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_header_parameter_uid(b, l + 1));
    r = p && report_error_(b, user_rights_header_parameter_member_of_groups(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_header_parameter_password(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_header_parameter_target(b, l + 1)) && r;
    r = p && user_rights_header_line_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::not_line_break);
    return r || p;
  }

  // user_rights_header_parameter_permission*
  private static boolean user_rights_header_line_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_header_parameter_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_header_line_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_MEMBEROFGROUPS
  public static boolean user_rights_header_parameter_member_of_groups(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_member_of_groups")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS, "<user rights header parameter member of groups>");
    r = consumeTokens(b, 1, PARAMETERS_SEPARATOR, HEADER_MEMBEROFGROUPS);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_PASSWORD
  public static boolean user_rights_header_parameter_password(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_password")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_PASSWORD, "<user rights header parameter password>");
    r = consumeTokens(b, 1, PARAMETERS_SEPARATOR, HEADER_PASSWORD);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR (
  //     HEADER_READ
  //     | HEADER_CHANGE
  //     | HEADER_CREATE
  //     | HEADER_REMOVE
  //     | HEADER_CHANGE_PERM
  // )
  public static boolean user_rights_header_parameter_permission(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_permission")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_PERMISSION, "<user rights header parameter permission>");
    r = consumeToken(b, PARAMETERS_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_header_parameter_permission_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  // HEADER_READ
  //     | HEADER_CHANGE
  //     | HEADER_CREATE
  //     | HEADER_REMOVE
  //     | HEADER_CHANGE_PERM
  private static boolean user_rights_header_parameter_permission_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_permission_1")) return false;
    boolean r;
    r = consumeToken(b, HEADER_READ);
    if (!r) r = consumeToken(b, HEADER_CHANGE);
    if (!r) r = consumeToken(b, HEADER_CREATE);
    if (!r) r = consumeToken(b, HEADER_REMOVE);
    if (!r) r = consumeToken(b, HEADER_CHANGE_PERM);
    return r;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_TARGET
  public static boolean user_rights_header_parameter_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_target")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_TARGET, "<user rights header parameter target>");
    r = consumeTokens(b, 1, PARAMETERS_SEPARATOR, HEADER_TARGET);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  /* ********************************************************** */
  // HEADER_TYPE
  public static boolean user_rights_header_parameter_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_type")) return false;
    if (!nextTokenIs(b, HEADER_TYPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HEADER_TYPE);
    exit_section_(b, m, USER_RIGHTS_HEADER_PARAMETER_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_UID
  public static boolean user_rights_header_parameter_uid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_uid")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_UID, "<user rights header parameter uid>");
    r = consumeTokens(b, 1, PARAMETERS_SEPARATOR, HEADER_UID);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  /* ********************************************************** */
  // START_USERRIGHTS
  public static boolean user_rights_start(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_start")) return false;
    if (!nextTokenIs(b, START_USERRIGHTS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, START_USERRIGHTS);
    exit_section_(b, m, USER_RIGHTS_START, r);
    return r;
  }

  /* ********************************************************** */
  // user_rights_value_type
  //     user_rights_value_uid
  //     user_rights_value_member_of_groups
  //     user_rights_value_password?
  //     user_rights_value_target
  //     user_rights_value_permission*
  public static boolean user_rights_value_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINE, "<user rights value line>");
    r = user_rights_value_type(b, l + 1);
    r = r && user_rights_value_uid(b, l + 1);
    r = r && user_rights_value_member_of_groups(b, l + 1);
    r = r && user_rights_value_line_3(b, l + 1);
    r = r && user_rights_value_target(b, l + 1);
    r = r && user_rights_value_line_5(b, l + 1);
    exit_section_(b, l, m, r, false, AclParser::not_line_break);
    return r;
  }

  // user_rights_value_password?
  private static boolean user_rights_value_line_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_3")) return false;
    user_rights_value_password(b, l + 1);
    return true;
  }

  // user_rights_value_permission*
  private static boolean user_rights_value_line_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_line_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR FIELD_VALUE? (COMMA FIELD_VALUE)*
  public static boolean user_rights_value_member_of_groups(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_member_of_groups")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_MEMBER_OF_GROUPS, "<user rights value member of groups>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_member_of_groups_1(b, l + 1));
    r = p && user_rights_value_member_of_groups_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // FIELD_VALUE?
  private static boolean user_rights_value_member_of_groups_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_member_of_groups_1")) return false;
    consumeToken(b, FIELD_VALUE);
    return true;
  }

  // (COMMA FIELD_VALUE)*
  private static boolean user_rights_value_member_of_groups_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_member_of_groups_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_member_of_groups_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_member_of_groups_2", c)) break;
    }
    return true;
  }

  // COMMA FIELD_VALUE
  private static boolean user_rights_value_member_of_groups_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_member_of_groups_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, FIELD_VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR FIELD_VALUE?
  public static boolean user_rights_value_password(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_password")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_PASSWORD, "<user rights value password>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_password_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // FIELD_VALUE?
  private static boolean user_rights_value_password_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_password_1")) return false;
    consumeToken(b, FIELD_VALUE);
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR (PERMISSION_DENIED | PERMISSION_ALLOWED | PERMISSION_INHERITED)?
  public static boolean user_rights_value_permission(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_permission")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_PERMISSION, "<user rights value permission>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_permission_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // (PERMISSION_DENIED | PERMISSION_ALLOWED | PERMISSION_INHERITED)?
  private static boolean user_rights_value_permission_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_permission_1")) return false;
    user_rights_value_permission_1_0(b, l + 1);
    return true;
  }

  // PERMISSION_DENIED | PERMISSION_ALLOWED | PERMISSION_INHERITED
  private static boolean user_rights_value_permission_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_permission_1_0")) return false;
    boolean r;
    r = consumeToken(b, PERMISSION_DENIED);
    if (!r) r = consumeToken(b, PERMISSION_ALLOWED);
    if (!r) r = consumeToken(b, PERMISSION_INHERITED);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR FIELD_VALUE? (DOT FIELD_VALUE)?
  public static boolean user_rights_value_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_TARGET, "<user rights value target>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_target_1(b, l + 1));
    r = p && user_rights_value_target_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // FIELD_VALUE?
  private static boolean user_rights_value_target_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_1")) return false;
    consumeToken(b, FIELD_VALUE);
    return true;
  }

  // (DOT FIELD_VALUE)?
  private static boolean user_rights_value_target_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_2")) return false;
    user_rights_value_target_2_0(b, l + 1);
    return true;
  }

  // DOT FIELD_VALUE
  private static boolean user_rights_value_target_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, FIELD_VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE?
  public static boolean user_rights_value_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_type")) return false;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_TYPE, "<user rights value type>");
    consumeToken(b, FIELD_VALUE);
    exit_section_(b, l, m, true, false, AclParser::recover_value_line);
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR FIELD_VALUE?
  public static boolean user_rights_value_uid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_uid")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_UID, "<user rights value uid>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_uid_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // FIELD_VALUE?
  private static boolean user_rights_value_uid_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_uid_1")) return false;
    consumeToken(b, FIELD_VALUE);
    return true;
  }

}
