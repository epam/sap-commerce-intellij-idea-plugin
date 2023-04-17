/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.psi.TokenType;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;
import static com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchParserDefinition.COMMENT;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*;

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

EOL=\R
WHITE_SPACE=\s+

LEFT_BRACE=(\{)
RIGHT_BRACE=(\})
RIGHT_DOUBLE_BRACE=(}})
LEFT_DOUBLE_BRACE=(\{\{)
EQUALS_OPERATOR=(=)
GREATER_THAN_OR_EQUALS_OPERATOR=(>=)
LESS_THAN_OR_EQUALS_OPERATOR=(<=)
SPACE=[ \t\n\x0B\f\r]+
IDENTIFIER=[:letter:][a-zA-Z_0-9]*
PARAMETER_IDENTIFIER=([:jletter:] [:jletterdigit:]*)
COLUMN_REFERENCE_IDENTIFIER=([:jletter:] [:jletterdigit:]*)
TABLE_NAME_IDENTIFIER=([:jletter:] [:jletterdigit:]*)
STRING=('([^'\\]|\\.)*'|\"([^\"\\]|\\\"|\\'|\\)*\")
NUMBER=[:jdigit:]*
LINE_COMMENT=--[^r\n]*

%%
<YYINITIAL> {
  {WHITE_SPACE}                          { return WHITE_SPACE; }

  "%"                                    { return PERCENT; }
  "'"                                    { return QUOTE; }
  "*"                                    { return ASTERISK; }
  "+"                                    { return PLUS_SIGN; }
  ","                                    { return COMMA; }
  "-"                                    { return MINUS_SIGN; }
  "."                                    { return DOT; }
  ":"                                    { return COLON; }
  ";"                                    { return SEMICOLON; }
  "?"                                    { return QUESTION_MARK; }
  "!"                                    { return EXCLAMATION_MARK; }
  "_"                                    { return UNDERSCORE; }
  "["                                    { return LEFT_BRACKET; }
  "]"                                    { return RIGHT_BRACKET; }
  "("                                    { return LEFT_PAREN; }
  ")"                                    { return RIGHT_PAREN; }
  "<>"                                   { return NOT_EQUALS_OPERATOR; }
  ">"                                    { return GREATER_THAN_OPERATOR; }
  "<"                                    { return LESS_THAN_OPERATOR; }
  "NUMERIC_LITERAL"                      { return NUMERIC_LITERAL; }
  "AND"                                  { return AND; }
  "OR"                                   { return OR; }
  "IS"                                   { return IS; }
  "NOT"                                  { return NOT; }
  "BETWEEN"                              { return BETWEEN; }
  "CAST"                                 { return CAST; }
  "AS"                                   { return AS; }
  "CASE"                                 { return CASE; }
  "WHEN"                                 { return WHEN; }
  "THEN"                                 { return THEN; }
  "ELSE"                                 { return ELSE; }
  "END"                                  { return END; }
  "LIKE"                                 { return LIKE; }
  "GLOB"                                 { return GLOB; }
  "REGEXP"                               { return REGEXP; }
  "MATCH"                                { return MATCH; }
  "ESCAPE"                               { return ESCAPE; }
  "ISNULL"                               { return ISNULL; }
  "NOTNULL"                              { return NOTNULL; }
  "NULL"                                 { return NULL; }
  "IN"                                   { return IN; }
  "EXISTS"                               { return EXISTS; }
  "SELECT"                               { return SELECT; }
  "NUMBERED_PARAMETER"                   { return NUMBERED_PARAMETER; }
  "NAMED_PARAMETER"                      { return NAMED_PARAMETER; }
  "CURRENT_TIME"                         { return CURRENT_TIME; }
  "CURRENT_DATE"                         { return CURRENT_DATE; }
  "CURRENT_TIMESTAMP"                    { return CURRENT_TIMESTAMP; }
  "LIMIT"                                { return LIMIT; }
  "OFFSET"                               { return OFFSET; }
  "ORDER"                                { return ORDER; }
  "BY"                                   { return BY; }
  "DISTINCT"                             { return DISTINCT; }
  "ALL"                                  { return ALL; }
  "GROUP"                                { return GROUP; }
  "HAVING"                               { return HAVING; }
  "WHERE"                                { return WHERE; }
  "FROM"                                 { return FROM; }
  "LEFT"                                 { return LEFT; }
  "OUTER"                                { return OUTER; }
  "INNER"                                { return INNER; }
  "CROSS"                                { return CROSS; }
  "JOIN"                                 { return JOIN; }
  "ON"                                   { return ON; }
  "USING"                                { return USING; }
  "ASC"                                  { return ASC; }
  "DESC"                                 { return DESC; }
  "UNION"                                { return UNION; }
  "BRACKET_LITERAL"                      { return BRACKET_LITERAL; }
  "BACKTICK_LITERAL"                     { return BACKTICK_LITERAL; }
  "SINGLE_QUOTE_STRING_LITERAL"          { return SINGLE_QUOTE_STRING_LITERAL; }
  "DOUBLE_QUOTE_STRING_LITERAL"          { return DOUBLE_QUOTE_STRING_LITERAL; }

  {LEFT_BRACE}                           { return LEFT_BRACE; }
  {RIGHT_BRACE}                          { return RIGHT_BRACE; }
  {RIGHT_DOUBLE_BRACE}                   { return RIGHT_DOUBLE_BRACE; }
  {LEFT_DOUBLE_BRACE}                    { return LEFT_DOUBLE_BRACE; }
  {EQUALS_OPERATOR}                      { return EQUALS_OPERATOR; }
  {GREATER_THAN_OR_EQUALS_OPERATOR}      { return GREATER_THAN_OR_EQUALS_OPERATOR; }
  {LESS_THAN_OR_EQUALS_OPERATOR}         { return LESS_THAN_OR_EQUALS_OPERATOR; }
  {SPACE}                                { return SPACE; }
  {IDENTIFIER}                           { return IDENTIFIER; }
  {PARAMETER_IDENTIFIER}                 { return PARAMETER_IDENTIFIER; }
  {COLUMN_REFERENCE_IDENTIFIER}          { return COLUMN_REFERENCE_IDENTIFIER; }
  {TABLE_NAME_IDENTIFIER}                { return TABLE_NAME_IDENTIFIER; }
  {STRING}                               { return STRING; }
  {NUMBER}                               { return NUMBER; }
  {LINE_COMMENT}                         { return LINE_COMMENT; }

}

[^] { return BAD_CHARACTER; }
