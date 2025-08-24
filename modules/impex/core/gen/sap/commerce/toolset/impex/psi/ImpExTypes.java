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
package sap.commerce.toolset.impex.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import sap.commerce.toolset.impex.psi.impl.*;

public interface ImpExTypes {

  IElementType ANY_ATTRIBUTE_NAME = new ImpExElementType("ANY_ATTRIBUTE_NAME");
  IElementType ANY_ATTRIBUTE_VALUE = new ImpExElementType("ANY_ATTRIBUTE_VALUE");
  IElementType ANY_HEADER_MODE = new ImpExElementType("ANY_HEADER_MODE");
  IElementType ANY_HEADER_PARAMETER_NAME = new ImpExElementType("ANY_HEADER_PARAMETER_NAME");
  IElementType ATTRIBUTE = new ImpExElementType("ATTRIBUTE");
  IElementType BEANSHELL_SCRIPT_BODY = new ImpExElementType("BEANSHELL_SCRIPT_BODY");
  IElementType COMMENT = new ImpExElementType("COMMENT");
  IElementType DOCUMENT_ID_DEC = new ImpExElementType("DOCUMENT_ID_DEC");
  IElementType DOCUMENT_ID_USAGE = new ImpExElementType("DOCUMENT_ID_USAGE");
  IElementType FULL_HEADER_PARAMETER = new ImpExElementType("FULL_HEADER_PARAMETER");
  IElementType FULL_HEADER_TYPE = new ImpExElementType("FULL_HEADER_TYPE");
  IElementType GROOVY_SCRIPT_BODY = new ImpExElementType("GROOVY_SCRIPT_BODY");
  IElementType HEADER_LINE = new ImpExElementType("HEADER_LINE");
  IElementType HEADER_TYPE_NAME = new ImpExElementType("HEADER_TYPE_NAME");
  IElementType JAVASCRIPT_SCRIPT_BODY = new ImpExElementType("JAVASCRIPT_SCRIPT_BODY");
  IElementType MACRO_DECLARATION = new ImpExElementType("MACRO_DECLARATION");
  IElementType MACRO_NAME_DEC = new ImpExElementType("MACRO_NAME_DEC");
  IElementType MACRO_USAGE_DEC = new ImpExElementType("MACRO_USAGE_DEC");
  IElementType MACRO_VALUE_DEC = new ImpExElementType("MACRO_VALUE_DEC");
  IElementType MODIFIERS = new ImpExElementType("MODIFIERS");
  IElementType PARAMETER = new ImpExElementType("PARAMETER");
  IElementType PARAMETERS = new ImpExElementType("PARAMETERS");
  IElementType ROOT_MACRO_USAGE = new ImpExElementType("ROOT_MACRO_USAGE");
  IElementType SCRIPT = new ImpExElementType("SCRIPT");
  IElementType STRING = new ImpExElementType("STRING");
  IElementType SUB_PARAMETERS = new ImpExElementType("SUB_PARAMETERS");
  IElementType SUB_TYPE_NAME = new ImpExElementType("SUB_TYPE_NAME");
  IElementType USER_RIGHTS = new ImpExElementType("USER_RIGHTS");
  IElementType USER_RIGHTS_ATTRIBUTE_VALUE = new ImpExElementType("USER_RIGHTS_ATTRIBUTE_VALUE");
  IElementType USER_RIGHTS_END = new ImpExElementType("USER_RIGHTS_END");
  IElementType USER_RIGHTS_FIRST_VALUE_GROUP = new ImpExElementType("USER_RIGHTS_FIRST_VALUE_GROUP");
  IElementType USER_RIGHTS_HEADER_LINE = new ImpExElementType("USER_RIGHTS_HEADER_LINE");
  IElementType USER_RIGHTS_HEADER_PARAMETER = new ImpExElementType("USER_RIGHTS_HEADER_PARAMETER");
  IElementType USER_RIGHTS_MULTI_VALUE = new ImpExElementType("USER_RIGHTS_MULTI_VALUE");
  IElementType USER_RIGHTS_PERMISSION_VALUE = new ImpExElementType("USER_RIGHTS_PERMISSION_VALUE");
  IElementType USER_RIGHTS_SINGLE_VALUE = new ImpExElementType("USER_RIGHTS_SINGLE_VALUE");
  IElementType USER_RIGHTS_START = new ImpExElementType("USER_RIGHTS_START");
  IElementType USER_RIGHTS_VALUE_GROUP = new ImpExElementType("USER_RIGHTS_VALUE_GROUP");
  IElementType USER_RIGHTS_VALUE_LINE = new ImpExElementType("USER_RIGHTS_VALUE_LINE");
  IElementType VALUE = new ImpExElementType("VALUE");
  IElementType VALUE_GROUP = new ImpExElementType("VALUE_GROUP");
  IElementType VALUE_LINE = new ImpExElementType("VALUE_LINE");

  IElementType ALTERNATIVE_MAP_DELIMITER = new ImpExTokenType("ALTERNATIVE_MAP_DELIMITER");
  IElementType ALTERNATIVE_PATTERN = new ImpExTokenType("ALTERNATIVE_PATTERN");
  IElementType ASSIGN_VALUE = new ImpExTokenType("ASSIGN_VALUE");
  IElementType ATTRIBUTE_NAME = new ImpExTokenType("ATTRIBUTE_NAME");
  IElementType ATTRIBUTE_SEPARATOR = new ImpExTokenType("ATTRIBUTE_SEPARATOR");
  IElementType ATTRIBUTE_VALUE = new ImpExTokenType("ATTRIBUTE_VALUE");
  IElementType BEAN_SHELL_MARKER = new ImpExTokenType("BEAN_SHELL_MARKER");
  IElementType BOOLEAN = new ImpExTokenType("BOOLEAN");
  IElementType COLLECTION_APPEND_PREFIX = new ImpExTokenType("COLLECTION_APPEND_PREFIX");
  IElementType COLLECTION_MERGE_PREFIX = new ImpExTokenType("COLLECTION_MERGE_PREFIX");
  IElementType COLLECTION_REMOVE_PREFIX = new ImpExTokenType("COLLECTION_REMOVE_PREFIX");
  IElementType COMMA = new ImpExTokenType("COMMA");
  IElementType CRLF = new ImpExTokenType("CRLF");
  IElementType DEFAULT_KEY_VALUE_DELIMITER = new ImpExTokenType("DEFAULT_KEY_VALUE_DELIMITER");
  IElementType DEFAULT_PATH_DELIMITER = new ImpExTokenType("DEFAULT_PATH_DELIMITER");
  IElementType DIGIT = new ImpExTokenType("DIGIT");
  IElementType DOCUMENT_ID = new ImpExTokenType("DOCUMENT_ID");
  IElementType DOT = new ImpExTokenType("DOT");
  IElementType DOUBLE_STRING = new ImpExTokenType("DOUBLE_STRING");
  IElementType END_USERRIGHTS = new ImpExTokenType("END_USERRIGHTS");
  IElementType FIELD_LIST_ITEM_SEPARATOR = new ImpExTokenType("FIELD_LIST_ITEM_SEPARATOR");
  IElementType FIELD_VALUE = new ImpExTokenType("FIELD_VALUE");
  IElementType FIELD_VALUE_EXPLODED_JAR_PREFIX = new ImpExTokenType("FIELD_VALUE_EXPLODED_JAR_PREFIX");
  IElementType FIELD_VALUE_FILE_PREFIX = new ImpExTokenType("FIELD_VALUE_FILE_PREFIX");
  IElementType FIELD_VALUE_HTTP_PREFIX = new ImpExTokenType("FIELD_VALUE_HTTP_PREFIX");
  IElementType FIELD_VALUE_IGNORE = new ImpExTokenType("FIELD_VALUE_IGNORE");
  IElementType FIELD_VALUE_JAR_PREFIX = new ImpExTokenType("FIELD_VALUE_JAR_PREFIX");
  IElementType FIELD_VALUE_NULL = new ImpExTokenType("FIELD_VALUE_NULL");
  IElementType FIELD_VALUE_PASSWORD_ENCODING_PREFIX = new ImpExTokenType("FIELD_VALUE_PASSWORD_ENCODING_PREFIX");
  IElementType FIELD_VALUE_SCRIPT_PREFIX = new ImpExTokenType("FIELD_VALUE_SCRIPT_PREFIX");
  IElementType FIELD_VALUE_SEPARATOR = new ImpExTokenType("FIELD_VALUE_SEPARATOR");
  IElementType FIELD_VALUE_ZIP_PREFIX = new ImpExTokenType("FIELD_VALUE_ZIP_PREFIX");
  IElementType FUNCTION = new ImpExTokenType("FUNCTION");
  IElementType GROOVY_MARKER = new ImpExTokenType("GROOVY_MARKER");
  IElementType HEADER_MODE_INSERT = new ImpExTokenType("HEADER_MODE_INSERT");
  IElementType HEADER_MODE_INSERT_UPDATE = new ImpExTokenType("HEADER_MODE_INSERT_UPDATE");
  IElementType HEADER_MODE_REMOVE = new ImpExTokenType("HEADER_MODE_REMOVE");
  IElementType HEADER_MODE_UPDATE = new ImpExTokenType("HEADER_MODE_UPDATE");
  IElementType HEADER_PARAMETER_NAME = new ImpExTokenType("HEADER_PARAMETER_NAME");
  IElementType HEADER_SPECIAL_PARAMETER_NAME = new ImpExTokenType("HEADER_SPECIAL_PARAMETER_NAME");
  IElementType HEADER_TYPE = new ImpExTokenType("HEADER_TYPE");
  IElementType JAVASCRIPT_MARKER = new ImpExTokenType("JAVASCRIPT_MARKER");
  IElementType LEFT_ROUND_BRACKET = new ImpExTokenType("LEFT_ROUND_BRACKET");
  IElementType LEFT_SQUARE_BRACKET = new ImpExTokenType("LEFT_SQUARE_BRACKET");
  IElementType LINE_COMMENT = new ImpExTokenType("LINE_COMMENT");
  IElementType MACRO_NAME_DECLARATION = new ImpExTokenType("MACRO_NAME_DECLARATION");
  IElementType MACRO_USAGE = new ImpExTokenType("MACRO_USAGE");
  IElementType MACRO_VALUE = new ImpExTokenType("MACRO_VALUE");
  IElementType MEMBEROFGROUPS = new ImpExTokenType("MEMBEROFGROUPS");
  IElementType MULTILINE_SEPARATOR = new ImpExTokenType("MULTILINE_SEPARATOR");
  IElementType PARAMETERS_SEPARATOR = new ImpExTokenType("PARAMETERS_SEPARATOR");
  IElementType PASSWORD = new ImpExTokenType("PASSWORD");
  IElementType PERMISSION = new ImpExTokenType("PERMISSION");
  IElementType PERMISSION_ALLOWED = new ImpExTokenType("PERMISSION_ALLOWED");
  IElementType PERMISSION_DENIED = new ImpExTokenType("PERMISSION_DENIED");
  IElementType RIGHT_ROUND_BRACKET = new ImpExTokenType("RIGHT_ROUND_BRACKET");
  IElementType RIGHT_SQUARE_BRACKET = new ImpExTokenType("RIGHT_SQUARE_BRACKET");
  IElementType SCRIPT_ACTION = new ImpExTokenType("SCRIPT_ACTION");
  IElementType SCRIPT_BODY_VALUE = new ImpExTokenType("SCRIPT_BODY_VALUE");
  IElementType SINGLE_STRING = new ImpExTokenType("SINGLE_STRING");
  IElementType START_USERRIGHTS = new ImpExTokenType("START_USERRIGHTS");
  IElementType TARGET = new ImpExTokenType("TARGET");
  IElementType TYPE = new ImpExTokenType("TYPE");
  IElementType UID = new ImpExTokenType("UID");
  IElementType VALUE_SUBTYPE = new ImpExTokenType("VALUE_SUBTYPE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ANY_ATTRIBUTE_NAME) {
        return new ImpExAnyAttributeNameImpl(node);
      }
      else if (type == ANY_ATTRIBUTE_VALUE) {
        return new ImpExAnyAttributeValueImpl(node);
      }
      else if (type == ANY_HEADER_MODE) {
        return new ImpExAnyHeaderModeImpl(node);
      }
      else if (type == ANY_HEADER_PARAMETER_NAME) {
        return new ImpExAnyHeaderParameterNameImpl(node);
      }
      else if (type == ATTRIBUTE) {
        return new ImpExAttributeImpl(node);
      }
      else if (type == BEANSHELL_SCRIPT_BODY) {
        return new ImpExBeanshellScriptBodyImpl(node);
      }
      else if (type == COMMENT) {
        return new ImpExCommentImpl(node);
      }
      else if (type == DOCUMENT_ID_DEC) {
        return new ImpExDocumentIdDecImpl(node);
      }
      else if (type == DOCUMENT_ID_USAGE) {
        return new ImpExDocumentIdUsageImpl(node);
      }
      else if (type == FULL_HEADER_PARAMETER) {
        return new ImpExFullHeaderParameterImpl(node);
      }
      else if (type == FULL_HEADER_TYPE) {
        return new ImpExFullHeaderTypeImpl(node);
      }
      else if (type == GROOVY_SCRIPT_BODY) {
        return new ImpExGroovyScriptBodyImpl(node);
      }
      else if (type == HEADER_LINE) {
        return new ImpExHeaderLineImpl(node);
      }
      else if (type == HEADER_TYPE_NAME) {
        return new ImpExHeaderTypeNameImpl(node);
      }
      else if (type == JAVASCRIPT_SCRIPT_BODY) {
        return new ImpExJavascriptScriptBodyImpl(node);
      }
      else if (type == MACRO_DECLARATION) {
        return new ImpExMacroDeclarationImpl(node);
      }
      else if (type == MACRO_NAME_DEC) {
        return new ImpExMacroNameDecImpl(node);
      }
      else if (type == MACRO_USAGE_DEC) {
        return new ImpExMacroUsageDecImpl(node);
      }
      else if (type == MACRO_VALUE_DEC) {
        return new ImpExMacroValueDecImpl(node);
      }
      else if (type == MODIFIERS) {
        return new ImpExModifiersImpl(node);
      }
      else if (type == PARAMETER) {
        return new ImpExParameterImpl(node);
      }
      else if (type == PARAMETERS) {
        return new ImpExParametersImpl(node);
      }
      else if (type == ROOT_MACRO_USAGE) {
        return new ImpExRootMacroUsageImpl(node);
      }
      else if (type == SCRIPT) {
        return new ImpExScriptImpl(node);
      }
      else if (type == STRING) {
        return new ImpExStringImpl(node);
      }
      else if (type == SUB_PARAMETERS) {
        return new ImpExSubParametersImpl(node);
      }
      else if (type == SUB_TYPE_NAME) {
        return new ImpExSubTypeNameImpl(node);
      }
      else if (type == USER_RIGHTS) {
        return new ImpExUserRightsImpl(node);
      }
      else if (type == USER_RIGHTS_ATTRIBUTE_VALUE) {
        return new ImpExUserRightsAttributeValueImpl(node);
      }
      else if (type == USER_RIGHTS_END) {
        return new ImpExUserRightsEndImpl(node);
      }
      else if (type == USER_RIGHTS_FIRST_VALUE_GROUP) {
        return new ImpExUserRightsFirstValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_LINE) {
        return new ImpExUserRightsHeaderLineImpl(node);
      }
      else if (type == USER_RIGHTS_HEADER_PARAMETER) {
        return new ImpExUserRightsHeaderParameterImpl(node);
      }
      else if (type == USER_RIGHTS_MULTI_VALUE) {
        return new ImpExUserRightsMultiValueImpl(node);
      }
      else if (type == USER_RIGHTS_PERMISSION_VALUE) {
        return new ImpExUserRightsPermissionValueImpl(node);
      }
      else if (type == USER_RIGHTS_SINGLE_VALUE) {
        return new ImpExUserRightsSingleValueImpl(node);
      }
      else if (type == USER_RIGHTS_START) {
        return new ImpExUserRightsStartImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_GROUP) {
        return new ImpExUserRightsValueGroupImpl(node);
      }
      else if (type == USER_RIGHTS_VALUE_LINE) {
        return new ImpExUserRightsValueLineImpl(node);
      }
      else if (type == VALUE) {
        return new ImpExValueImpl(node);
      }
      else if (type == VALUE_GROUP) {
        return new ImpExValueGroupImpl(node);
      }
      else if (type == VALUE_LINE) {
        return new ImpExValueLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
