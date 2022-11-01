// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexAnyAttributeNameImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexAnyAttributeValueImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexAnyHeaderModeImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexAnyHeaderParameterNameImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexAttributeImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexBeanShellImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexCommentImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexFullHeaderParameterImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexFullHeaderTypeImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexHeaderLineImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexHeaderTypeNameImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexMacroDeclarationImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexMacroNameDecImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexMacroUsageDecImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexMacroValueDecImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexModifiersImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexParameterImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexParametersImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexRootMacroUsageImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexStringImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexSubParametersImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexValueGroupImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexValueImpl;
import com.intellij.idea.plugin.hybris.impex.psi.impl.ImpexValueLineImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

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
  IElementType COMMA = new ImpexTokenType("COMMA");
  IElementType CRLF = new ImpexTokenType("CRLF");
  IElementType DEFAULT_KEY_VALUE_DELIMITER = new ImpexTokenType("DEFAULT_KEY_VALUE_DELIMITER");
  IElementType DEFAULT_PATH_DELIMITER = new ImpexTokenType("DEFAULT_PATH_DELIMITER");
  IElementType DIGIT = new ImpexTokenType("DIGIT");
  IElementType DOCUMENT_ID = new ImpexTokenType("DOCUMENT_ID");
  IElementType DOT = new ImpexTokenType("DOT");
  IElementType DOUBLE_STRING = new ImpexTokenType("DOUBLE_STRING");
  IElementType FIELD_LIST_ITEM_SEPARATOR = new ImpexTokenType("FIELD_LIST_ITEM_SEPARATOR");
  IElementType FIELD_VALUE = new ImpexTokenType("FIELD_VALUE");
  IElementType FIELD_VALUE_IGNORE = new ImpexTokenType("FIELD_VALUE_IGNORE");
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
  IElementType LEFT_ROUND_BRACKET = new ImpexTokenType("LEFT_ROUND_BRACKET");
  IElementType LEFT_SQUARE_BRACKET = new ImpexTokenType("LEFT_SQUARE_BRACKET");
  IElementType LINE_COMMENT = new ImpexTokenType("LINE_COMMENT");
  IElementType MACRO_NAME_DECLARATION = new ImpexTokenType("MACRO_NAME_DECLARATION");
  IElementType MACRO_USAGE = new ImpexTokenType("MACRO_USAGE");
  IElementType MACRO_VALUE = new ImpexTokenType("MACRO_VALUE");
  IElementType PARAMETERS_SEPARATOR = new ImpexTokenType("PARAMETERS_SEPARATOR");
  IElementType RIGHT_ROUND_BRACKET = new ImpexTokenType("RIGHT_ROUND_BRACKET");
  IElementType RIGHT_SQUARE_BRACKET = new ImpexTokenType("RIGHT_SQUARE_BRACKET");
  IElementType SINGLE_STRING = new ImpexTokenType("SINGLE_STRING");
  IElementType VALUE_SUBTYPE = new ImpexTokenType("VALUE_SUBTYPE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type.equals(ANY_ATTRIBUTE_NAME)) {
        return new ImpexAnyAttributeNameImpl(node);
      }
      else if (type.equals(ANY_ATTRIBUTE_VALUE)) {
        return new ImpexAnyAttributeValueImpl(node);
      }
      else if (type.equals(ANY_HEADER_MODE)) {
        return new ImpexAnyHeaderModeImpl(node);
      }
      else if (type.equals(ANY_HEADER_PARAMETER_NAME)) {
        return new ImpexAnyHeaderParameterNameImpl(node);
      }
      else if (type.equals(ATTRIBUTE)) {
        return new ImpexAttributeImpl(node);
      }
      else if (type.equals(BEAN_SHELL)) {
        return new ImpexBeanShellImpl(node);
      }
      else if (type.equals(COMMENT)) {
        return new ImpexCommentImpl(node);
      }
      else if (type.equals(FULL_HEADER_PARAMETER)) {
        return new ImpexFullHeaderParameterImpl(node);
      }
      else if (type.equals(FULL_HEADER_TYPE)) {
        return new ImpexFullHeaderTypeImpl(node);
      }
      else if (type.equals(HEADER_LINE)) {
        return new ImpexHeaderLineImpl(node);
      }
      else if (type.equals(HEADER_TYPE_NAME)) {
        return new ImpexHeaderTypeNameImpl(node);
      }
      else if (type.equals(MACRO_DECLARATION)) {
        return new ImpexMacroDeclarationImpl(node);
      }
      else if (type.equals(MACRO_NAME_DEC)) {
        return new ImpexMacroNameDecImpl(node);
      }
      else if (type.equals(MACRO_USAGE_DEC)) {
        return new ImpexMacroUsageDecImpl(node);
      }
      else if (type.equals(MACRO_VALUE_DEC)) {
        return new ImpexMacroValueDecImpl(node);
      }
      else if (type.equals(MODIFIERS)) {
        return new ImpexModifiersImpl(node);
      }
      else if (type.equals(PARAMETER)) {
        return new ImpexParameterImpl(node);
      }
      else if (type.equals(PARAMETERS)) {
        return new ImpexParametersImpl(node);
      }
      else if (type.equals(ROOT_MACRO_USAGE)) {
        return new ImpexRootMacroUsageImpl(node);
      }
      else if (type.equals(STRING)) {
        return new ImpexStringImpl(node);
      }
      else if (type.equals(SUB_PARAMETERS)) {
        return new ImpexSubParametersImpl(node);
      }
      else if (type.equals(VALUE)) {
        return new ImpexValueImpl(node);
      }
      else if (type.equals(VALUE_GROUP)) {
        return new ImpexValueGroupImpl(node);
      }
      else if (type.equals(VALUE_LINE)) {
        return new ImpexValueLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
