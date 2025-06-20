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
package com.intellij.idea.plugin.hybris.acl.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.acl.psi.impl.*;

public interface AclTypes {

  IElementType USER_RIGHTS = new AclElementType("USER_RIGHTS");
  IElementType USER_RIGHTS_BODY = new AclElementType("USER_RIGHTS_BODY");
  IElementType USER_RIGHTS_END = new AclElementType("USER_RIGHTS_END");
  IElementType USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE = new AclElementType("USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE");
  IElementType USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE = new AclElementType("USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE");
  IElementType USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS");
  IElementType USER_RIGHTS_HEADER_PARAMETER_PASSWORD = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_PASSWORD");
  IElementType USER_RIGHTS_HEADER_PARAMETER_PERMISSION = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_PERMISSION");
  IElementType USER_RIGHTS_HEADER_PARAMETER_TARGET = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_TARGET");
  IElementType USER_RIGHTS_HEADER_PARAMETER_TYPE = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_TYPE");
  IElementType USER_RIGHTS_HEADER_PARAMETER_UID = new AclElementType("USER_RIGHTS_HEADER_PARAMETER_UID");
  IElementType USER_RIGHTS_START = new AclElementType("USER_RIGHTS_START");
  IElementType USER_RIGHTS_VALUE_GROUPS = new AclElementType("USER_RIGHTS_VALUE_GROUPS");
  IElementType USER_RIGHTS_VALUE_GROUP_BLANK = new AclElementType("USER_RIGHTS_VALUE_GROUP_BLANK");
  IElementType USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS = new AclElementType("USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS");
  IElementType USER_RIGHTS_VALUE_GROUP_PASSWORD = new AclElementType("USER_RIGHTS_VALUE_GROUP_PASSWORD");
  IElementType USER_RIGHTS_VALUE_GROUP_PERMISSION = new AclElementType("USER_RIGHTS_VALUE_GROUP_PERMISSION");
  IElementType USER_RIGHTS_VALUE_GROUP_TARGET = new AclElementType("USER_RIGHTS_VALUE_GROUP_TARGET");
  IElementType USER_RIGHTS_VALUE_GROUP_TYPE = new AclElementType("USER_RIGHTS_VALUE_GROUP_TYPE");
  IElementType USER_RIGHTS_VALUE_GROUP_UID = new AclElementType("USER_RIGHTS_VALUE_GROUP_UID");
  IElementType USER_RIGHTS_VALUE_GROUP_UID_BLANK = new AclElementType("USER_RIGHTS_VALUE_GROUP_UID_BLANK");
  IElementType USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE = new AclElementType("USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE");
  IElementType USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE = new AclElementType("USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE");
  IElementType USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE = new AclElementType("USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE");
  IElementType USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE = new AclElementType("USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE");
  IElementType USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE = new AclElementType("USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE");
  IElementType USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE = new AclElementType("USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE");
  IElementType USER_RIGHTS_VALUE_PERMISSION = new AclElementType("USER_RIGHTS_VALUE_PERMISSION");
  IElementType USER_RIGHTS_VALUE_TARGET = new AclElementType("USER_RIGHTS_VALUE_TARGET");

  IElementType COMMA = new AclTokenType("COMMA");
  IElementType CRLF = new AclTokenType("CRLF");
  IElementType DOT = new AclTokenType("DOT");
  IElementType DUMMY_SEPARATOR = new AclTokenType("DUMMY_SEPARATOR");
  IElementType END_USERRIGHTS = new AclTokenType("END_USERRIGHTS");
  IElementType FIELD_VALUE = new AclTokenType("FIELD_VALUE");
  IElementType FIELD_VALUE_SEPARATOR = new AclTokenType("FIELD_VALUE_SEPARATOR");
  IElementType FIELD_VALUE_TARGET_ATTRIBUTE = new AclTokenType("FIELD_VALUE_TARGET_ATTRIBUTE");
  IElementType FIELD_VALUE_TARGET_TYPE = new AclTokenType("FIELD_VALUE_TARGET_TYPE");
  IElementType FIELD_VALUE_TYPE = new AclTokenType("FIELD_VALUE_TYPE");
  IElementType FIELD_VALUE_TYPE_SEPARATOR = new AclTokenType("FIELD_VALUE_TYPE_SEPARATOR");
  IElementType HEADER_CHANGE = new AclTokenType("HEADER_CHANGE");
  IElementType HEADER_CHANGE_PERM = new AclTokenType("HEADER_CHANGE_PERM");
  IElementType HEADER_CREATE = new AclTokenType("HEADER_CREATE");
  IElementType HEADER_MEMBEROFGROUPS = new AclTokenType("HEADER_MEMBEROFGROUPS");
  IElementType HEADER_PASSWORD = new AclTokenType("HEADER_PASSWORD");
  IElementType HEADER_READ = new AclTokenType("HEADER_READ");
  IElementType HEADER_REMOVE = new AclTokenType("HEADER_REMOVE");
  IElementType HEADER_TARGET = new AclTokenType("HEADER_TARGET");
  IElementType HEADER_TYPE = new AclTokenType("HEADER_TYPE");
  IElementType HEADER_UID = new AclTokenType("HEADER_UID");
  IElementType LINE_COMMENT = new AclTokenType("LINE_COMMENT");
  IElementType PARAMETERS_SEPARATOR = new AclTokenType("PARAMETERS_SEPARATOR");
  IElementType PASSWORD = new AclTokenType("PASSWORD");
  IElementType PERMISSION_DENIED = new AclTokenType("PERMISSION_DENIED");
  IElementType PERMISSION_GRANTED = new AclTokenType("PERMISSION_GRANTED");
  IElementType PERMISSION_INHERITED = new AclTokenType("PERMISSION_INHERITED");
  IElementType START_USERRIGHTS = new AclTokenType("START_USERRIGHTS");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == USER_RIGHTS) {
        return new AclUserRightsImpl(node);
      }
      else if (type == USER_RIGHTS_BODY) {
        return new AclUserRightsBodyImpl(node);
      }
      else if (type == USER_RIGHTS_END) {
        return new AclUserRightsEndImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_LINE_PASSWORD_AWARE) {
        return new AclUserRightsHeaderLinePasswordAwareImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_LINE_PASSWORD_UNAWARE) {
        return new AclUserRightsHeaderLinePasswordUnawareImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_MEMBER_OF_GROUPS) {
        return new AclUserRightsHeaderParameterMemberOfGroupsImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_PASSWORD) {
        return new AclUserRightsHeaderParameterPasswordImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_PERMISSION) {
        return new AclUserRightsHeaderParameterPermissionImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_TARGET) {
        return new AclUserRightsHeaderParameterTargetImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_TYPE) {
        return new AclUserRightsHeaderParameterTypeImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER_UID) {
        return new AclUserRightsHeaderParameterUidImpl(node);
      }
      else if (type == USER_RIGHTS_START) {
        return new AclUserRightsStartImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUPS) {
        return new AclUserRightsValueGroupsImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_BLANK) {
        return new AclUserRightsValueGroupBlankImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_MEMBER_OF_GROUPS) {
        return new AclUserRightsValueGroupMemberOfGroupsImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_PASSWORD) {
        return new AclUserRightsValueGroupPasswordImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_PERMISSION) {
        return new AclUserRightsValueGroupPermissionImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_TARGET) {
        return new AclUserRightsValueGroupTargetImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_TYPE) {
        return new AclUserRightsValueGroupTypeImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_UID) {
        return new AclUserRightsValueGroupUidImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP_UID_BLANK) {
        return new AclUserRightsValueGroupUidBlankImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE) {
        return new AclUserRightsValueLinesPasswordAwareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE) {
        return new AclUserRightsValueLinesPasswordUnawareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE) {
        return new AclUserRightsValueLinePasswordAwareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE) {
        return new AclUserRightsValueLinePasswordUnawareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE) {
        return new AclUserRightsValueLineTypePasswordAwareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE) {
        return new AclUserRightsValueLineTypePasswordUnawareImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_PERMISSION) {
        return new AclUserRightsValuePermissionImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_TARGET) {
        return new AclUserRightsValueTargetImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
