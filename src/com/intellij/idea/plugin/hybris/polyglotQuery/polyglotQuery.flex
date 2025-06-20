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

 package com.intellij.idea.plugin.hybris.polyglotQuery;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes.*;

%%

%{
  public _PolyglotQueryLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _PolyglotQueryLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%caseless

WHITE_SPACE=\s+

IDENTIFIER=([:letter:])([:letter:]|[:digit:]|_)*
LINE_COMMENT=--[^r\n]*
COMMENT="/*" ( ([^"*"]|[\r\n])* ("*"+ [^"*""/"] )? )* ("*" | "*"+"/")?

%state GET_STATE
%state ATTRIBUTE_STATE

%%
<YYINITIAL> {
  {WHITE_SPACE}       { return WHITE_SPACE; }

  "?"                 { return QUESTION_MARK; }
  "."                 { return DOT; }
  ","                 { return COMMA; }
  "["                 { return LBRACKET; }
  "]"                 { return RBRACKET; }
  "{"                 { yybegin(ATTRIBUTE_STATE); return LBRACE; }
  "}"                 { return RBRACE; }
  "&"                 { return AMP; }
  "="                 { return EQ; }
  ">"                 { return GT; }
  ">="                { return GTE; }
  "<"                 { return LT; }
  "<="                { return LTE; }
  "<>"                { return UNEQ; }
  "("                 { return LPAREN; }
  ")"                 { return RPAREN; }
  "GET"               { yybegin(GET_STATE); return GET; }
  "ASC"               { return ASC; }
  "DESC"              { return DESC; }
  "ORDER"             { return ORDER; }
  "BY"                { return BY; }
  "WHERE"             { return WHERE; }
  "OR"                { return OR; }
  "AND"               { return AND; }
  "IS"                { return IS; }
  "NOT"               { return NOT; }
  "NULL"              { return NULL; }

  {IDENTIFIER}        { return IDENTIFIER; }
  {LINE_COMMENT}      { return LINE_COMMENT; }
  {COMMENT}           { return COMMENT; }

}

<GET_STATE> {
  {WHITE_SPACE}       { return WHITE_SPACE; }

  "{"                 { return LBRACE; }
  "}"                 { yybegin(YYINITIAL); return RBRACE; }

  {IDENTIFIER}        { return IDENTIFIER; }
  {LINE_COMMENT}      { return LINE_COMMENT; }
  {COMMENT}           { return COMMENT; }
}

<ATTRIBUTE_STATE> {
  {WHITE_SPACE}       { return WHITE_SPACE; }

  "}"                 { yybegin(YYINITIAL); return RBRACE; }
  "["                 { return LBRACKET; }
  "]"                 { return RBRACKET; }

  {IDENTIFIER}        { return IDENTIFIER; }
  {LINE_COMMENT}      { return LINE_COMMENT; }
  {COMMENT}           { return COMMENT; }
}

[^] { return BAD_CHARACTER; }
