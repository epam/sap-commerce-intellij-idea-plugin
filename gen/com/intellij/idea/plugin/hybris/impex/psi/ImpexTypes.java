/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * ----------------------------------------------------------------
 *
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.impex.psi.impl.*;

public interface ImpexTypes {

  IElementType ANY_ATTRIBUTE_NAME = new ImpexElementType("ANY_ATTRIBUTE_NAME");
  IElementType ANY_ATTRIBUTE_VALUE = new ImpexElementType("ANY_ATTRIBUTE_VALUE");
  IElementType ANY_HEADER_MODE = new ImpexElementType("ANY_HEADER_MODE");
  IElementType ANY_HEADER_PARAMETER_NAME = new ImpexElementType("ANY_HEADER_PARAMETER_NAME");
  IElementType ATTRIBUTE = new ImpexElementType("ATTRIBUTE");
  IElementType BEAN_SHELL = new ImpexElementType("BEAN_SHELL");
  IElementType COMMENT = new ImpexElementType("COMMENT");
  IElementType FULL_HEADER_PARAMETER = new ImpexElementType("FULL_HEADER_PARAMETER");
  IElementType FULL_HEADER_TYPE = new ImpexElementType("FULL_HEADER_TYPE");
  IElementType HEADER_LINE = new ImpexElementType("HEADER_LINE");
  IElementType HEADER_TYPE_NAME = new ImpexElementType("HEADER_TYPE_NAME");
  IElementType MACRO_DECLARATION = new ImpexElementType("MACRO_DECLARATION");
  IElementType MACRO_NAME_DEC = new ImpexElementType("MACRO_NAME_DEC");
  IElementType MACRO_USAGE_DEC = new ImpexElementType("MACRO_USAGE_DEC");
  IElementType MACRO_VALUE_DEC = new ImpexElementType("MACRO_VALUE_DEC");
  IElementType MODIFIERS = new ImpexElementType("MODIFIERS");
  IElementType PARAMETER = new ImpexElementType("PARAMETER");
  IElementType PARAMETERS = new ImpexElementType("PARAMETERS");
  IElementType ROOT_MACRO_USAGE = new ImpexElementType("ROOT_MACRO_USAGE");
  IElementType STRING = new ImpexElementType("STRING");
  IElementType SUB_PARAMETERS = new ImpexElementType("SUB_PARAMETERS");
  IElementType SUB_TYPE_NAME = new ImpexElementType("SUB_TYPE_NAME");
  IElementType USER_RIGHTS = new ImpexElementType("USER_RIGHTS");
  IElementType USER_RIGHTS_ATTRIBUTE_VALUE = new ImpexElementType("USER_RIGHTS_ATTRIBUTE_VALUE");
  IElementType USER_RIGHTS_END = new ImpexElementType("USER_RIGHTS_END");
  IElementType USER_RIGHTS_FIRST_VALUE_GROUP = new ImpexElementType("USER_RIGHTS_FIRST_VALUE_GROUP");
  IElementType USER_RIGHTS_HEADER_LINE = new ImpexElementType("USER_RIGHTS_HEADER_LINE");
  IElementType USER_RIGHTS_HEADER_PARAMETER = new ImpexElementType("USER_RIGHTS_HEADER_PARAMETER");
  IElementType USER_RIGHTS_MULTI_VALUE = new ImpexElementType("USER_RIGHTS_MULTI_VALUE");
  IElementType USER_RIGHTS_PERMISSION_VALUE = new ImpexElementType("USER_RIGHTS_PERMISSION_VALUE");
  IElementType USER_RIGHTS_SINGLE_VALUE = new ImpexElementType("USER_RIGHTS_SINGLE_VALUE");
  IElementType USER_RIGHTS_START = new ImpexElementType("USER_RIGHTS_START");
  IElementType USER_RIGHTS_VALUE_GROUP = new ImpexElementType("USER_RIGHTS_VALUE_GROUP");
  IElementType USER_RIGHTS_VALUE_LINE = new ImpexElementType("USER_RIGHTS_VALUE_LINE");
  IElementType VALUE = new ImpexElementType("VALUE");
  IElementType VALUE_GROUP = new ImpexElementType("VALUE_GROUP");
  IElementType VALUE_LINE = new ImpexElementType("VALUE_LINE");

  IElementType ALTERNATIVE_MAP_DELIMITER = new ImpexTokenType("ALTERNATIVE_MAP_DELIMITER");
  IElementType ALTERNATIVE_PATTERN = new ImpexTokenType("ALTERNATIVE_PATTERN");
  IElementType ASSIGN_VALUE = new ImpexTokenType("ASSIGN_VALUE");
  IElementType ATTRIBUTE_NAME = new ImpexTokenType("ATTRIBUTE_NAME");
  IElementType ATTRIBUTE_SEPARATOR = new ImpexTokenType("ATTRIBUTE_SEPARATOR");
  IElementType ATTRIBUTE_VALUE = new ImpexTokenType("ATTRIBUTE_VALUE");
  IElementType BEAN_SHELL_BODY = new ImpexTokenType("BEAN_SHELL_BODY");
  IElementType BEAN_SHELL_MARKER = new ImpexTokenType("BEAN_SHELL_MARKER");
  IElementType BOOLEAN = new ImpexTokenType("BOOLEAN");
  IElementType COLLECTION_APPEND_PREFIX = new ImpexTokenType("COLLECTION_APPEND_PREFIX");
  IElementType COLLECTION_MERGE_PREFIX = new ImpexTokenType("COLLECTION_MERGE_PREFIX");
  IElementType COLLECTION_REMOVE_PREFIX = new ImpexTokenType("COLLECTION_REMOVE_PREFIX");
  IElementType COMMA = new ImpexTokenType("COMMA");
  IElementType CRLF = new ImpexTokenType("CRLF");
  IElementType DEFAULT_KEY_VALUE_DELIMITER = new ImpexTokenType("DEFAULT_KEY_VALUE_DELIMITER");
  IElementType DEFAULT_PATH_DELIMITER = new ImpexTokenType("DEFAULT_PATH_DELIMITER");
  IElementType DIGIT = new ImpexTokenType("DIGIT");
  IElementType DOCUMENT_ID = new ImpexTokenType("DOCUMENT_ID");
  IElementType DOT = new ImpexTokenType("DOT");
  IElementType DOUBLE_STRING = new ImpexTokenType("DOUBLE_STRING");
  IElementType END_USERRIGHTS = new ImpexTokenType("END_USERRIGHTS");
  IElementType FIELD_LIST_ITEM_SEPARATOR = new ImpexTokenType("FIELD_LIST_ITEM_SEPARATOR");
  IElementType FIELD_VALUE = new ImpexTokenType("FIELD_VALUE");
  IElementType FIELD_VALUE_IGNORE = new ImpexTokenType("FIELD_VALUE_IGNORE");
  IElementType FIELD_VALUE_NULL = new ImpexTokenType("FIELD_VALUE_NULL");
  IElementType FIELD_VALUE_SEPARATOR = new ImpexTokenType("FIELD_VALUE_SEPARATOR");
  IElementType FIELD_VALUE_URL = new ImpexTokenType("FIELD_VALUE_URL");
  IElementType FUNCTION = new ImpexTokenType("FUNCTION");
  IElementType HEADER_MODE_INSERT = new ImpexTokenType("HEADER_MODE_INSERT");
  IElementType HEADER_MODE_INSERT_UPDATE = new ImpexTokenType("HEADER_MODE_INSERT_UPDATE");
  IElementType HEADER_MODE_REMOVE = new ImpexTokenType("HEADER_MODE_REMOVE");
  IElementType HEADER_MODE_UPDATE = new ImpexTokenType("HEADER_MODE_UPDATE");
  IElementType HEADER_PARAMETER_NAME = new ImpexTokenType("HEADER_PARAMETER_NAME");
  IElementType HEADER_SPECIAL_PARAMETER_NAME = new ImpexTokenType("HEADER_SPECIAL_PARAMETER_NAME");
  IElementType HEADER_TYPE = new ImpexTokenType("HEADER_TYPE");
  IElementType LEFT_BRACE = new ImpexTokenType("LEFT_BRACE");
  IElementType LEFT_ROUND_BRACKET = new ImpexTokenType("LEFT_ROUND_BRACKET");
  IElementType LEFT_SQUARE_BRACKET = new ImpexTokenType("LEFT_SQUARE_BRACKET");
  IElementType LINE_COMMENT = new ImpexTokenType("LINE_COMMENT");
  IElementType MACRO_NAME_DECLARATION = new ImpexTokenType("MACRO_NAME_DECLARATION");
  IElementType MACRO_USAGE = new ImpexTokenType("MACRO_USAGE");
  IElementType MACRO_VALUE = new ImpexTokenType("MACRO_VALUE");
  IElementType MEMBEROFGROUPS = new ImpexTokenType("MEMBEROFGROUPS");
  IElementType PARAMETERS_SEPARATOR = new ImpexTokenType("PARAMETERS_SEPARATOR");
  IElementType PASSWORD = new ImpexTokenType("PASSWORD");
  IElementType PERMISSION = new ImpexTokenType("PERMISSION");
  IElementType PERMISSION_ALLOWED = new ImpexTokenType("PERMISSION_ALLOWED");
  IElementType PERMISSION_DENIED = new ImpexTokenType("PERMISSION_DENIED");
  IElementType QUESTION_MARK = new ImpexTokenType("QUESTION_MARK");
  IElementType RIGHT_BRACE = new ImpexTokenType("RIGHT_BRACE");
  IElementType RIGHT_ROUND_BRACKET = new ImpexTokenType("RIGHT_ROUND_BRACKET");
  IElementType RIGHT_SQUARE_BRACKET = new ImpexTokenType("RIGHT_SQUARE_BRACKET");
  IElementType SINGLE_STRING = new ImpexTokenType("SINGLE_STRING");
  IElementType START_USERRIGHTS = new ImpexTokenType("START_USERRIGHTS");
  IElementType TARGET = new ImpexTokenType("TARGET");
  IElementType TYPE = new ImpexTokenType("TYPE");
  IElementType UID = new ImpexTokenType("UID");
  IElementType VALUE_SUBTYPE = new ImpexTokenType("VALUE_SUBTYPE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ANY_ATTRIBUTE_NAME) {
        return new ImpexAnyAttributeNameImpl(node);
      }
      else if (type == ANY_ATTRIBUTE_VALUE) {
        return new ImpexAnyAttributeValueImpl(node);
      }
      else if (type == ANY_HEADER_MODE) {
        return new ImpexAnyHeaderModeImpl(node);
      }
      else if (type == ANY_HEADER_PARAMETER_NAME) {
        return new ImpexAnyHeaderParameterNameImpl(node);
      }
      else if (type == ATTRIBUTE) {
        return new ImpexAttributeImpl(node);
      }
      else if (type == BEAN_SHELL) {
        return new ImpexBeanShellImpl(node);
      }
      else if (type == COMMENT) {
        return new ImpexCommentImpl(node);
      }
      else if (type == FULL_HEADER_PARAMETER) {
        return new ImpexFullHeaderParameterImpl(node);
      }
      else if (type == FULL_HEADER_TYPE) {
        return new ImpexFullHeaderTypeImpl(node);
      }
      else if (type == HEADER_LINE) {
        return new ImpexHeaderLineImpl(node);
      }
      else if (type == HEADER_TYPE_NAME) {
        return new ImpexHeaderTypeNameImpl(node);
      }
      else if (type == MACRO_DECLARATION) {
        return new ImpexMacroDeclarationImpl(node);
      }
      else if (type == MACRO_NAME_DEC) {
        return new ImpexMacroNameDecImpl(node);
      }
      else if (type == MACRO_USAGE_DEC) {
        return new ImpexMacroUsageDecImpl(node);
      }
      else if (type == MACRO_VALUE_DEC) {
        return new ImpexMacroValueDecImpl(node);
      }
      else if (type == MODIFIERS) {
        return new ImpexModifiersImpl(node);
      }
      else if (type == PARAMETER) {
        return new ImpexParameterImpl(node);
      }
      else if (type == PARAMETERS) {
        return new ImpexParametersImpl(node);
      }
      else if (type == ROOT_MACRO_USAGE) {
        return new ImpexRootMacroUsageImpl(node);
      }
      else if (type == STRING) {
        return new ImpexStringImpl(node);
      }
      else if (type == SUB_PARAMETERS) {
        return new ImpexSubParametersImpl(node);
      }
      else if (type == SUB_TYPE_NAME) {
        return new ImpexSubTypeNameImpl(node);
      }
      else if (type == USER_RIGHTS) {
        return new ImpexUserRightsImpl(node);
      }
      else if (type == USER_RIGHTS_ATTRIBUTE_VALUE) {
        return new ImpexUserRightsAttributeValueImpl(node);
      }
      else if (type == USER_RIGHTS_END) {
        return new ImpexUserRightsEndImpl(node);
      }
      else if (type == USER_RIGHTS_FIRST_VALUE_GROUP) {
        return new ImpexUserRightsFirstValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_LINE) {
        return new ImpexUserRightsHeaderLineImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER) {
        return new ImpexUserRightsHeaderParameterImpl(node);
      }
      else if (type == USER_RIGHTS_MULTI_VALUE) {
        return new ImpexUserRightsMultiValueImpl(node);
      }
      else if (type == USER_RIGHTS_PERMISSION_VALUE) {
        return new ImpexUserRightsPermissionValueImpl(node);
      }
      else if (type == USER_RIGHTS_SINGLE_VALUE) {
        return new ImpexUserRightsSingleValueImpl(node);
      }
      else if (type == USER_RIGHTS_START) {
        return new ImpexUserRightsStartImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP) {
        return new ImpexUserRightsValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE) {
        return new ImpexUserRightsValueLineImpl(node);
      }
      else if (type == VALUE) {
        return new ImpexValueImpl(node);
      }
      else if (type == VALUE_GROUP) {
        return new ImpexValueGroupImpl(node);
      }
      else if (type == VALUE_LINE) {
        return new ImpexValueLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
