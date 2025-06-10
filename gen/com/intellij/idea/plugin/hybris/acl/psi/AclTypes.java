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
package com.intellij.idea.plugin.hybris.acl.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.acl.psi.impl.*;

public interface AclTypes {

  IElementType COMMENT = new AclElementType("COMMENT");
  IElementType USER_RIGHTS = new AclElementType("USER_RIGHTS");
  IElementType USER_RIGHTS_ATTRIBUTE_VALUE = new AclElementType("USER_RIGHTS_ATTRIBUTE_VALUE");
  IElementType USER_RIGHTS_END = new AclElementType("USER_RIGHTS_END");
  IElementType USER_RIGHTS_FIRST_VALUE_GROUP = new AclElementType("USER_RIGHTS_FIRST_VALUE_GROUP");
  IElementType USER_RIGHTS_HEADER_LINE = new AclElementType("USER_RIGHTS_HEADER_LINE");
  IElementType USER_RIGHTS_HEADER_PARAMETER = new AclElementType("USER_RIGHTS_HEADER_PARAMETER");
  IElementType USER_RIGHTS_MULTI_VALUE = new AclElementType("USER_RIGHTS_MULTI_VALUE");
  IElementType USER_RIGHTS_PERMISSION_VALUE = new AclElementType("USER_RIGHTS_PERMISSION_VALUE");
  IElementType USER_RIGHTS_SINGLE_VALUE = new AclElementType("USER_RIGHTS_SINGLE_VALUE");
  IElementType USER_RIGHTS_START = new AclElementType("USER_RIGHTS_START");
  IElementType USER_RIGHTS_VALUE_GROUP = new AclElementType("USER_RIGHTS_VALUE_GROUP");
  IElementType USER_RIGHTS_VALUE_LINE = new AclElementType("USER_RIGHTS_VALUE_LINE");

  IElementType COMMA = new AclTokenType("COMMA");
  IElementType CRLF = new AclTokenType("CRLF");
  IElementType DOT = new AclTokenType("DOT");
  IElementType END_USERRIGHTS = new AclTokenType("END_USERRIGHTS");
  IElementType FIELD_VALUE = new AclTokenType("FIELD_VALUE");
  IElementType FIELD_VALUE_SEPARATOR = new AclTokenType("FIELD_VALUE_SEPARATOR");
  IElementType LINE_COMMENT = new AclTokenType("LINE_COMMENT");
  IElementType MEMBEROFGROUPS = new AclTokenType("MEMBEROFGROUPS");
  IElementType MULTILINE_SEPARATOR = new AclTokenType("MULTILINE_SEPARATOR");
  IElementType PARAMETERS_SEPARATOR = new AclTokenType("PARAMETERS_SEPARATOR");
  IElementType PASSWORD = new AclTokenType("PASSWORD");
  IElementType PERMISSION = new AclTokenType("PERMISSION");
  IElementType PERMISSION_ALLOWED = new AclTokenType("PERMISSION_ALLOWED");
  IElementType PERMISSION_DENIED = new AclTokenType("PERMISSION_DENIED");
  IElementType START_USERRIGHTS = new AclTokenType("START_USERRIGHTS");
  IElementType STRING = new AclTokenType("string");
  IElementType TARGET = new AclTokenType("TARGET");
  IElementType TYPE = new AclTokenType("TYPE");
  IElementType UID = new AclTokenType("UID");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COMMENT) {
        return new AclCommentImpl(node);
      }
      else if (type == USER_RIGHTS) {
        return new AclUserRightsImpl(node);
      }
      else if (type == USER_RIGHTS_ATTRIBUTE_VALUE) {
        return new AclUserRightsAttributeValueImpl(node);
      }
      else if (type == USER_RIGHTS_END) {
        return new AclUserRightsEndImpl(node);
      }
      else if (type == USER_RIGHTS_FIRST_VALUE_GROUP) {
        return new AclUserRightsFirstValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_LINE) {
        return new AclUserRightsHeaderLineImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER) {
        return new AclUserRightsHeaderParameterImpl(node);
      }
      else if (type == USER_RIGHTS_MULTI_VALUE) {
        return new AclUserRightsMultiValueImpl(node);
      }
      else if (type == USER_RIGHTS_PERMISSION_VALUE) {
        return new AclUserRightsPermissionValueImpl(node);
      }
      else if (type == USER_RIGHTS_SINGLE_VALUE) {
        return new AclUserRightsSingleValueImpl(node);
      }
      else if (type == USER_RIGHTS_START) {
        return new AclUserRightsStartImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP) {
        return new AclUserRightsValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE) {
        return new AclUserRightsValueLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
