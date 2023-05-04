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
package com.intellij.idea.plugin.hybris.polyglotQuery.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryElementType;
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryTokenType;
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.impl.*;

public interface PolyglotQueryTypes {

  IElementType ATTRIBUTE_KEY = new PolyglotQueryElementType("ATTRIBUTE_KEY");
  IElementType BIND_PARAMETER = new PolyglotQueryElementType("BIND_PARAMETER");
  IElementType CMP_OPERATOR = new PolyglotQueryElementType("CMP_OPERATOR");
  IElementType EXPR_AND = new PolyglotQueryElementType("EXPR_AND");
  IElementType EXPR_ATOM = new PolyglotQueryElementType("EXPR_ATOM");
  IElementType EXPR_OR = new PolyglotQueryElementType("EXPR_OR");
  IElementType LOCALIZED_NAME = new PolyglotQueryElementType("LOCALIZED_NAME");
  IElementType NULL_OPERATOR = new PolyglotQueryElementType("NULL_OPERATOR");
  IElementType ORDER_BY = new PolyglotQueryElementType("ORDER_BY");
  IElementType ORDER_KEY = new PolyglotQueryElementType("ORDER_KEY");
  IElementType QUERY = new PolyglotQueryElementType("QUERY");
  IElementType TYPE_KEY = new PolyglotQueryElementType("TYPE_KEY");
  IElementType TYPE_KEY_NAME = new PolyglotQueryElementType("TYPE_KEY_NAME");
  IElementType WHERE_CLAUSE = new PolyglotQueryElementType("WHERE_CLAUSE");

  IElementType AMP = new PolyglotQueryTokenType("&");
  IElementType AND = new PolyglotQueryTokenType("AND");
  IElementType ASC = new PolyglotQueryTokenType("ASC");
  IElementType BY = new PolyglotQueryTokenType("BY");
  IElementType COMMENT = new PolyglotQueryTokenType("COMMENT");
  IElementType DESC = new PolyglotQueryTokenType("DESC");
  IElementType DOT = new PolyglotQueryTokenType(".");
  IElementType EQ = new PolyglotQueryTokenType("=");
  IElementType FROM = new PolyglotQueryTokenType("FROM");
  IElementType GET = new PolyglotQueryTokenType("GET");
  IElementType GT = new PolyglotQueryTokenType(">");
  IElementType GTE = new PolyglotQueryTokenType(">=");
  IElementType IDENTIFIER = new PolyglotQueryTokenType("IDENTIFIER");
  IElementType IS = new PolyglotQueryTokenType("IS");
  IElementType LBRACE = new PolyglotQueryTokenType("{");
  IElementType LBRACKET = new PolyglotQueryTokenType("[");
  IElementType LINE_COMMENT = new PolyglotQueryTokenType("LINE_COMMENT");
  IElementType LPAREN = new PolyglotQueryTokenType("(");
  IElementType LT = new PolyglotQueryTokenType("<");
  IElementType LTE = new PolyglotQueryTokenType("<=");
  IElementType NOT = new PolyglotQueryTokenType("NOT");
  IElementType NULL = new PolyglotQueryTokenType("NULL");
  IElementType OR = new PolyglotQueryTokenType("OR");
  IElementType ORDER = new PolyglotQueryTokenType("ORDER");
  IElementType QUESTION_MARK = new PolyglotQueryTokenType("?");
  IElementType RBRACE = new PolyglotQueryTokenType("}");
  IElementType RBRACKET = new PolyglotQueryTokenType("]");
  IElementType RPAREN = new PolyglotQueryTokenType(")");
  IElementType UNEQ = new PolyglotQueryTokenType("<>");
  IElementType WHERE = new PolyglotQueryTokenType("WHERE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ATTRIBUTE_KEY) {
        return new PolyglotQueryAttributeKeyImpl(node);
      }
      else if (type == BIND_PARAMETER) {
        return new PolyglotQueryBindParameterImpl(node);
      }
      else if (type == CMP_OPERATOR) {
        return new PolyglotQueryCmpOperatorImpl(node);
      }
      else if (type == EXPR_AND) {
        return new PolyglotQueryExprAndImpl(node);
      }
      else if (type == EXPR_ATOM) {
        return new PolyglotQueryExprAtomImpl(node);
      }
      else if (type == EXPR_OR) {
        return new PolyglotQueryExprOrImpl(node);
      }
      else if (type == LOCALIZED_NAME) {
        return new PolyglotQueryLocalizedNameImpl(node);
      }
      else if (type == NULL_OPERATOR) {
        return new PolyglotQueryNullOperatorImpl(node);
      }
      else if (type == ORDER_BY) {
        return new PolyglotQueryOrderByImpl(node);
      }
      else if (type == ORDER_KEY) {
        return new PolyglotQueryOrderKeyImpl(node);
      }
      else if (type == QUERY) {
        return new PolyglotQueryQueryImpl(node);
      }
      else if (type == TYPE_KEY) {
        return new PolyglotQueryTypeKeyImpl(node);
      }
      else if (type == TYPE_KEY_NAME) {
        return new PolyglotQueryTypeKeyNameImpl(node);
      }
      else if (type == WHERE_CLAUSE) {
        return new PolyglotQueryWhereClauseImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
