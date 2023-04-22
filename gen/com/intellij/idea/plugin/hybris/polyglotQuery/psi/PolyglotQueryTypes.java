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

  IElementType PROPERTY = new PolyglotQueryElementType("PROPERTY");

  IElementType AMP = new PolyglotQueryTokenType("&");
  IElementType COMMENT = new PolyglotQueryTokenType("COMMENT");
  IElementType CRLF = new PolyglotQueryTokenType("CRLF");
  IElementType EQ = new PolyglotQueryTokenType("=");
  IElementType GT = new PolyglotQueryTokenType(">");
  IElementType GTE = new PolyglotQueryTokenType(">=");
  IElementType IDENTIFIER = new PolyglotQueryTokenType("IDENTIFIER");
  IElementType KEY = new PolyglotQueryTokenType("KEY");
  IElementType LBRACE = new PolyglotQueryTokenType("{");
  IElementType LBRACKET = new PolyglotQueryTokenType("[");
  IElementType LT = new PolyglotQueryTokenType("<");
  IElementType LTE = new PolyglotQueryTokenType("<=");
  IElementType QUESTION_MARK = new PolyglotQueryTokenType("?");
  IElementType RBRACE = new PolyglotQueryTokenType("}");
  IElementType RBRACKET = new PolyglotQueryTokenType("]");
  IElementType SEPARATOR = new PolyglotQueryTokenType("SEPARATOR");
  IElementType UNEQ = new PolyglotQueryTokenType("<>");
  IElementType VALUE = new PolyglotQueryTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == PROPERTY) {
        return new PolyglotQueryPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
