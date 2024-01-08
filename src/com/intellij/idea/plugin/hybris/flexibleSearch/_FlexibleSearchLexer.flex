/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.flexibleSearch;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%{
    public _FlexibleSearchLexer() {
        this((java.io.Reader)null);
    }
%}

%public
%class _FlexibleSearchLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%caseless

WHITE_SPACE=\s+

NUMERIC_LITERAL=(([0-9]+(\.[0-9]*)?|\.[0-9]+)(E(\+|-)?[0-9]+)?)|(0x[0-9a-f]+)
BOOLEAN_LITERAL=((true)|(false))
SINGLE_QUOTE_STRING_LITERAL=X?'(''|[^'])*'
DOUBLE_QUOTE_STRING_LITERAL=X?\"(\"\"|[^\"])*\"
BACKTICK_LITERAL=`(``|[^`])*`
IDENTIFIER=([:letter:]|_)([:letter:]|[:digit:]|_)*
NAMED_PARAMETER=[?][:jletterdigit:]+
COMMENT="/*" ( ([^"*"]|[\r\n])* ("*"+ [^"*""/"] )? )* ("*" | "*"+"/")?
LINE_COMMENT=--[^\r\n]*

%state LOCALIZED_STATE

%%
<YYINITIAL> {
  {WHITE_SPACE}                      { return WHITE_SPACE; }

  "?"                                { return QUESTION_MARK; }
  "!"                                { return EXCLAMATION_MARK; }
  "["                                { return LBRACKET; }
  "]"                                { yybegin(LOCALIZED_STATE); return RBRACKET; }
  "{"                                { return LBRACE; }
  "}"                                { return RBRACE; }
  "{{"                               { return LDBRACE; }
  "}}"                               { return RDBRACE; }
  "}}}"                              { yypushback(2); return RBRACE; }
  "("                                { return LPAREN; }
  ")"                                { return RPAREN; }
  "&"                                { return AMP; }
  "|"                                { return BAR; }
  ","                                { return COMMA; }
  "||"                               { return CONCAT; }
  "/"                                { return DIV; }
  "."                                { return DOT; }
  "="                                { return EQ; }
  "=="                               { return EQEQ; }
  ">"                                { return GT; }
  ">="                               { return GTE; }
  "<"                                { return LT; }
  "<="                               { return LTE; }
  "-"                                { return MINUS; }
  "%"                                { return MOD; }
  "!="                               { return NOT_EQ; }
  "+"                                { return PLUS; }
  ";"                                { return SEMICOLON; }
  ":"                                { return COLON; }
  "<<"                               { return SHL; }
  ">>"                               { return SHR; }
  "*"                                { return STAR; }
  "~"                                { return TILDE; }
  "<>"                               { return UNEQ; }
  //":o"                               { return OUTER_JOIN; }
  "FULL"                             { return FULL; }
  "INTERVAL"                         { return INTERVAL; }
  "AND"                              { return AND; }
  "OR"                               { return OR; }
  "IS"                               { return IS; }
  "NOT"                              { return NOT; }
  "IN"                               { return IN; }
  "BETWEEN"                          { return BETWEEN; }
  "CAST"                             { return CAST; }
  "AS"                               { return AS; }
  "CASE"                             { return CASE; }
  "WHEN"                             { return WHEN; }
  "THEN"                             { return THEN; }
  "ELSE"                             { return ELSE; }
  "END"                              { return END; }
  "LIKE"                             { return LIKE; }
  "REGEXP"                           { return REGEXP; }
  "MATCH"                            { return MATCH; }
  "ESCAPE"                           { return ESCAPE; }
  "NULL"                             { return NULL; }
  "EXISTS"                           { return EXISTS; }
  "DISTINCT"                         { return DISTINCT; }
  "SELECT"                           { return SELECT; }
  "NUMBERED_PARAMETER"               { return NUMBERED_PARAMETER; }
  "LIMIT"                            { return LIMIT; }
  "OFFSET"                           { return OFFSET; }
  "ORDER"                            { return ORDER; }
  "BY"                               { return BY; }
  "ALL"                              { return ALL; }
  "GROUP"                            { return GROUP; }
  "HAVING"                           { return HAVING; }
  "WHERE"                            { return WHERE; }
  "FROM"                             { return FROM; }
  "LEFT"                             { return LEFT; }
  "OUTER"                            { return OUTER; }
  "INNER"                            { return INNER; }
  "RIGHT"                            { return RIGHT; }
  "JOIN"                             { return JOIN; }
  "ON"                               { return ON; }
  "USING"                            { return USING; }
  "ASC"                              { return ASC; }
  "DESC"                             { return DESC; }
  "UNION"                            { return UNION; }

  {BOOLEAN_LITERAL}                  { return BOOLEAN_LITERAL; }
  {NUMERIC_LITERAL}                  { return NUMERIC_LITERAL; }
  {SINGLE_QUOTE_STRING_LITERAL}      { return SINGLE_QUOTE_STRING_LITERAL; }
  {DOUBLE_QUOTE_STRING_LITERAL}      { return DOUBLE_QUOTE_STRING_LITERAL; }
  {BACKTICK_LITERAL}                 { return BACKTICK_LITERAL; }
  {IDENTIFIER}                       { return IDENTIFIER; }
  {LINE_COMMENT}                     { return LINE_COMMENT; }
  {NAMED_PARAMETER}                  { return NAMED_PARAMETER; }
  {COMMENT}                          { return COMMENT; }

}
<LOCALIZED_STATE> {
  {WHITE_SPACE}                      { return WHITE_SPACE; }

  ":o"                               { yybegin(YYINITIAL); return OUTER_JOIN; }
  "}"                                { yybegin(YYINITIAL); return RBRACE; }

  {IDENTIFIER}                       { return IDENTIFIER; }

}

[^] { return BAD_CHARACTER; }
