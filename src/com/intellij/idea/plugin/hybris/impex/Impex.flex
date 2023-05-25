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

package com.intellij.idea.plugin.hybris.impex;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.CustomHighlighterTokenType;

%%

%class ImpexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
    return;
%eof}
%ignorecase

identifier  = [a-zA-Z0-9_-]

crlf        = (([\n])|([\r])|(\r\n))
not_crlf    = [^\r\n]
white_space = [ \t\f]

line_comment = [#][^\r\n]*

bean_shell_marker = [#][%]
bean_shell_body = (({double_string})|{not_crlf}*)

single_string = ['](('')|([^'\r\n])*)[']
// Double string can contain line break
double_string = [\"](([\"][\"])|[^\"])*[\"]

macro_name_declaration = [$](([a-zA-Z0-9_-]|(config-)))+{white_space}*[=]
root_macro_usage       = [$]([\.\(\)a-zA-Z0-9_-])+
macro_usage            = [$](config-)?({identifier}({dot})?)+
macro_config_usage     = [$](config-)({identifier}({dot})?)+
macro_value            = ({not_crlf}|({identifier}({dot})?)+)

left_square_bracket  = [\[]
right_square_bracket = [\]]

left_round_bracket  = [\(]
right_round_bracket = [\)]

semicolon    = [;]
comma        = [,]
dot          = [.]
assign_value = [=]

default_path_delimiter      = [:]
default_key_value_delimiter = "->"
alternative_map_delimiter   = [|]

boolean = (("true")|("false"))
digit   = [-+]?[0-9]+([.][0-9]+)?
//class_with_package = ({identifier}+[.]{identifier}+)+

parameter_name = ({identifier}+([.]?{identifier}+)*)+
alternative_pattern = [|]
special_parameter_name = [@]{identifier}+

attribute_name  = ({identifier}|[.])+
attribute_value = [^, \t\f\]\r\n]+

document_id = [&]{identifier}+

header_mode_insert        = "INSERT"
header_mode_update        = "UPDATE"
header_mode_insert_update = "INSERT_UPDATE"
header_mode_remove        = "REMOVE"

header_type = {identifier}+

value_subtype      = {identifier}+
field_value        = ({not_crlf}|{identifier}+)
field_value_url    = ([/]{identifier}+)+[.]{identifier}+
field_value_ignore = "<ignore>"

start_userrights                  = [$]START_USERRIGHTS
end_userrights                    = [$]END_USERRIGHTS

%state WAITING_MACRO_VALUE
%state MACRO_DECLARATION
%state HEADER_TYPE
%state HEADER_LINE
%state FIELD_VALUE
%state BEAN_SHELL
%state MODYFIERS_BLOCK
%state WAITING_ATTR_OR_PARAM_VALUE
%state HEADER_PARAMETERS
%state MACRO_USAGE
%state MACRO_CONFIG_USAGE
%state WAITING_MACRO_CONFIG_USAGE
%state USER_RIGHTS_START
%state USER_RIGHTS_END
%state USER_RIGHTS_HEADER_LINE
%state USER_RIGHTS_WAIT_FOR_VALUE_LINE
%state USER_RIGHTS_VALUE_LINE

%%

{white_space}+                                              { return TokenType.WHITE_SPACE; }

<YYINITIAL> {
    {bean_shell_marker}                                     { yybegin(BEAN_SHELL); return ImpexTypes.BEAN_SHELL_MARKER; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {line_comment}                                          { yybegin(YYINITIAL); return ImpexTypes.LINE_COMMENT; }

    {start_userrights}                                      { yybegin(USER_RIGHTS_START); return ImpexTypes.START_USERRIGHTS; }
    {root_macro_usage}                                      { return ImpexTypes.MACRO_USAGE; }
    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {macro_name_declaration}                                {
                                                              yybegin(MACRO_DECLARATION);
                                                              /* Push back '='. */
                                                              yypushback(1);
                                                              /* Push back spaces. */
                                                              yypushback(yylength() - yytext().toString().trim().length());
                                                              return ImpexTypes.MACRO_NAME_DECLARATION;
                                                            }

    {header_mode_insert}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_INSERT; }
    {header_mode_update}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_UPDATE; }
    {header_mode_insert_update}                             { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_INSERT_UPDATE; }
    {header_mode_remove}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_REMOVE; }

    {value_subtype}                                         { yybegin(FIELD_VALUE); return ImpexTypes.VALUE_SUBTYPE; }
    {semicolon}                                             { yybegin(FIELD_VALUE); return ImpexTypes.FIELD_VALUE_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<USER_RIGHTS_START> {
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(USER_RIGHTS_HEADER_LINE); return ImpexTypes.CRLF; }
}

<USER_RIGHTS_HEADER_LINE> {
    "type"                                                  { return ImpexTypes.TYPE; }
    "uid"                                                   { return ImpexTypes.UID; }
    "MemberOfGroups"                                        { return ImpexTypes.MEMBEROFGROUPS; }
    "password"                                              { return ImpexTypes.PASSWORD; }
    "target"                                                { return ImpexTypes.TARGET; }
    "read"                                                  { return ImpexTypes.READ; }
    "change"                                                { return ImpexTypes.CHANGE; }
    "create"                                                { return ImpexTypes.CREATE; }
    "delete"                                                { return ImpexTypes.DELETE; }
    "remove"                                                { return ImpexTypes.REMOVE; }
    "change_perm"                                           { return ImpexTypes.CHANGE_PERM; }
    {line_comment}                                          { return ImpexTypes.LINE_COMMENT; }
    {semicolon}                                             { yybegin(USER_RIGHTS_WAIT_FOR_VALUE_LINE); return ImpexTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpexTypes.END_USERRIGHTS; }
    {crlf}                                                  { return ImpexTypes.CRLF; }
}

<USER_RIGHTS_WAIT_FOR_VALUE_LINE> {
    "type"                                                  { return ImpexTypes.TYPE; }
    "uid"                                                   { return ImpexTypes.UID; }
    "MemberOfGroups"                                        { return ImpexTypes.MEMBEROFGROUPS; }
    "password"                                              { return ImpexTypes.PASSWORD; }
    "target"                                                { return ImpexTypes.TARGET; }
    "read"                                                  { return ImpexTypes.READ; }
    "change"                                                { return ImpexTypes.CHANGE; }
    "create"                                                { return ImpexTypes.CREATE; }
    "delete"                                                { return ImpexTypes.DELETE; }
    "remove"                                                { return ImpexTypes.REMOVE; }
    "change_perm"                                           { return ImpexTypes.CHANGE_PERM; }
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpexTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return ImpexTypes.CRLF; }
}

<USER_RIGHTS_VALUE_LINE> {
    "-"                                                     { return ImpexTypes.PERMISSION_DENIED; }
    "+"                                                     { return ImpexTypes.PERMISSION_ALLOWED; }
    {identifier}+                                           { return ImpexTypes.FIELD_VALUE; }
    {line_comment}                                          { return ImpexTypes.LINE_COMMENT; }
    {semicolon}                                             { return ImpexTypes.FIELD_VALUE_SEPARATOR; }
    {dot}                                                   { return ImpexTypes.DOT; }
    {comma}                                                 { return ImpexTypes.COMMA; }

    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return ImpexTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return ImpexTypes.CRLF; }
}

<USER_RIGHTS_END> {
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<BEAN_SHELL> {
    {bean_shell_body}                                       { return ImpexTypes.BEAN_SHELL_BODY; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<FIELD_VALUE> {
    {semicolon}                                             { return ImpexTypes.FIELD_VALUE_SEPARATOR; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }
    {field_value_ignore}                                    { return ImpexTypes.FIELD_VALUE_IGNORE; }
    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
//    {class_with_package}                                    { return ImpexTypes.CLASS_WITH_PACKAGE; }

    {comma}                                                 { return ImpexTypes.FIELD_LIST_ITEM_SEPARATOR; }
    {default_path_delimiter}                                { return ImpexTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
    {default_key_value_delimiter}                           { return ImpexTypes.DEFAULT_KEY_VALUE_DELIMITER; }

    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }

    {field_value_url}                                       { return ImpexTypes.FIELD_VALUE_URL; }
    {field_value}                                           { return ImpexTypes.FIELD_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<HEADER_TYPE> {
    {header_type}                                           { yybegin(HEADER_LINE); return ImpexTypes.HEADER_TYPE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<HEADER_LINE> {
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }
    {comma}                                                 { return ImpexTypes.COMMA; }
    {dot}                                                   { return ImpexTypes.DOT; }

    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {document_id}                                           { return ImpexTypes.DOCUMENT_ID; }
    {parameter_name}{white_space}?+{left_round_bracket}     {
                                                              yybegin(HEADER_LINE);
                                                              yypushback(1);
                                                              return ImpexTypes.FUNCTION; 
                                                            }
    {parameter_name}                                        { return ImpexTypes.HEADER_PARAMETER_NAME; }
    {alternative_pattern}                                   { return ImpexTypes.ALTERNATIVE_PATTERN; }
    {special_parameter_name}                                { return ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME; }
    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpexTypes.ASSIGN_VALUE; }

    {left_round_bracket}                                    { return ImpexTypes.LEFT_ROUND_BRACKET; }
    {right_round_bracket}                                   { return ImpexTypes.RIGHT_ROUND_BRACKET; }

    {left_square_bracket}                                   { yybegin(MODYFIERS_BLOCK); return ImpexTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpexTypes.RIGHT_SQUARE_BRACKET; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<MODYFIERS_BLOCK> {
    {attribute_name}                                        { return ImpexTypes.ATTRIBUTE_NAME; }

    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpexTypes.ASSIGN_VALUE; }

    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpexTypes.RIGHT_SQUARE_BRACKET; }

    {comma}                                                 { return ImpexTypes.ATTRIBUTE_SEPARATOR; }

    {alternative_map_delimiter}                             { yybegin(MODYFIERS_BLOCK); return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<WAITING_ATTR_OR_PARAM_VALUE> {
    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }
//    {class_with_package}                                    { return ImpexTypes.CLASS_WITH_PACKAGE; }
    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {comma}                                                 { yybegin(MODYFIERS_BLOCK); return ImpexTypes.ATTRIBUTE_SEPARATOR; }
    {attribute_value}                                       { return ImpexTypes.ATTRIBUTE_VALUE; }
    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpexTypes.RIGHT_SQUARE_BRACKET; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<MACRO_DECLARATION> {
    {assign_value}                                          { yybegin(WAITING_MACRO_VALUE); return ImpexTypes.ASSIGN_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<WAITING_MACRO_VALUE> {
    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {macro_usage}                                           { yypushback(yylength()); yybegin(WAITING_MACRO_CONFIG_USAGE); }
    {special_parameter_name}                                { return ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME; }

    {left_round_bracket}                                    { return ImpexTypes.LEFT_ROUND_BRACKET; }
    {right_round_bracket}                                   { return ImpexTypes.RIGHT_ROUND_BRACKET; }

    {left_square_bracket}                                   { return ImpexTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpexTypes.RIGHT_SQUARE_BRACKET; }

//    {assign_value}                                          { return ImpexTypes.ASSIGN_VALUE; }

    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
    {field_value_ignore}                                    { return ImpexTypes.FIELD_VALUE_IGNORE; }

    {comma}                                                 { return ImpexTypes.COMMA; }

    {header_mode_insert}                                    { return ImpexTypes.HEADER_MODE_INSERT; }
    {header_mode_update}                                    { return ImpexTypes.HEADER_MODE_UPDATE; }
    {header_mode_insert_update}                             { return ImpexTypes.HEADER_MODE_INSERT_UPDATE; }
    {header_mode_remove}                                    { return ImpexTypes.HEADER_MODE_REMOVE; }

    {macro_value}                                           { return ImpexTypes.MACRO_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<WAITING_MACRO_CONFIG_USAGE> {
    {macro_config_usage}                                     { yybegin(WAITING_MACRO_VALUE); return ImpexTypes.MACRO_USAGE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
   .                                                        { yypushback(yylength()); yybegin(MACRO_USAGE); }
}

<MACRO_USAGE> {
    {macro_usage}                                            { yybegin(WAITING_MACRO_VALUE); return ImpexTypes.MACRO_USAGE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
