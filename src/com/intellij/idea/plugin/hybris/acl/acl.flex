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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.idea.plugin.hybris.acl.psi.AclTypes.*;

%%

%{
  private AtomicInteger permissionHeader = new AtomicInteger(0);
  private AtomicInteger valueColumn = new AtomicInteger(0);
  private AtomicInteger targetIdentifiers = new AtomicInteger(0);
  private AtomicBoolean passwordColumnPresent = new AtomicBoolean(false);
  private AtomicBoolean headerFound = new AtomicBoolean(false);
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
y_white_space = {crlf}({crlf}|{white_space})*

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
{line_comment}                                              { return AclTypes.LINE_COMMENT; }

<YYINITIAL> {
    {start_userrights}                                      { yybegin(USER_RIGHTS_START); return AclTypes.START_USERRIGHTS; }
    <<EOF>>                                                 { return null; }
}

<USER_RIGHTS_START> {
    {semicolon}                                             { return AclTypes.DUMMY_SEPARATOR; }
    {y_white_space}                                         {
        yybegin(USER_RIGHTS_HEADER_LINE);
        permissionHeader.set(0);
        passwordColumnPresent.set(false);
        headerFound.set(false);
        return TokenType.WHITE_SPACE;
    }
}

<USER_RIGHTS_HEADER_LINE> {
    "Type"                                                  { headerFound.set(true); return AclTypes.HEADER_TYPE; }
    "UID"                                                   { return AclTypes.HEADER_UID; }
    "MemberOfGroups"                                        { return AclTypes.HEADER_MEMBEROFGROUPS; }
    "Password"                                              { passwordColumnPresent.set(true); return AclTypes.HEADER_PASSWORD; }
    "Target"                                                { return AclTypes.HEADER_TARGET; }
    {identifier}+                                           {
        return switch (permissionHeader.incrementAndGet()) {
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
    {y_white_space}                                         {
        if (headerFound.get()) {
            valueColumn.set(0);
            targetIdentifiers.set(0);
            yybegin(USER_RIGHTS_VALUE_LINE);
        }
        return TokenType.WHITE_SPACE;
    }
}

<USER_RIGHTS_VALUE_LINE> {
    {minus}                                                 { return AclTypes.PERMISSION_DENIED; }
    {plus}                                                  { return AclTypes.PERMISSION_GRANTED; }
    {identifier}+                                           {
            if (passwordColumnPresent.get() && valueColumn.get() == 4 || valueColumn.get() == 3) {
                return switch (targetIdentifiers.getAndIncrement()) {
                    case 0 -> AclTypes.FIELD_VALUE_TARGET_TYPE;
                    case 1 -> AclTypes.FIELD_VALUE_TARGET_ATTRIBUTE;
                    default -> AclTypes.FIELD_VALUE;
                };
            }
            return valueColumn.get() == 0
                ? AclTypes.FIELD_VALUE_TYPE
                : AclTypes.FIELD_VALUE;
        }
    {dot}                                                   {
          return passwordColumnPresent.get() && valueColumn.get() >= 5
            || !passwordColumnPresent.get() && valueColumn.get() >= 4
            ? AclTypes.PERMISSION_INHERITED
            : AclTypes.DOT;
      }
    {comma}                                                 { return AclTypes.COMMA; }

    {semicolon}                                             {
        valueColumn.incrementAndGet();
        if (passwordColumnPresent.get() && valueColumn.get() == 3) yybegin(USER_RIGHTS_VALUE_PASSWORD);
        return valueColumn.get() == 1
            ? AclTypes.FIELD_VALUE_TYPE_SEPARATOR
            : AclTypes.FIELD_VALUE_SEPARATOR;
    }
    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return AclTypes.END_USERRIGHTS; }
    {y_white_space}                                         {
        valueColumn.set(0);
        targetIdentifiers.set(0);
        yybegin(USER_RIGHTS_VALUE_LINE);
        return TokenType.WHITE_SPACE;
    }
}

<USER_RIGHTS_VALUE_PASSWORD> {
    {triple_quoted_string}                                  { return AclTypes.PASSWORD; }
    {single_quoted_string}                                  { return AclTypes.PASSWORD; }
    {unquoted_string}                                       { return AclTypes.PASSWORD; }

    {semicolon}                                             {
        valueColumn.incrementAndGet();
        yybegin(USER_RIGHTS_VALUE_LINE);
        return AclTypes.FIELD_VALUE_SEPARATOR;
    }
    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return AclTypes.END_USERRIGHTS; }
    {y_white_space}                                         { valueColumn.set(0); yybegin(USER_RIGHTS_VALUE_LINE); return TokenType.WHITE_SPACE; }
}

<USER_RIGHTS_END> {
    {semicolon}                                             { return AclTypes.DUMMY_SEPARATOR; }
}

{y_white_space}                                             { yybegin(YYINITIAL); return AclTypes.CRLF; }

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
