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
not_crlf    = [^\r\n]
white_space = [ \t\f]

line_comment = [#]{not_crlf}*

semicolon     = [;]
comma         = [,]
dot           = [.]

field_value        = ({not_crlf}|{identifier}+)

start_userrights                  = [$]START_USERRIGHTS
end_userrights                    = [$]END_USERRIGHTS

%state USER_RIGHTS_START
%state USER_RIGHTS_END
%state USER_RIGHTS_HEADER_LINE
%state USER_RIGHTS_WAIT_FOR_VALUE_LINE
%state USER_RIGHTS_VALUE_LINE

%%

{white_space}+                                              { return TokenType.WHITE_SPACE; }

<YYINITIAL> {
    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {start_userrights}                                      { yybegin(USER_RIGHTS_START); return AclTypes.START_USERRIGHTS; }
    {crlf}                                                  { yybegin(YYINITIAL); return AclTypes.CRLF; }
}

<USER_RIGHTS_START> {
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(USER_RIGHTS_HEADER_LINE); return AclTypes.CRLF; }
}

<USER_RIGHTS_HEADER_LINE> {
    "Type"                                                  { return AclTypes.TYPE; }
    "UID"                                                   { return AclTypes.UID; }
    "MemberOfGroups"                                        { return AclTypes.MEMBEROFGROUPS; }
    "Password"                                              { return AclTypes.PASSWORD; }
    "Target"                                                { return AclTypes.TARGET; }
    {identifier}+                                           { return AclTypes.PERMISSION; }
    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {semicolon}                                             { yybegin(USER_RIGHTS_WAIT_FOR_VALUE_LINE); return AclTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  { return AclTypes.CRLF; }
}

<USER_RIGHTS_WAIT_FOR_VALUE_LINE> {
    "Type"                                                  { return AclTypes.TYPE; }
    "UID"                                                   { return AclTypes.UID; }
    "MemberOfGroups"                                        { return AclTypes.MEMBEROFGROUPS; }
    "Password"                                              { return AclTypes.PASSWORD; }
    "Target"                                                { return AclTypes.TARGET; }
    {identifier}+                                           { return AclTypes.PERMISSION; }
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return AclTypes.CRLF; }
}

<USER_RIGHTS_VALUE_LINE> {
// even if we may have one more Header line in the body of the user rights, it will be ignored by ImportExportUserRightsHelper
//    {user_rights_type}                                      { yybegin(USER_RIGHTS_HEADER_LINE); yypushback(yylength()); }
    "-"                                                     { return AclTypes.PERMISSION_DENIED; }
    "+"                                                     { return AclTypes.PERMISSION_ALLOWED; }
    {identifier}+                                           { return AclTypes.FIELD_VALUE; }
    {line_comment}                                          { return AclTypes.LINE_COMMENT; }
    {semicolon}                                             { return AclTypes.FIELD_VALUE_SEPARATOR; }
    {dot}                                                   { return AclTypes.DOT; }
    {comma}                                                 { return AclTypes.COMMA; }

    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return AclTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return AclTypes.CRLF; }
}

<USER_RIGHTS_END> {
    {semicolon}                                             { return AclTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return AclTypes.CRLF; }
}

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
