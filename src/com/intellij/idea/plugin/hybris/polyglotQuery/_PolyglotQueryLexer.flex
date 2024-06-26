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
