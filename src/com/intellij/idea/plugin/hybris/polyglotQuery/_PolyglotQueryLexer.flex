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

EOL=\R
WHITE_SPACE=\s+

IDENTIFIER=([:letter:])([:letter:]|[:digit:]|_|\.)*

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return WHITE_SPACE; }

  "?"                { return QUESTION_MARK; }
  "["                { return LBRACKET; }
  "]"                { return RBRACKET; }
  "{"                { return LBRACE; }
  "}"                { return RBRACE; }
  "&"                { return AMP; }
  "="                { return EQ; }
  ">"                { return GT; }
  ">="               { return GTE; }
  "<"                { return LT; }
  "<="               { return LTE; }
  "<>"               { return UNEQ; }

  {IDENTIFIER}       { return IDENTIFIER; }

}

[^] { return BAD_CHARACTER; }
