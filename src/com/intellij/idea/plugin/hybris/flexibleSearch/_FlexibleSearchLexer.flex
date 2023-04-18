package com.intellij.idea.plugin.hybris.flexibleSearch;

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
%caseless

EOL=\R
WHITE_SPACE=\s+

NUMERIC_LITERAL=(([0-9]+(\.[0-9]*)?|\.[0-9]+)(E(\+|-)?[0-9]+)?)|(0x[0-9a-f]+)
SINGLE_QUOTE_STRING_LITERAL=X?'(''|[^'])*'
DOUBLE_QUOTE_STRING_LITERAL=X?\"(\"\"|[^\"])*\"
BRACKET_LITERAL=\[[^\]]*\]
BACKTICK_LITERAL=`(``|[^`])*`
IDENTIFIER=([:letter:]|_)([:letter:]|[:digit:]|_)*
NAMED_PARAMETER=[?][:jletterdigit:]+
COMMENT="/*" ( ([^"*"]|[\r\n])* ("*"+ [^"*""/"] )? )* ("*" | "*"+"/")?
LINE_COMMENT=--[^r\n]*
WHITE_SPACE_FOR_LIVE_PREVIEW=[ \t\n\x0B\f\r]+

%%
<YYINITIAL> {
  {WHITE_SPACE}                       { return WHITE_SPACE; }

  "?"                                 { return QUESTION_MARK; }
  "!"                                 { return EXCLAMATION_MARK; }
  "["                                 { return LBRACKET; }
  "]"                                 { return RBRACKET; }
  "{"                                 { return LBRACE; }
  "}"                                 { return RBRACE; }
  "{{"                                { return LDBRACE; }
  "}}"                                { return RDBRACE; }
  "("                                 { return LPAREN; }
  ")"                                 { return RPAREN; }
  "&"                                 { return AMP; }
  "|"                                 { return BAR; }
  ","                                 { return COMMA; }
  "||"                                { return CONCAT; }
  "/"                                 { return DIV; }
  "."                                 { return DOT; }
  "="                                 { return EQ; }
  "=="                                { return EQEQ; }
  ">"                                 { return GT; }
  ">="                                { return GTE; }
  "<"                                 { return LT; }
  "<="                                { return LTE; }
  "-"                                 { return MINUS; }
  "%"                                 { return MOD; }
  "!="                                { return NOT_EQ; }
  "+"                                 { return PLUS; }
  ";"                                 { return SEMICOLON; }
  ":"                                 { return COLON; }
  "<<"                                { return SHL; }
  ">>"                                { return SHR; }
  "*"                                 { return STAR; }
  "~"                                 { return TILDE; }
  "<>"                                { return UNEQ; }
  "FULL"                              { return FULL; }
  "RIGHT"                             { return RIGHT; }
  "AND"                               { return AND; }
  "OR"                                { return OR; }
  "IS"                                { return IS; }
  "NOT"                               { return NOT; }
  "BETWEEN"                           { return BETWEEN; }
  "CAST"                              { return CAST; }
  "AS"                                { return AS; }
  "CASE"                              { return CASE; }
  "WHEN"                              { return WHEN; }
  "THEN"                              { return THEN; }
  "ELSE"                              { return ELSE; }
  "END"                               { return END; }
  "LIKE"                              { return LIKE; }
  "GLOB"                              { return GLOB; }
  "REGEXP"                            { return REGEXP; }
  "MATCH"                             { return MATCH; }
  "ESCAPE"                            { return ESCAPE; }
  "NULL"                              { return NULL; }
  "IN"                                { return IN; }
  "EXISTS"                            { return EXISTS; }
  "DISTINCT"                          { return DISTINCT; }
  "SELECT"                            { return SELECT; }
  "NUMBERED_PARAMETER"                { return NUMBERED_PARAMETER; }
  "CURRENT_TIME"                      { return CURRENT_TIME; }
  "CURRENT_DATE"                      { return CURRENT_DATE; }
  "CURRENT_TIMESTAMP"                 { return CURRENT_TIMESTAMP; }
  "LIMIT"                             { return LIMIT; }
  "OFFSET"                            { return OFFSET; }
  "ORDER"                             { return ORDER; }
  "BY"                                { return BY; }
  "ALL"                               { return ALL; }
  "GROUP"                             { return GROUP; }
  "HAVING"                            { return HAVING; }
  "WHERE"                             { return WHERE; }
  "FROM"                              { return FROM; }
  "LEFT"                              { return LEFT; }
  "OUTER"                             { return OUTER; }
  "INNER"                             { return INNER; }
  "CROSS"                             { return CROSS; }
  "JOIN"                              { return JOIN; }
  "ON"                                { return ON; }
  "USING"                             { return USING; }
  "ASC"                               { return ASC; }
  "DESC"                              { return DESC; }
  "UNION"                             { return UNION; }
  "ASTERISK"                          { return ASTERISK; }

  {NUMERIC_LITERAL}                   { return NUMERIC_LITERAL; }
  {SINGLE_QUOTE_STRING_LITERAL}       { return SINGLE_QUOTE_STRING_LITERAL; }
  {DOUBLE_QUOTE_STRING_LITERAL}       { return DOUBLE_QUOTE_STRING_LITERAL; }
  {BRACKET_LITERAL}                   { return BRACKET_LITERAL; }
  {BACKTICK_LITERAL}                  { return BACKTICK_LITERAL; }
  {IDENTIFIER}                        { return IDENTIFIER; }
  {NAMED_PARAMETER}                   { return NAMED_PARAMETER; }
  {COMMENT}                           { return COMMENT; }
  {LINE_COMMENT}                      { return LINE_COMMENT; }
  {WHITE_SPACE_FOR_LIVE_PREVIEW}      { return WHITE_SPACE_FOR_LIVE_PREVIEW; }

}

[^] { return BAD_CHARACTER; }
