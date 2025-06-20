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

/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
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
  // (user_rights | LINE_COMMENT | CRLF)*
  static boolean aclFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aclFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!aclFile_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aclFile", c)) break;
    }
    return true;
  }

  // user_rights | LINE_COMMENT | CRLF
  private static boolean aclFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aclFile_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = user_rights(b, l + 1);
    if (!r) r = consumeToken(b, LINE_COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | END_USERRIGHTS)
  static boolean recover_header_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_line")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_header_line_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | END_USERRIGHTS
  private static boolean recover_header_line_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_line_0")) return false;
    boolean r;
    r = consumeToken(b, FIELD_VALUE_TYPE);
    if (!r) r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    if (!r) r = consumeToken(b, END_USERRIGHTS);
    return r;
  }

  /* ********************************************************** */
  // !(PARAMETERS_SEPARATOR | FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | END_USERRIGHTS)
  static boolean recover_header_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_header_parameter_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PARAMETERS_SEPARATOR | FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | END_USERRIGHTS
  private static boolean recover_header_parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_header_parameter_0")) return false;
    boolean r;
    r = consumeToken(b, PARAMETERS_SEPARATOR);
    if (!r) r = consumeToken(b, FIELD_VALUE_TYPE);
    if (!r) r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    if (!r) r = consumeToken(b, END_USERRIGHTS);
    return r;
  }

  /* ********************************************************** */
  // !(START_USERRIGHTS | LINE_COMMENT | CRLF)
  static boolean recover_user_rights(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_user_rights")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_user_rights_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // START_USERRIGHTS | LINE_COMMENT | CRLF
  private static boolean recover_user_rights_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_user_rights_0")) return false;
    boolean r;
    r = consumeToken(b, START_USERRIGHTS);
    if (!r) r = consumeToken(b, LINE_COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    return r;
  }

  /* ********************************************************** */
  // !(END_USERRIGHTS)
  static boolean recover_user_rights_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_user_rights_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, END_USERRIGHTS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS)
  static boolean recover_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_value_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FIELD_VALUE_TYPE | FIELD_VALUE_SEPARATOR | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS
  private static boolean recover_value_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_0")) return false;
    boolean r;
    r = consumeToken(b, FIELD_VALUE_TYPE);
    if (!r) r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    if (!r) r = consumeToken(b, FIELD_VALUE_TYPE_SEPARATOR);
    if (!r) r = consumeToken(b, END_USERRIGHTS);
    return r;
  }

  /* ********************************************************** */
  // !(FIELD_VALUE_TYPE | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS)
  static boolean recover_value_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_line")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_value_line_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FIELD_VALUE_TYPE | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS
  private static boolean recover_value_line_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_line_0")) return false;
    boolean r;
    r = consumeToken(b, FIELD_VALUE_TYPE);
    if (!r) r = consumeToken(b, FIELD_VALUE_TYPE_SEPARATOR);
    if (!r) r = consumeToken(b, END_USERRIGHTS);
    return r;
  }

  /* ********************************************************** */
  // !(FIELD_VALUE_TYPE | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS)
  static boolean recover_value_lines(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_lines")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_value_lines_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FIELD_VALUE_TYPE | FIELD_VALUE_TYPE_SEPARATOR | END_USERRIGHTS
  private static boolean recover_value_lines_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_value_lines_0")) return false;
    boolean r;
    r = consumeToken(b, FIELD_VALUE_TYPE);
    if (!r) r = consumeToken(b, FIELD_VALUE_TYPE_SEPARATOR);
    if (!r) r = consumeToken(b, END_USERRIGHTS);
    return r;
  }

  /* ********************************************************** */
  // user_rights_start user_rights_body? user_rights_end
  public static boolean user_rights(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS, "<user rights>");
    r = user_rights_start(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_1(b, l + 1));
    r = p && user_rights_end(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_user_rights);
    return r || p;
  }

  // user_rights_body?
  private static boolean user_rights_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_1")) return false;
    user_rights_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // user_rights_body_password_aware | user_rights_body_password_unaware
  public static boolean user_rights_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_BODY, "<user rights body>");
    r = user_rights_body_password_aware(b, l + 1);
    if (!r) r = user_rights_body_password_unaware(b, l + 1);
    exit_section_(b, l, m, r, false, AclParser::recover_user_rights_body);
    return r;
  }

  /* ********************************************************** */
  // user_rights_header_line_password_aware user_rights_value_lines_password_aware*
  static boolean user_rights_body_password_aware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_password_aware")) return false;
    if (!nextTokenIs(b, HEADER_TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = user_rights_header_line_password_aware(b, l + 1);
    p = r; // pin = 1
    r = r && user_rights_body_password_aware_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // user_rights_value_lines_password_aware*
  private static boolean user_rights_body_password_aware_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_password_aware_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_lines_password_aware(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_body_password_aware_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_header_line_password_unaware user_rights_value_lines_password_unaware*
  static boolean user_rights_body_password_unaware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_password_unaware")) return false;
    if (!nextTokenIs(b, HEADER_TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = user_rights_header_line_password_unaware(b, l + 1);
    p = r; // pin = 1
    r = r && user_rights_body_password_unaware_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // user_rights_value_lines_password_unaware*
  private static boolean user_rights_body_password_unaware_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_body_password_unaware_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_lines_password_unaware(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_body_password_unaware_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // END_USERRIGHTS DUMMY_SEPARATOR*
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

  // DUMMY_SEPARATOR*
  private static boolean user_rights_end_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_end_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, DUMMY_SEPARATOR)) break;
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
  public static boolean user_rights_header_line_password_aware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line_password_aware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE, "<user rights header line password aware>");
    r = user_rights_header_parameter_type(b, l + 1);
    r = r && user_rights_header_parameter_uid(b, l + 1);
    r = r && user_rights_header_parameter_member_of_groups(b, l + 1);
    r = r && user_rights_header_parameter_password(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, user_rights_header_parameter_target(b, l + 1));
    r = p && user_rights_header_line_password_aware_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  // user_rights_header_parameter_permission*
  private static boolean user_rights_header_line_password_aware_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line_password_aware_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_header_parameter_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_header_line_password_aware_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_header_parameter_type
  //     user_rights_header_parameter_uid
  //     user_rights_header_parameter_member_of_groups
  //     user_rights_header_parameter_target
  //     user_rights_header_parameter_permission*
  public static boolean user_rights_header_line_password_unaware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line_password_unaware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE, "<user rights header line password unaware>");
    r = user_rights_header_parameter_type(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_header_parameter_uid(b, l + 1));
    r = p && report_error_(b, user_rights_header_parameter_member_of_groups(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_header_parameter_target(b, l + 1)) && r;
    r = p && user_rights_header_line_password_unaware_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_header_line);
    return r || p;
  }

  // user_rights_header_parameter_permission*
  private static boolean user_rights_header_line_password_unaware_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_line_password_unaware_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_header_parameter_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_header_line_password_unaware_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_MEMBEROFGROUPS
  public static boolean user_rights_header_parameter_member_of_groups(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_member_of_groups")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS, "<user rights header parameter member of groups>");
    r = consumeTokens(b, 2, PARAMETERS_SEPARATOR, HEADER_MEMBEROFGROUPS);
    exit_section_(b, l, m, r, false, AclParser::recover_header_parameter);
    return r;
  }

  /* ********************************************************** */
  // PARAMETERS_SEPARATOR HEADER_PASSWORD
  public static boolean user_rights_header_parameter_password(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_header_parameter_password")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_PASSWORD, "<user rights header parameter password>");
    r = consumeTokens(b, 2, PARAMETERS_SEPARATOR, HEADER_PASSWORD);
    exit_section_(b, l, m, r, false, AclParser::recover_header_parameter);
    return r;
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
    exit_section_(b, l, m, r, p, AclParser::recover_header_parameter);
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
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_TARGET, "<user rights header parameter target>");
    r = consumeTokens(b, 2, PARAMETERS_SEPARATOR, HEADER_TARGET);
    exit_section_(b, l, m, r, false, AclParser::recover_header_parameter);
    return r;
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
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_HEADER_PARAMETER_UID, "<user rights header parameter uid>");
    r = consumeTokens(b, 2, PARAMETERS_SEPARATOR, HEADER_UID);
    exit_section_(b, l, m, r, false, AclParser::recover_header_parameter);
    return r;
  }

  /* ********************************************************** */
  // START_USERRIGHTS DUMMY_SEPARATOR*
  public static boolean user_rights_start(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_start")) return false;
    if (!nextTokenIs(b, START_USERRIGHTS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, START_USERRIGHTS);
    r = r && user_rights_start_1(b, l + 1);
    exit_section_(b, m, USER_RIGHTS_START, r);
    return r;
  }

  // DUMMY_SEPARATOR*
  private static boolean user_rights_start_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_start_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, DUMMY_SEPARATOR)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_start_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR
  public static boolean user_rights_value_group_blank(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_blank")) return false;
    if (!nextTokenIs(b, FIELD_VALUE_SEPARATOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    exit_section_(b, m, USER_RIGHTS_VALUE_GROUP_BLANK, r);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR user_rights_value_groups?
  public static boolean user_rights_value_group_member_of_groups(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_member_of_groups")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS, "<user rights value group member of groups>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_group_member_of_groups_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value);
    return r || p;
  }

  // user_rights_value_groups?
  private static boolean user_rights_value_group_member_of_groups_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_member_of_groups_1")) return false;
    user_rights_value_groups(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR PASSWORD?
  public static boolean user_rights_value_group_password(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_password")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_PASSWORD, "<user rights value group password>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_group_password_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value);
    return r || p;
  }

  // PASSWORD?
  private static boolean user_rights_value_group_password_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_password_1")) return false;
    consumeToken(b, PASSWORD);
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR user_rights_value_permission?
  public static boolean user_rights_value_group_permission(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_permission")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_PERMISSION, "<user rights value group permission>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_group_permission_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value);
    return r || p;
  }

  // user_rights_value_permission?
  private static boolean user_rights_value_group_permission_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_permission_1")) return false;
    user_rights_value_permission(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FIELD_VALUE_SEPARATOR user_rights_value_target
  public static boolean user_rights_value_group_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_target")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_TARGET, "<user rights value group target>");
    r = consumeToken(b, FIELD_VALUE_SEPARATOR);
    p = r; // pin = 1
    r = r && user_rights_value_target(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value);
    return r || p;
  }

  /* ********************************************************** */
  // FIELD_VALUE_TYPE
  public static boolean user_rights_value_group_type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_type")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_TYPE, "<user rights value group type>");
    r = consumeToken(b, FIELD_VALUE_TYPE);
    exit_section_(b, l, m, r, false, AclParser::recover_value);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE_TYPE_SEPARATOR FIELD_VALUE
  public static boolean user_rights_value_group_uid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_uid")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_GROUP_UID, "<user rights value group uid>");
    r = consumeTokens(b, 1, FIELD_VALUE_TYPE_SEPARATOR, FIELD_VALUE);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, AclParser::recover_value);
    return r || p;
  }

  /* ********************************************************** */
  // FIELD_VALUE_TYPE_SEPARATOR
  public static boolean user_rights_value_group_uid_blank(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_group_uid_blank")) return false;
    if (!nextTokenIs(b, FIELD_VALUE_TYPE_SEPARATOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE_TYPE_SEPARATOR);
    exit_section_(b, m, USER_RIGHTS_VALUE_GROUP_UID_BLANK, r);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE (COMMA FIELD_VALUE)*
  public static boolean user_rights_value_groups(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_groups")) return false;
    if (!nextTokenIs(b, FIELD_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE);
    r = r && user_rights_value_groups_1(b, l + 1);
    exit_section_(b, m, USER_RIGHTS_VALUE_GROUPS, r);
    return r;
  }

  // (COMMA FIELD_VALUE)*
  private static boolean user_rights_value_groups_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_groups_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_groups_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_groups_1", c)) break;
    }
    return true;
  }

  // COMMA FIELD_VALUE
  private static boolean user_rights_value_groups_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_groups_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, FIELD_VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // user_rights_value_group_uid_blank
  //     user_rights_value_group_blank
  //     user_rights_value_group_blank
  //     user_rights_value_group_target
  //     user_rights_value_group_permission*
  public static boolean user_rights_value_line_password_aware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_password_aware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE, "<user rights value line password aware>");
    r = user_rights_value_group_uid_blank(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_group_blank(b, l + 1));
    r = p && report_error_(b, user_rights_value_group_blank(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_value_group_target(b, l + 1)) && r;
    r = p && user_rights_value_line_password_aware_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // user_rights_value_group_permission*
  private static boolean user_rights_value_line_password_aware_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_password_aware_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_group_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_line_password_aware_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_value_group_uid_blank
  //     user_rights_value_group_blank
  //     user_rights_value_group_target
  //     user_rights_value_group_permission*
  public static boolean user_rights_value_line_password_unaware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_password_unaware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE, "<user rights value line password unaware>");
    r = user_rights_value_group_uid_blank(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_group_blank(b, l + 1));
    r = p && report_error_(b, user_rights_value_group_target(b, l + 1)) && r;
    r = p && user_rights_value_line_password_unaware_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // user_rights_value_group_permission*
  private static boolean user_rights_value_line_password_unaware_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_password_unaware_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_group_permission(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_line_password_unaware_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_value_group_type
  //     user_rights_value_group_uid
  //     user_rights_value_group_member_of_groups?
  //     user_rights_value_group_password?
  //     user_rights_value_group_blank*
  public static boolean user_rights_value_line_type_password_aware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_aware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE, "<user rights value line type password aware>");
    r = user_rights_value_group_type(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_group_uid(b, l + 1));
    r = p && report_error_(b, user_rights_value_line_type_password_aware_2(b, l + 1)) && r;
    r = p && report_error_(b, user_rights_value_line_type_password_aware_3(b, l + 1)) && r;
    r = p && user_rights_value_line_type_password_aware_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // user_rights_value_group_member_of_groups?
  private static boolean user_rights_value_line_type_password_aware_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_aware_2")) return false;
    user_rights_value_group_member_of_groups(b, l + 1);
    return true;
  }

  // user_rights_value_group_password?
  private static boolean user_rights_value_line_type_password_aware_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_aware_3")) return false;
    user_rights_value_group_password(b, l + 1);
    return true;
  }

  // user_rights_value_group_blank*
  private static boolean user_rights_value_line_type_password_aware_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_aware_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_group_blank(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_line_type_password_aware_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_value_group_type
  //     user_rights_value_group_uid
  //     user_rights_value_group_member_of_groups?
  //     user_rights_value_group_blank*
  public static boolean user_rights_value_line_type_password_unaware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_unaware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE, "<user rights value line type password unaware>");
    r = user_rights_value_group_type(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, user_rights_value_group_uid(b, l + 1));
    r = p && report_error_(b, user_rights_value_line_type_password_unaware_2(b, l + 1)) && r;
    r = p && user_rights_value_line_type_password_unaware_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, AclParser::recover_value_line);
    return r || p;
  }

  // user_rights_value_group_member_of_groups?
  private static boolean user_rights_value_line_type_password_unaware_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_unaware_2")) return false;
    user_rights_value_group_member_of_groups(b, l + 1);
    return true;
  }

  // user_rights_value_group_blank*
  private static boolean user_rights_value_line_type_password_unaware_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_line_type_password_unaware_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_group_blank(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_line_type_password_unaware_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_value_line_type_password_aware user_rights_value_line_password_aware*
  public static boolean user_rights_value_lines_password_aware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_lines_password_aware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE, "<user rights value lines password aware>");
    r = user_rights_value_line_type_password_aware(b, l + 1);
    p = r; // pin = 1
    r = r && user_rights_value_lines_password_aware_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value_lines);
    return r || p;
  }

  // user_rights_value_line_password_aware*
  private static boolean user_rights_value_lines_password_aware_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_lines_password_aware_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_line_password_aware(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_lines_password_aware_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // user_rights_value_line_type_password_unaware user_rights_value_line_password_unaware*
  public static boolean user_rights_value_lines_password_unaware(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_lines_password_unaware")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE, "<user rights value lines password unaware>");
    r = user_rights_value_line_type_password_unaware(b, l + 1);
    p = r; // pin = 1
    r = r && user_rights_value_lines_password_unaware_1(b, l + 1);
    exit_section_(b, l, m, r, p, AclParser::recover_value_lines);
    return r || p;
  }

  // user_rights_value_line_password_unaware*
  private static boolean user_rights_value_lines_password_unaware_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_lines_password_unaware_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!user_rights_value_line_password_unaware(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "user_rights_value_lines_password_unaware_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PERMISSION_DENIED | PERMISSION_GRANTED | PERMISSION_INHERITED
  public static boolean user_rights_value_permission(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_permission")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_PERMISSION, "<user rights value permission>");
    r = consumeToken(b, PERMISSION_DENIED);
    if (!r) r = consumeToken(b, PERMISSION_GRANTED);
    if (!r) r = consumeToken(b, PERMISSION_INHERITED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE_TARGET_TYPE? (DOT FIELD_VALUE_TARGET_ATTRIBUTE)?
  public static boolean user_rights_value_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USER_RIGHTS_VALUE_TARGET, "<user rights value target>");
    r = user_rights_value_target_0(b, l + 1);
    p = r; // pin = 1
    r = r && user_rights_value_target_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FIELD_VALUE_TARGET_TYPE?
  private static boolean user_rights_value_target_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_0")) return false;
    consumeToken(b, FIELD_VALUE_TARGET_TYPE);
    return true;
  }

  // (DOT FIELD_VALUE_TARGET_ATTRIBUTE)?
  private static boolean user_rights_value_target_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_1")) return false;
    user_rights_value_target_1_0(b, l + 1);
    return true;
  }

  // DOT FIELD_VALUE_TARGET_ATTRIBUTE
  private static boolean user_rights_value_target_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "user_rights_value_target_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, FIELD_VALUE_TARGET_ATTRIBUTE);
    exit_section_(b, m, null, r);
    return r;
  }

}
