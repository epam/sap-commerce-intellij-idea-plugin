/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

// See SAP Commerce sources: ImpExConstants

identifier  = [a-zA-Z0-9_-]

crlf        = (([\n])|([\r])|(\r\n))
not_crlf    = [^\r\n]
white_space = [ \t\f]
backslash   = [\\]

multiline_separator   = {backslash}{crlf}

line_comment = [#]{not_crlf}*

single_string = ['](('')|([^'\r\n])*)[']
// Double string can contain line break
double_string = [\"](([\"][\"])|[^\"])*[\"]

script_action = (beforeEach | afterEach | if | endif)[:]
script_body_value = [^ '\"\r\n]+

macro_name_declaration = [$](([\w\d-]|{white_space})+({backslash}\s*)*)+[=]
root_macro_usage       = [$]([\.\(\)a-zA-Z0-9_-])+
macro_usage            = [$](config-)?({identifier}({dot})?)+
macro_config_usage     = [$](config-)({identifier}({dot})?)+
macro_value            = ({not_crlf}|({identifier}({dot})?)+|({backslash}\s*)+)

left_square_bracket  = [\[]
right_square_bracket = [\]]

left_round_bracket  = [\(]
right_round_bracket = [\)]

semicolon     = [;]
comma         = [,]
dot           = [.]
assign_value  = [=]

// see - CollectionValueTranslator
// value must start with this prefix
collection_append_prefix = "(+)"
collection_remove_prefix = "(-)"
collection_merge_prefix  = "(+?)"

default_path_delimiter      = [:]
default_key_value_delimiter = "->"
alternative_map_delimiter   = [|]

// see - AtomicValueTranslator
boolean = ("true"|"false"|"ja"|"nein"|"wahr"|"falsch"|"y"|"n"|-|\+)

digit   = [-+]?[0-9]+([.][0-9]+)?
//class_with_package = ({identifier}+[.]{identifier}+)+

parameter_name = ({identifier}+([.@]?{identifier}+)*)+
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
//field_value_url    = ([/]{identifier}+)+[.]{identifier}+
field_value_ignore = "<ignore>"
field_value_null   = "<null>"
field_value_prefix_password_encoding = "*:" | "plain:" | "sha-256:" | "sha-512:" | "md5:" | "pbkdf2:"

start_userrights                  = [$]START_USERRIGHTS
end_userrights                    = [$]END_USERRIGHTS

%state WAITING_MACRO_VALUE
%state MACRO_DECLARATION
%state HEADER_TYPE
%state HEADER_LINE
%state FIELD_VALUE
%state BEAN_SHELL
%state SCRIPT
%state SCRIPT_BODY
%state MODIFIERS_BLOCK
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
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {line_comment}                                          {
                                                                final String text = yytext().toString().trim();
                                                                int index = text.indexOf("#%groovy%");

                                                                if (index > -1) {
                                                                    yybegin(SCRIPT_BODY);
                                                                    yypushback(yylength() - 9);
                                                                    return ImpexTypes.GROOVY_MARKER;
                                                                }

                                                                index = text.indexOf("#%javascript%");
                                                                if (index > -1) {
                                                                    yybegin(SCRIPT_BODY);
                                                                    yypushback(yylength() - 13);
                                                                    return ImpexTypes.JAVASCRIPT_MARKER;
                                                                }

                                                                index = text.indexOf("#%bsh%");
                                                                if (index > -1) {
                                                                    yybegin(SCRIPT_BODY);
                                                                    yypushback(yylength() - 6);
                                                                    return ImpexTypes.BEAN_SHELL_MARKER;
                                                                }

                                                                index = text.indexOf("#%");
                                                                if (index > -1) {
                                                                    yybegin(SCRIPT_BODY);
                                                                    yypushback(yylength() - 2);
                                                                    return ImpexTypes.BEAN_SHELL_MARKER;
                                                                }

                                                                return ImpexTypes.LINE_COMMENT;
                                                            }

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
    "Type"                                                  { return ImpexTypes.TYPE; }
    "UID"                                                   { return ImpexTypes.UID; }
    "MemberOfGroups"                                        { return ImpexTypes.MEMBEROFGROUPS; }
    "Password"                                              { return ImpexTypes.PASSWORD; }
    "Target"                                                { return ImpexTypes.TARGET; }
    {identifier}+                                           { return ImpexTypes.PERMISSION; }
    {line_comment}                                          { return ImpexTypes.LINE_COMMENT; }
    {semicolon}                                             { yybegin(USER_RIGHTS_WAIT_FOR_VALUE_LINE); return ImpexTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpexTypes.END_USERRIGHTS; }
    {crlf}                                                  { return ImpexTypes.CRLF; }
}

<USER_RIGHTS_WAIT_FOR_VALUE_LINE> {
    "Type"                                                  { return ImpexTypes.TYPE; }
    "UID"                                                   { return ImpexTypes.UID; }
    "MemberOfGroups"                                        { return ImpexTypes.MEMBEROFGROUPS; }
    "Password"                                              { return ImpexTypes.PASSWORD; }
    "Target"                                                { return ImpexTypes.TARGET; }
    {identifier}+                                           { return ImpexTypes.PERMISSION; }
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpexTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return ImpexTypes.CRLF; }
}

<USER_RIGHTS_VALUE_LINE> {
// even if we may have one more Header line in the body of the user rights, it will be ignored by ImportExportUserRightsHelper
//    {user_rights_type}                                      { yybegin(USER_RIGHTS_HEADER_LINE); yypushback(yylength()); }
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

<SCRIPT_BODY> {
    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {script_action}                                         { return ImpexTypes.SCRIPT_ACTION;}
    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }
    {script_body_value}                                     { return ImpexTypes.SCRIPT_BODY_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<FIELD_VALUE> {
    {field_value_prefix_password_encoding}                  { return ImpexTypes.FIELD_VALUE_PASSWORD_ENCODING_PREFIX; }
    "zip:"                                                  { return ImpexTypes.FIELD_VALUE_ZIP_PREFIX; }
    "file:"                                                 { return ImpexTypes.FIELD_VALUE_FILE_PREFIX; }
    "jar:"                                                  { return ImpexTypes.FIELD_VALUE_JAR_PREFIX; }
    "model://"                                              { return ImpexTypes.FIELD_VALUE_SCRIPT_PREFIX; }
    "/medias/"                                              { return ImpexTypes.FIELD_VALUE_EXPLODED_JAR_PREFIX; }
    "http:http"                                             {
                                                                yypushback(4);
                                                                return ImpexTypes.FIELD_VALUE_HTTP_PREFIX;
                                                            }
    {semicolon}                                             { return ImpexTypes.FIELD_VALUE_SEPARATOR; }
    {multiline_separator}                                   { return ImpexTypes.MULTILINE_SEPARATOR; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }
    {field_value_ignore}                                    { return ImpexTypes.FIELD_VALUE_IGNORE; }
    {field_value_null}                                      { return ImpexTypes.FIELD_VALUE_NULL; }
    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
//    {class_with_package}                                    { return ImpexTypes.CLASS_WITH_PACKAGE; }

    {comma}                                                 { return ImpexTypes.FIELD_LIST_ITEM_SEPARATOR; }
    {default_path_delimiter}                                { return ImpexTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
    {default_key_value_delimiter}                           { return ImpexTypes.DEFAULT_KEY_VALUE_DELIMITER; }

    {collection_append_prefix}                              { return ImpexTypes.COLLECTION_APPEND_PREFIX; }
    {collection_remove_prefix}                              { return ImpexTypes.COLLECTION_REMOVE_PREFIX; }
    {collection_merge_prefix}                               { return ImpexTypes.COLLECTION_MERGE_PREFIX; }

    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }

//    {field_value_url}                                       { return ImpexTypes.FIELD_VALUE_URL; }
    {field_value}                                           { return ImpexTypes.FIELD_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<HEADER_TYPE> {
    {header_type}                                           { yybegin(HEADER_LINE); return ImpexTypes.HEADER_TYPE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<HEADER_LINE> {
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }
    {multiline_separator}                                   { return ImpexTypes.MULTILINE_SEPARATOR; }
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

    {left_square_bracket}                                   { yybegin(MODIFIERS_BLOCK); return ImpexTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpexTypes.RIGHT_SQUARE_BRACKET; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpexTypes.CRLF; }
}

<MODIFIERS_BLOCK> {
    {attribute_name}                                        { return ImpexTypes.ATTRIBUTE_NAME; }

    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpexTypes.ASSIGN_VALUE; }

    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpexTypes.RIGHT_SQUARE_BRACKET; }

    {comma}                                                 { return ImpexTypes.ATTRIBUTE_SEPARATOR; }

    {alternative_map_delimiter}                             { yybegin(MODIFIERS_BLOCK); return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
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
    {comma}                                                 { yybegin(MODIFIERS_BLOCK); return ImpexTypes.ATTRIBUTE_SEPARATOR; }
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

    {assign_value}                                          { return ImpexTypes.ASSIGN_VALUE; }

    {left_square_bracket}                                   { return ImpexTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpexTypes.RIGHT_SQUARE_BRACKET; }

    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
    {field_value_ignore}                                    { return ImpexTypes.FIELD_VALUE_IGNORE; }
    {field_value_null}                                      { return ImpexTypes.FIELD_VALUE_NULL; }

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
