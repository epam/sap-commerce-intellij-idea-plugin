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

package com.intellij.idea.plugin.hybris.acl;

import com.intellij.lexer.FlexLexer;
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.idea.plugin.hybris.acl.psi.AclTypes.*;

%%

%{
  private int permissionHeader = 0;
  private int valueColumn = 0;
  private boolean passwordColumnPresent = false;
  private boolean headerFound = false;
  public _AclLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _AclLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%eof{
    return;
%eof}
%ignorecase

identifier  = [a-zA-Z0-9_-]

crlf        = (([\n])|([\r])|(\r\n))
crlf_char   = [\r\n]
not_crlf    = [^\r\n]
white_space = [ \t\f]

line_comment = [#]{not_crlf}*

semicolon     = [;]
comma         = [,]
dot           = [.]
plus          = [+]
minus         = [-]

triple_quoted_string = \"\"\"[^{crlf_char}][^{crlf_char}]*\"\"\"
single_quoted_string = \"[^;\"{crlf_char}][^;\"{crlf_char}]*\"
unquoted_string = [^;\"{white_space}{crlf_char}]([^;\"{crlf_char}]*[^;\"{white_space}{crlf_char}])?

start_userrights                  = [$]START_USERRIGHTS
end_userrights                    = [$]END_USERRIGHTS

%state USER_RIGHTS_START
%state USER_RIGHTS_END
%state USER_RIGHTS_HEADER_LINE
%state USER_RIGHTS_VALUE_LINE
%state USER_RIGHTS_VALUE_PASSWORD

%%

{white_space}+                                              { return TokenType.WHITE_SPACE; }

<YYINITIAL> {
    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {start_userrights}                                      { yybegin(USER_RIGHTS_START); return AclTypes.START_USERRIGHTS; }
    {crlf}                                                  { yybegin(YYINITIAL); return AclTypes.CRLF; }
}

<USER_RIGHTS_START> {
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  {
        yybegin(USER_RIGHTS_HEADER_LINE);
        permissionHeader=0;
        passwordColumnPresent=false;
        headerFound=false;
        return AclTypes.CRLF;
    }
}

<USER_RIGHTS_HEADER_LINE> {
    "Type"                                                  { headerFound=true; return AclTypes.HEADER_TYPE; }
    "UID"                                                   { return AclTypes.HEADER_UID; }
    "MemberOfGroups"                                        { return AclTypes.HEADER_MEMBEROFGROUPS; }
    "Password"                                              { passwordColumnPresent=true; return AclTypes.HEADER_PASSWORD; }
    "Target"                                                { return AclTypes.HEADER_TARGET; }
    {identifier}+                                           {
        permissionHeader++;

        return switch (permissionHeader) {
            case 1 -> AclTypes.HEADER_READ;
            case 2 -> AclTypes.HEADER_CHANGE;
            case 3 -> AclTypes.HEADER_CREATE;
            case 4 -> AclTypes.HEADER_REMOVE;
            case 5 -> AclTypes.HEADER_CHANGE_PERM;
            // any other columns are not expected
            default -> TokenType.BAD_CHARACTER;
        };
    }
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  {
        if (headerFound) {
            valueColumn=0;
            yybegin(USER_RIGHTS_VALUE_LINE);
        }
        return AclTypes.CRLF;
    }
}

<USER_RIGHTS_VALUE_LINE> {
    {minus}                                                 { return AclTypes.PERMISSION_DENIED; }
    {plus}                                                  { return AclTypes.PERMISSION_ALLOWED; }
    {identifier}+                                           { return AclTypes.FIELD_VALUE; }
    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {dot}                                                   {
          if (passwordColumnPresent && valueColumn >= 5 || !passwordColumnPresent && valueColumn >= 4) return AclTypes.PERMISSION_INHERITED;
          return AclTypes.DOT;
      }
    {comma}                                                 { return AclTypes.COMMA; }

    {semicolon}                                             {
        valueColumn++;
        if (passwordColumnPresent && valueColumn == 3) yybegin(USER_RIGHTS_VALUE_PASSWORD);
        return AclTypes.FIELD_VALUE_SEPARATOR;
    }
    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  { valueColumn=0; yybegin(USER_RIGHTS_VALUE_LINE); return AclTypes.CRLF; }
}

<USER_RIGHTS_VALUE_PASSWORD> {
    {triple_quoted_string}                                  { return AclTypes.PASSWORD; }
    {single_quoted_string}                                  { return AclTypes.PASSWORD; }
    {unquoted_string}                                       { return AclTypes.PASSWORD; }

    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {semicolon}                                             {
        valueColumn++;
        yybegin(USER_RIGHTS_VALUE_LINE);
        return AclTypes.FIELD_VALUE_SEPARATOR;
    }
    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  { valueColumn=0; yybegin(USER_RIGHTS_VALUE_LINE); return AclTypes.CRLF; }
}

<USER_RIGHTS_END> {
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return AclTypes.CRLF; }
}

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
